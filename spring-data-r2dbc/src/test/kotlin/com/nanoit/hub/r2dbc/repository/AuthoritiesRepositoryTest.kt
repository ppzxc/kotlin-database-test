package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.dto.Role
import com.nanoit.hub.r2dbc.configuration.R2dbcConfiguration
import com.nanoit.hub.r2dbc.entity.AuthenticationsEntity
import com.nanoit.hub.r2dbc.entity.AuthoritiesEntity
import com.nanoit.hub.r2dbc.entity.ConfigsEntity
import com.nanoit.hub.test.container.PostgreSQLTestContainers
import com.nanoit.hub.test.entity.TestUsersEntity
import io.kotest.core.spec.Spec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@DisplayName("INTEGRATION - AuthoritiesRepository")
@ActiveProfiles("test")
@Import(value = [R2dbcConfiguration::class])
@DataR2dbcTest
class AuthoritiesRepositoryTest(
    @Autowired
    val usersRepository: UsersRepository,
    @Autowired
    val authenticationsRepository: AuthenticationsRepository,
    @Autowired
    val authoritiesRepository: AuthoritiesRepository,
    @Autowired
    val configsRepository: ConfigsRepository,
    @Autowired
    val rolesRepository: RolesRepository,
    @Autowired
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : PostgreSQLTestContainers() {

    override suspend fun beforeSpec(spec: Spec) {
        refreshPostgreSql(r2dbcEntityTemplate)
    }

    init {
        Given("사용자 한명이 주어져 있을때") {
            val expectedUser = Mono.zip(
                authenticationsRepository.save(AuthenticationsEntity.defaults()),
                configsRepository.save(ConfigsEntity.defaults()),
            ).flatMap {
                usersRepository.save(TestUsersEntity.giveMeOne(authenticationsId = it.t1.id, configsId = it.t2.id))
            }.block()!!

            Then("기본 권한 부여가 가능하다.") {
                val authorities = rolesRepository.findAll().collectList().block()!!
                val roleUserEntity = authorities.find { it.role == Role.USER }!!
                authoritiesRepository.save(
                    AuthoritiesEntity.defaults(
                        expectedUser.id,
                        roleUserEntity.id
                    )
                ).`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.shouldNotBeNull()
                        it.id.shouldBeGreaterThan(1)
                        it.usersId shouldBe expectedUser.id
                        it.rolesId shouldBe roleUserEntity.id
                        it.version shouldBe 1
                        it.createdDate shouldBeGreaterThan expectedUser.createdDate
                        it.lastModifiedDate shouldBeGreaterThan expectedUser.lastModifiedDate
                    }
                    .expectComplete()
                    .verify()
            }

            Then("중복으로 권한이 부여될때 에러가 발생한다.") {
                val authorities = rolesRepository.findAll().collectList().block()!!
                val roleUserEntity = authorities.find { it.role == Role.USER }!!
                authoritiesRepository.save(
                    AuthoritiesEntity.defaults(
                        expectedUser.id,
                        roleUserEntity.id
                    )
                ).block()!!

                authoritiesRepository.save(
                    AuthoritiesEntity.defaults(
                        expectedUser.id,
                        roleUserEntity.id
                    )
                ).`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<DuplicateKeyException>()
                    }
                    .verify()
            }
        }
    }
}
