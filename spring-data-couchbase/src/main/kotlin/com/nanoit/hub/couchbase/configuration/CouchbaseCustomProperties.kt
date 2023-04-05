package com.nanoit.hub.couchbase.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "couchbase")
data class CouchbaseCustomProperties(
    var connectionString: String,
    var username: String,
    var password: String,
    var bucketName: String,
)
