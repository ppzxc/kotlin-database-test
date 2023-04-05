package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.r2dbc.configuration.R2dbcConfiguration
import com.nanoit.hub.r2dbc.entity.AuthenticationsEntity
import com.nanoit.hub.test.container.PostgreSQLTestContainers
import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier

@DisplayName("INTEGRATION - AuthenticationsRepository")
@ActiveProfiles("test")
@Import(value = [R2dbcConfiguration::class])
@DataR2dbcTest
class AuthenticationsRepositoryTest(
    @Autowired
    val authenticationsRepository: AuthenticationsRepository,
    @Autowired
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : PostgreSQLTestContainers() {

    override suspend fun beforeSpec(spec: Spec) {
        refreshPostgreSql(r2dbcEntityTemplate)
    }

    init {
        Given("기본 인증 객체가 주어졌을때") {
            val defaults = AuthenticationsEntity.defaults()

            Then("DB 에 정상 추가 된다.") {
                authenticationsRepository.save(defaults)
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.id.shouldNotBeNull()
                        it.accountEmailVerified shouldBe defaults.accountEmailVerified
                        it.accountNonExpired shouldBe defaults.accountNonExpired
                        it.accountNonLocked shouldBe defaults.accountNonLocked
                        it.credentialsNonExpired shouldBe defaults.credentialsNonExpired
                        it.enabled shouldBe defaults.enabled
                        it.version shouldBe 1
                        it.createdDate shouldBeAfter  defaults.createdDate
                        it.lastModifiedDate shouldBeAfter defaults.lastModifiedDate
                    }.expectComplete()
                    .verify()
            }
        }
    }
}
