package com.nanoit.hub.scylla.configuration

import com.nanoit.hub.scylla.properties.CustomScyllaProperties
import com.nanoit.hub.spring.utils.LocalAndTestProfileCondition
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.cassandra.config.AbstractReactiveCassandraConfiguration
import org.springframework.data.cassandra.config.CqlSessionFactoryBean
import org.springframework.data.cassandra.config.EnableReactiveCassandraAuditing
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories
import org.springframework.data.domain.ReactiveAuditorAware
import reactor.core.publisher.Mono


@Configuration
@EnableReactiveCassandraAuditing
@EnableReactiveCassandraRepositories(basePackageClasses = [ScyllaConfiguration::class])
class ScyllaConfiguration(
    val customScyllaProperties: CustomScyllaProperties,
    @Value("\${spring.application.name}") val applicationName: String,
) : AbstractReactiveCassandraConfiguration() {
    private val log = LoggerFactory.getLogger(ScyllaConfiguration::class.java)

    @PostConstruct
    fun postConstruct() {
        log.info(customScyllaProperties.contactsPoints)
    }

    @Bean
    fun reactiveAuditorAware(): ReactiveAuditorAware<String> = ReactiveAuditorAware { Mono.just(applicationName) }

    @Conditional(LocalAndTestProfileCondition::class)
    override fun getKeyspaceCreations(): MutableList<CreateKeyspaceSpecification> {
        return mutableListOf(
            CreateKeyspaceSpecification.createKeyspace(customScyllaProperties.keyspaceName)
                .ifNotExists()
                .withSimpleReplication()
        )
    }

    @Conditional(LocalAndTestProfileCondition::class)
    override fun keyspacePopulator(): KeyspacePopulator =
        ResourceKeyspacePopulator(ClassPathResource("schema.cql"))

    override fun getContactPoints(): String = customScyllaProperties.contactsPoints

    override fun getPort(): Int = customScyllaProperties.port

    override fun getLocalDataCenter(): String = customScyllaProperties.localDatacenter

    override fun getKeyspaceName(): String = customScyllaProperties.keyspaceName

//    override fun getSchemaAction(): SchemaAction = SchemaAction.valueOf(customScyllaProperties.schemaAction)

    override fun cassandraSession(): CqlSessionFactoryBean {
        val cqlSessionFactoryBean = super.cassandraSession()
        cqlSessionFactoryBean.setUsername(customScyllaProperties.username)
        cqlSessionFactoryBean.setPassword(customScyllaProperties.password)
        return cqlSessionFactoryBean
    }
}
