package com.nanoit.hub.r2dbc.configuration

import com.nanoit.hub.utility.spring.LocalAndTestProfileCondition
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@EnableR2dbcAuditing
class R2dbcConfiguration {

    @Conditional(LocalAndTestProfileCondition::class)
    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        initializer.setDatabasePopulator(
            CompositeDatabasePopulator(
                ResourceDatabasePopulator(ClassPathResource("schema/1_before.sql")),
                ResourceDatabasePopulator(ClassPathResource("schema/2_function.sql")),
                ResourceDatabasePopulator(ClassPathResource("schema/3_schema.sql")),
                ResourceDatabasePopulator(ClassPathResource("schema/4_after.sql")),
                ResourceDatabasePopulator(ClassPathResource("schema/5_data.sql"))
            )
        )
        return initializer
    }
}
