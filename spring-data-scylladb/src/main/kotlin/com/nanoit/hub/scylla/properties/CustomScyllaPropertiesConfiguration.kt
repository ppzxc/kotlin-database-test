package com.nanoit.hub.scylla.properties

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CustomScyllaProperties::class)
class CustomScyllaPropertiesConfiguration
