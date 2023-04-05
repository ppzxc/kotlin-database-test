package com.nanoit.hub.scylla

import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

abstract class CombinedSpec : BehaviorSpec() {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            // REDIS
            registry.add("spring.application.name") { "test-node-1" }
            registry.add("scylla.keyspace-name") { "hub" }
            registry.add("scylla.local-datacenter") { "datacenter1" }
            registry.add("scylla.contacts-points") { ScyllaDbTestContainerEnvironments.scyllaDb.host }
            registry.add("scylla.port") { ScyllaDbTestContainerEnvironments.scyllaDb.firstMappedPort }
            registry.add("scylla.username") { "cassandra" }
            registry.add("scylla.password") { "cassandra" }

            // LOGGING
            registry.add("logging.level.root") { "debug" }
            registry.add("logging.level.org.springframework.data.cassandra.core.cql.CqlTemplate") { "debug" }
        }
    }
}
