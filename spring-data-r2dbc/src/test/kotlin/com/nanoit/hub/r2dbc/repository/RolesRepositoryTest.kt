package com.nanoit.hub.r2dbc.repository

import com.nanoit.hub.dto.Role
import com.nanoit.hub.r2dbc.configuration.R2dbcConfiguration
import com.nanoit.hub.r2dbc.entity.RolesEntity
import com.nanoit.hub.test.container.PostgreSQLTestContainers
import io.kotest.core.spec.Spec
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

@DisplayName("INTEGRATION - RolesRepository")
@ActiveProfiles("test")
@Import(value = [R2dbcConfiguration::class])
@DataR2dbcTest
class RolesRepositoryTest(
    @Autowired
    val rolesRepository: RolesRepository,
    @Autowired
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : PostgreSQLTestContainers() {

    override suspend fun beforeSpec(spec: Spec) {
        refreshPostgreSql(r2dbcEntityTemplate)
    }

    init {
        Given("권한 종류가 주어져 있을때") {
            val roleCompanyAdmin = Role.TESTER

            Then("DB 에 정상 추가 된다.") {
                rolesRepository.save(RolesEntity.defaults(roleCompanyAdmin))
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.id.shouldNotBeNull()
                        it.role shouldBe roleCompanyAdmin
                        it.version shouldBe 1
                        it.createdDate.shouldNotBeNull()
                        it.lastModifiedDate.shouldNotBeNull()
                    }.expectComplete()
                    .verify()
            }
        }

        Given("중복된 권한이 주어졌을때") {
            val roleUser = Role.ADMIN

            Then("ROLE 중복 오류가 발생한다.") {
                rolesRepository.save(RolesEntity.defaults(roleUser))
                    .`as`(StepVerifier::create)
                    .expectErrorSatisfies {
                        it.shouldBeInstanceOf<DuplicateKeyException>()
                    }.verify()
            }
        }

        Given("기본 권한들이 있을때") {
            Then("권한 이름으로 권한 Entity 를 찾을 수 있다.") {
                rolesRepository.findByRole(Role.ADMIN)
                    .`as`(StepVerifier::create)
                    .expectSubscription()
                    .assertNext {
                        it.shouldNotBeNull()
                        it.id shouldBeGreaterThan 1
                        it.role shouldBe Role.ADMIN
                    }.expectComplete()
                    .verify()
            }
        }
    }
}
