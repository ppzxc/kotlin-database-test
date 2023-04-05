package com.nanoit.hub.scylla

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
object ScyllaDbTestContainerEnvironments : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringTestExtension(SpringTestLifecycleMode.Test))
    override val isolationMode = IsolationMode.InstancePerLeaf

    @Container
    @JvmStatic
    val scyllaDb = GenericContainer(DockerImageName.parse("scylladb/scylla:5.1.7"))
        .withClasspathResourceMapping("scylla.yaml", "/etc/scylla/scylla.yaml", BindMode.READ_WRITE)
        .withExposedPorts(9042)
        .withReuse(true)

    init {
        scyllaDb.start()
    }
}
