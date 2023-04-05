package com.nanoit.hub.scylla.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scylla")
data class CustomScyllaProperties(
    val keyspaceName: String,
    val localDatacenter: String,
    val contactsPoints: String,
    val port: Int,
    val username: String,
    val password: String,
)
