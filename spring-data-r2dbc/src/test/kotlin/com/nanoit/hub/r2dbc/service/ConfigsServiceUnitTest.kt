package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.exceptions.NotFoundUserException
import com.nanoit.hub.r2dbc.entity.ConfigsEntityMapper
import com.nanoit.hub.r2dbc.entity.ConfigsEntityMapperImpl
import com.nanoit.hub.r2dbc.repository.ConfigsRepository
import com.nanoit.hub.r2dbc.repository.UsersRepository
import com.nanoit.hub.test.RandomUtils
import com.nanoit.hub.test.TestUtils.shouldBeByMillis
import com.nanoit.hub.test.entity.TestConfigsEntity
import com.nanoit.hub.test.entity.TestUsersEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

val usersRepository: UsersRepository = mockk()
val configsRepository: ConfigsRepository = mockk()
val configsEntityMapper: ConfigsEntityMapper = ConfigsEntityMapperImpl()

@InjectMockKs
val configsService: ConfigsService = ConfigsServiceImpl(usersRepository, configsRepository, configsEntityMapper)

@DisplayName("UNIT - ConfigsService")
class ConfigsServiceUnitTest : BehaviorSpec({

    Given("사용자가 없는 상황에서") {
        val usersId = RandomUtils.long()
        every { usersRepository.findById(usersId) } returns Mono.empty()

        When("설정 정보를 요청하면") {
            val expected = configsService.getConfig(usersId)

            Then("NotFoundUserException 이 발생한다.") {
                StepVerifier.create(expected).verifyError(NotFoundUserException::class.java)
            }
        }
    }

    Given("사용자가 한명 있는 상황에서") {
        val expectedUsers = TestUsersEntity.giveMeOne(id = RandomUtils.long())
        val expectedConfigs = TestConfigsEntity.giveMeOne(expectedUsers.configsId, RandomUtils.string())
        every { usersRepository.findById(expectedUsers.id) } returns Mono.just(expectedUsers)
        every { usersRepository.findById(not(eq(expectedUsers.id))) } returns Mono.empty()
        every { configsRepository.findById(expectedUsers.configsId) } returns Mono.just(expectedConfigs)
        every { configsRepository.findById(not(eq(expectedUsers.configsId))) } returns Mono.empty()

        When("없는 사용자의 설정 정보를 요청하면") {
            val expected = configsService.getConfig(RandomUtils.long())

            Then("NotFoundUserException 이 발생한다.") {
                StepVerifier.create(expected).verifyError(NotFoundUserException::class.java)
            }
        }

        When("존재하는 사용자의 설정 정보를 요청하면") {
            val expected = configsService.getConfig(expectedUsers.id)

            Then("ConfigsDto 가 반환된다.") {
                StepVerifier.create(expected)
                    .expectSubscription()
                    .assertNext {
                        it.id shouldBe expectedConfigs.id
                        it.client shouldBe expectedConfigs.client
                        it.version shouldBe expectedConfigs.version
                        it.createdDate.shouldBeByMillis(expectedConfigs.createdDate).shouldBeTrue()
                        it.lastModifiedDate.shouldBeByMillis(expectedConfigs.lastModifiedDate).shouldBeTrue()
                    }.expectComplete()
                    .verify()
            }
        }
    }
})
