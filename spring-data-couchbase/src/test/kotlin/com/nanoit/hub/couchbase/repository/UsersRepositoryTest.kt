package com.nanoit.hub.couchbase.repository

import com.nanoit.hub.couchbase.configuration.CouchbaseCustomPropertiesConfiguration
import com.nanoit.hub.couchbase.configuration.CouchbaseConfiguration
import com.nanoit.hub.couchbase.entity.UsersEntity
import com.nanoit.hub.test.fixture.RandomUtils
import com.nanoit.hub.test.couchbase.CouchBaseTestContainersEnvironments
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(
    classes = [
        CouchbaseConfiguration::class,
        CouchbaseCustomPropertiesConfiguration::class,
    ]
)
class UsersRepositoryTest(
    @Autowired val usersRepository: UsersRepository,
) : CouchBaseTestContainersEnvironments() {

    init {
        Given("G1") {
            val givenUsers = UsersEntity.defaults(RandomUtils.email(), RandomUtils.password(), RandomUtils.string())
            val expectedUsers = usersRepository.save(givenUsers).block()!!

            Then("T1") {
                expectedUsers.shouldNotBeNull()
            }
        }
    }
}
