package com.nanoit.hub.scylla.service

import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.exceptions.DuplicateEmailException
import com.nanoit.hub.scylla.CombinedSpec
import com.nanoit.hub.scylla.configuration.ScyllaConfiguration
import com.nanoit.hub.scylla.entity.UsersEntityMapperImpl
import com.nanoit.hub.scylla.properties.CustomScyllaPropertiesConfiguration
import com.nanoit.hub.test.fixture.RandomUtils
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier

@Import(
    value = [
        ScyllaConfiguration::class, CustomScyllaPropertiesConfiguration::class,
        UsersServiceImpl::class, UsersEntityMapperImpl::class,
    ]
)
@DataCassandraTest
class UsersServiceImplTest(
    val usersService: UsersService,
) : CombinedSpec() {

    init {
        Given("회원 1명이 이미 가입되어 있고") {
            val givenSignUp =
                SignUpDto(RandomUtils.email(), RandomUtils.password(), RandomUtils.string(), RandomUtils.string())
            val givenUsers = usersService.signUp(givenSignUp).block()

            When("중복 email 로 회원 가입 요청시") {
                val expectedSignUp = givenSignUp.copy()

                Then("G1 W1 T1") {
                    usersService.signUp(expectedSignUp)
                        .`as`(StepVerifier::create)
                        .verifyErrorSatisfies {
                            it.shouldBeInstanceOf<DuplicateEmailException>()
                        }
                }
            }
        }
    }
}
