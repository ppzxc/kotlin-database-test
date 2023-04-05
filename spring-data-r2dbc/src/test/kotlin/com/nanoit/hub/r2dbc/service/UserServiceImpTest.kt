package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.PatchUsersDto
import com.nanoit.hub.dto.PutUsersDto
import com.nanoit.hub.dto.SignInDto
import com.nanoit.hub.exceptions.MessengerErrorCode
import com.nanoit.hub.exceptions.NotFoundUserException
import com.nanoit.hub.exceptions.SignInFailedException
import com.nanoit.hub.exceptions.SignUpFailedException
import com.nanoit.hub.r2dbc.configuration.R2dbcConfiguration
import com.nanoit.hub.r2dbc.entity.AuthenticationsEntity
import com.nanoit.hub.r2dbc.entity.ConfigsEntity
import com.nanoit.hub.r2dbc.entity.UsersEntity
import com.nanoit.hub.r2dbc.entity.UsersEntityMapperImpl
import com.nanoit.hub.r2dbc.repository.AuthenticationsRepository
import com.nanoit.hub.r2dbc.repository.AuthoritiesRepository
import com.nanoit.hub.r2dbc.repository.ConfigsRepository
import com.nanoit.hub.r2dbc.repository.RolesRepository
import com.nanoit.hub.r2dbc.repository.UsersRepository
import com.nanoit.hub.security.jwt.JsonWebTokenProvider
import com.nanoit.hub.security.jwt.JsonWebTokenProviderConfiguration
import com.nanoit.hub.security.jwt.JsonWebTokenProviderImpl
import com.nanoit.hub.test.RandomUtils
import com.nanoit.hub.test.TestUtils.shouldBeByMillis
import com.nanoit.hub.test.container.PostgreSQLTestContainers
import com.nanoit.hub.test.entity.TestDeviceDto
import com.nanoit.hub.test.entity.TestSignUpDto
import com.nanoit.hub.test.entity.TestUsersEntity
import com.nanoit.hub.utility.spring.PasswordHelper
import io.kotest.core.spec.Spec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@DisplayName("INTEGRATION - UsersServiceImpl - SignUp")
@ActiveProfiles("test")
@Import(
    value = [
        R2dbcConfiguration::class,
        UsersServiceImpl::class,
        UsersEntityMapperImpl::class,
        JsonWebTokenProviderImpl::class, JsonWebTokenProviderConfiguration::class, TokenServiceImpl::class
    ]
)
@DataR2dbcTest
class UserServiceImpTest(
    @Autowired
    private val usersService: UsersService,
    @Autowired
    private val usersRepository: UsersRepository,
    @Autowired
    private val authenticationsRepository: AuthenticationsRepository,
    @Autowired
    private val configsRepository: ConfigsRepository,
    @Autowired
    private val authoritiesRepository: AuthoritiesRepository,
    @Autowired
    private val rolesRepository: RolesRepository,
    @Autowired
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    @Autowired
    private val jsonWebTokenProvider: JsonWebTokenProvider
) : PostgreSQLTestContainers() {

    override suspend fun beforeSpec(spec: Spec) {
        refreshPostgreSql(r2dbcEntityTemplate)
    }

    init {
        Given("회원이 한명 주어져 있을때") {
            val givenSignUp = TestSignUpDto.giveMeOne()
            val expectedSignUp = usersService.signUp(givenSignUp).block()!!

            When("회원 한명을 검색할 때") {

                Then("사용자 ID 로 찾을 수 있다.") {
                    usersService.getUser(expectedSignUp.id)
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBeGreaterThan 1
                            it.email shouldBe expectedSignUp.email
                            it.password shouldBe expectedSignUp.password
                            it.username shouldBe expectedSignUp.username
                            it.description shouldBe expectedSignUp.description
                            it.version shouldBe expectedSignUp.version
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeByMillis(expectedSignUp.lastModifiedDate).shouldBeTrue()
                        }.expectComplete()
                        .verify()
                }

                Then("없는 users.id 인경우 NOT FOUND EXCEPTION 이 발생한다.") {
                    val usersId = RandomUtils.long()
                    usersService.getUser(usersId)
                        .`as`(StepVerifier::create)
                        .expectErrorSatisfies {
                            val exception = it.shouldBeInstanceOf<NotFoundUserException>()
                            exception.errorCode shouldBe MessengerErrorCode.NOT_FOUND_USER
                            exception.explain shouldBe "usersId=$usersId"
                        }.verify()
                }
            }

            When("회원 PUT 메소드가 실행될 때") {
                val putDto = PutUsersDto(
                    RandomUtils.password(),
                    RandomUtils.string(),
                    RandomUtils.string()
                )

                Then("usersId 가 잘못된 경우  NOT FOUND EXCEPTION 이 반환 된다.") {
                    val usersId = RandomUtils.long()
                    usersService.putUsers(usersId, putDto)
                        .`as`(StepVerifier::create)
                        .expectErrorSatisfies {
                            val exception = it.shouldBeInstanceOf<NotFoundUserException>()
                            exception.errorCode shouldBe MessengerErrorCode.NOT_FOUND_USER
                            exception.explain shouldBe "usersId=$usersId"
                        }.verify()
                }

                Then("PUT DTO 대로 업데이트가 진행된다.") {
                    usersService.putUsers(expectedSignUp.id, putDto)
                        .flatMap { usersRepository.findById(expectedSignUp.id) }
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBe expectedSignUp.id
                            it.email shouldBe expectedSignUp.email
                            PasswordHelper.compare(putDto.password, it.password) shouldBe true
                            it.username shouldBe putDto.username
                            it.description shouldBe putDto.description
                            it.version shouldBe expectedSignUp.version + 1
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeAfter(expectedSignUp.lastModifiedDate)
                        }.expectComplete()
                        .verify()
                }
            }

            When("회원 PATCH 메소드가 실행될 때") {
                val patch = PatchUsersDto()

                Then("usersId 가 잘못된 경우  NOT FOUND EXCEPTION 이 반환 된다.") {
                    val usersId = RandomUtils.long()
                    usersService.patchUsers(usersId, patch.copy(password = RandomUtils.password()))
                        .`as`(StepVerifier::create)
                        .expectErrorSatisfies {
                            val exception = it.shouldBeInstanceOf<NotFoundUserException>()
                            exception.errorCode shouldBe MessengerErrorCode.NOT_FOUND_USER
                            exception.explain shouldBe "usersId=$usersId"
                        }.verify()
                }

                Then("PATCH password 만 업데이트 진행 된다.") {
                    val expectedPatch = patch.copy(password = RandomUtils.password())
                    usersService.patchUsers(expectedSignUp.id, expectedPatch)
                        .flatMap { usersRepository.findById(expectedSignUp.id) }
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBe expectedSignUp.id
                            it.email shouldBe expectedSignUp.email
                            PasswordHelper.compare(expectedPatch.password!!, it.password) shouldBe true
                            it.username shouldBe expectedSignUp.username
                            it.description shouldBe expectedSignUp.description
                            it.version shouldBe expectedSignUp.version + 1
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeAfter(expectedSignUp.lastModifiedDate)
                        }.expectComplete()
                        .verify()
                }

                Then("PATCH username 만 업데이트 진행 된다.") {
                    val expectedPatch = patch.copy(username = RandomUtils.string())
                    usersService.patchUsers(expectedSignUp.id, expectedPatch)
                        .flatMap { usersRepository.findById(expectedSignUp.id) }
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBe expectedSignUp.id
                            it.email shouldBe expectedSignUp.email
                            PasswordHelper.compare(givenSignUp.password, it.password) shouldBe true
                            it.username shouldBe expectedPatch.username
                            it.description shouldBe expectedSignUp.description
                            it.version shouldBe expectedSignUp.version + 1
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeAfter(expectedSignUp.lastModifiedDate)
                        }.expectComplete()
                        .verify()
                }

                Then("PATCH description 만 업데이트 진행 된다.") {
                    val expectedPatch = patch.copy(description = RandomUtils.string())
                    usersService.patchUsers(expectedSignUp.id, expectedPatch)
                        .flatMap { usersRepository.findById(expectedSignUp.id) }
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBe expectedSignUp.id
                            it.email shouldBe expectedSignUp.email
                            PasswordHelper.compare(givenSignUp.password, it.password) shouldBe true
                            it.username shouldBe expectedSignUp.username
                            it.description shouldBe expectedPatch.description
                            it.version shouldBe expectedSignUp.version + 1
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeAfter(expectedSignUp.lastModifiedDate)
                        }.expectComplete()
                        .verify()
                }

                Then("PATCH 모두 업데이트 진행 된다.") {
                    val expectedPatch = patch.copy(
                        password = RandomUtils.password(),
                        username = RandomUtils.string(),
                        description = RandomUtils.string()
                    )
                    usersService.patchUsers(expectedSignUp.id, expectedPatch)
                        .flatMap { usersRepository.findById(expectedSignUp.id) }
                        .`as`(StepVerifier::create)
                        .expectSubscription()
                        .assertNext {
                            it.id shouldBe expectedSignUp.id
                            it.email shouldBe expectedSignUp.email
                            PasswordHelper.compare(expectedPatch.password!!, it.password) shouldBe true
                            it.username shouldBe expectedPatch.username
                            it.description shouldBe expectedPatch.description
                            it.version shouldBe expectedSignUp.version + 1
                            it.createdDate.shouldBeByMillis(expectedSignUp.createdDate).shouldBeTrue()
                            it.lastModifiedDate.shouldBeAfter(expectedSignUp.lastModifiedDate)
                        }.expectComplete()
                        .verify()
                }
            }

            When("회원 DELETE 메소드가 실행될 때") {

                Then("usersId 가 없는 경우 NOT FOUND 가 반환된다") {
                    val usersId = RandomUtils.long()
                    usersService.deleteUsers(usersId)
                        .`as`(StepVerifier::create)
                        .expectErrorSatisfies {
                            val exception = it.shouldBeInstanceOf<NotFoundUserException>()
                            exception.errorCode shouldBe MessengerErrorCode.NOT_FOUND_USER
                            exception.explain shouldBe "usersId=$usersId"
                        }.verify()
                }

                Then("usersId 가 정상일 경우 삭제 된다") {
                    usersService.deleteUsers(expectedSignUp.id).block()
                    usersRepository.existsById(expectedSignUp.id)
                        .`as`(StepVerifier::create)
                        .assertNext {
                            it shouldBe false
                        }.expectComplete()
                        .verify()
                }
            }
        }

        Given("회원 가입이 요청되면") {
            val expectedSignUp = TestSignUpDto.giveMeOne()

            Then("정상 회원 가입이 진행된다") {
                usersService.signUp(expectedSignUp)
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.id shouldBeGreaterThan 1
                        it.email shouldBe expectedSignUp.email
                        it.password shouldBe "****"
                        it.username shouldBe expectedSignUp.username
                        it.description shouldBe expectedSignUp.description
                        it.version shouldBe 1
                        it.createdDate.shouldNotBeNull()
                        it.lastModifiedDate.shouldNotBeNull()
                    }.expectComplete()
                    .verify()
            }
        }

        Given("회원 가입된 유저가 한명이 있을때") {
            val expectedSignUp = TestSignUpDto.giveMeOne()
            usersService.signUp(expectedSignUp).block()!!

            Then("중복된 email 이 아니면 정상 회원 가입이 진행된다.") {
                val secondExpectedSignUp = TestSignUpDto.giveMeOne()
                usersService.signUp(secondExpectedSignUp)
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.id shouldBeGreaterThan 1
                        it.email shouldBe secondExpectedSignUp.email
                        it.password shouldBe "****"
                        it.username shouldBe secondExpectedSignUp.username
                        it.description shouldBe secondExpectedSignUp.description
                        it.version shouldBe 1
                        it.createdDate.shouldNotBeNull()
                        it.lastModifiedDate.shouldNotBeNull()
                    }.expectComplete()
                    .verify()
            }

            Then("중복된 email 요청은 거부된다.") {
                usersService.signUp(expectedSignUp)
                    .`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<SignUpFailedException>()
                        it.errorCode shouldBe MessengerErrorCode.DUPLICATE_EMAIL
                        it.explain shouldBe "duplicate email=${expectedSignUp.email}"
                    }
                    .verify()
            }
        }

        Given("회원이 한명 있을때") {
            val expectedSignUp = TestSignUpDto.giveMeOne()
            val expectedUsers = usersService.signUp(expectedSignUp).block()!!
            val expectedRoles =
                authoritiesRepository.findAllByUsersId(expectedUsers.id).collectList().flatMap { authorities ->
                    rolesRepository.findAllById(authorities.map { it.rolesId }).collectList()
                }.block()!!.map { it.role.name }

            Then("정상 로그인이 진행된다.") {
                usersService.signIn(
                    SignInDto(
                        expectedSignUp.email,
                        expectedSignUp.password,
                        TestDeviceDto.giveMeOne()
                    )
                )
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.shouldNotBeNull()
                        it.accessToken.shouldNotBeNull()
                        it.refreshToken.shouldNotBeNull()

                        val accessTokenCredentials = jsonWebTokenProvider.isDeniedAccessToken(it.accessToken)
                        accessTokenCredentials.shouldNotBeNull()
                        accessTokenCredentials.id shouldBe expectedUsers.id
                        accessTokenCredentials.authorities shouldBe expectedRoles

                        val refreshTokenCredentials = jsonWebTokenProvider.isDeniedRefreshToken(it.refreshToken)
                        refreshTokenCredentials.shouldNotBeNull()
                        refreshTokenCredentials.id shouldBe expectedUsers.id
                        refreshTokenCredentials.authorities shouldBe expectedRoles
                    }.expectComplete()
                    .verify()
            }

            Then("없는 email 은 로그인이 거부된다.") {
                val email = RandomUtils.email()
                usersService.signIn(
                    SignInDto(
                        email,
                        expectedSignUp.password,
                        TestDeviceDto.giveMeOne()
                    )
                )
                    .`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<SignInFailedException>()
                        it.errorCode shouldBe MessengerErrorCode.NOT_FOUND_USER
                        it.explain shouldBe "email=$email"
                    }.verify()
            }

            Then("틀린 password 는 로그인이 거부된다.") {
                usersService.signIn(
                    SignInDto(
                        expectedSignUp.email,
                        RandomUtils.string(),
                        TestDeviceDto.giveMeOne()
                    )
                )
                    .`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<SignInFailedException>()
                        it.errorCode shouldBe MessengerErrorCode.INVALID_CREDENTIALS
                        it.explain shouldBe MessengerErrorCode.INVALID_CREDENTIALS.explain
                    }.verify()
            }
        }

        Given("권한이 없는 사용자가 한명 있을때") {
            val givenUsers = TestUsersEntity.giveMeOne()
            val expectedUsers = Mono.zip(
                authenticationsRepository.save(AuthenticationsEntity.defaults()),
                configsRepository.save(ConfigsEntity.defaults())
            ).flatMap {
                usersRepository.save(
                    UsersEntity.defaults(
                        authenticationsId = it.t1.id,
                        configsId = it.t2.id,
                        email = givenUsers.email,
                        password = PasswordHelper.encode(givenUsers.password),
                        username = givenUsers.username,
                        description = givenUsers.description
                    )
                )
            }.block()!!

            Then("정상 로그인이 진행된다.") {
                usersService.signIn(
                    SignInDto(
                        givenUsers.email,
                        givenUsers.password,
                        TestDeviceDto.giveMeOne()
                    )
                )
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.shouldNotBeNull()
                        it.accessToken.shouldNotBeNull()
                        it.refreshToken.shouldNotBeNull()

                        val accessTokenCredentials = jsonWebTokenProvider.isDeniedAccessToken(it.accessToken)
                        accessTokenCredentials.shouldNotBeNull()
                        accessTokenCredentials.id shouldBe expectedUsers.id
                        accessTokenCredentials.authorities.shouldBeEmpty()

                        val refreshTokenCredentials = jsonWebTokenProvider.isDeniedRefreshToken(it.refreshToken)
                        refreshTokenCredentials.shouldNotBeNull()
                        refreshTokenCredentials.id shouldBe expectedUsers.id
                        refreshTokenCredentials.authorities.shouldBeEmpty()
                    }.expectComplete()
                    .verify()
            }
        }
    }
}
