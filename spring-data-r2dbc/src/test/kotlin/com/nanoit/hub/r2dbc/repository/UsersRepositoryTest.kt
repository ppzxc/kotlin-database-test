package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.configuration.R2dbcConfiguration
import com.nanoit.hub.r2dbc.entity.AuthenticationsEntity
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
import reactor.test.StepVerifier

@DisplayName("INTEGRATION - UsersRepository")
@ActiveProfiles("test")
@Import(value = [R2dbcConfiguration::class])
@DataR2dbcTest
class UsersRepositoryTest(
    @Autowired
    val usersRepository: UsersRepository,
    @Autowired
    val authenticationsRepository: AuthenticationsRepository,
    @Autowired
    val configsRepository: ConfigsRepository,
    @Autowired
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : PostgreSQLTestContainers() {

    override suspend fun beforeSpec(spec: Spec) {
        refreshPostgreSql(r2dbcEntityTemplate)
    }

    init {
        Given("사용자 한명을 저장할 때") {
            val authentications = authenticationsRepository.save(AuthenticationsEntity.defaults()).block()!!
            val configs = configsRepository.save(ConfigsEntity.defaults()).block()!!
            val expected = TestUsersEntity.giveMeOne(authenticationsId = authentications.id, configsId = configs.id)

            Then("정상 저장 된다") {
                usersRepository.save(expected)
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.shouldNotBeNull()
                        it.id.shouldBeGreaterThan(1)
                        it.authenticationsId shouldBe authentications.id
                        it.configsId shouldBe configs.id
                        it.email shouldBe expected.email
                        it.username shouldBe expected.username
                        it.password shouldBe expected.password
                        it.description shouldBe expected.description
                        it.version shouldBe 1
                        it.createdDate shouldBeGreaterThan expected.createdDate
                        it.lastModifiedDate shouldBeGreaterThan expected.lastModifiedDate
                    }
                    .expectComplete()
                    .verify()
            }
        }

        Given("사용자 이메일이 중복되면") {
            val firstAuthentications = authenticationsRepository.save(AuthenticationsEntity.defaults()).block()!!
            val firstConfigs = configsRepository.save(ConfigsEntity.defaults()).block()!!
            val firstUser =
                usersRepository.save(
                    TestUsersEntity.giveMeOne(
                        authenticationsId = firstAuthentications.id,
                        configsId = firstConfigs.id
                    )
                ).block()!!

            Then("에러가 발생한다.") {
                val secondAuthentications = authenticationsRepository.save(AuthenticationsEntity.defaults()).block()!!
                val secondConfigs = configsRepository.save(ConfigsEntity.defaults()).block()!!
                usersRepository.save(
                    TestUsersEntity.giveMeOne(
                        authenticationsId = secondAuthentications.id,
                        configsId = secondConfigs.id,
                        email = firstUser.email
                    )
                )
                    .`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<DuplicateKeyException>()
                    }
                    .verify()
            }
        }
    }
}
