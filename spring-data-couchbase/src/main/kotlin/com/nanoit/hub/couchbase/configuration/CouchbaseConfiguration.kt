package com.nanoit.hub.couchbase.configuration

import com.couchbase.client.core.msg.kv.DurabilityLevel
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.Cluster
import com.couchbase.client.java.manager.bucket.BucketSettings
import com.couchbase.client.java.manager.bucket.BucketType
import com.couchbase.client.java.manager.collection.CollectionSpec
import com.nanoit.hub.spring.utils.LocalAndTestProfileCondition
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration
import org.springframework.data.couchbase.repository.auditing.EnableReactiveCouchbaseAuditing
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories
import org.springframework.data.domain.ReactiveAuditorAware
import reactor.core.publisher.Mono

@Configuration
@EnableReactiveCouchbaseAuditing
@EnableReactiveCouchbaseRepositories("com.nanoit.hub.couchbase")
class CouchbaseConfiguration(
    @Value("\${spring.application.name}") val applicationName: String,
    val couchbaseCustomProperties: CouchbaseCustomProperties,
) : AbstractCouchbaseConfiguration() {
    override fun getConnectionString(): String = couchbaseCustomProperties.connectionString

    override fun getUserName(): String = couchbaseCustomProperties.username

    override fun getPassword(): String = couchbaseCustomProperties.password

    override fun getBucketName(): String = couchbaseCustomProperties.bucketName

    @Bean
    fun reactiveAuditorAware(): ReactiveAuditorAware<String> = ReactiveAuditorAware { Mono.just(applicationName) }

    // INIT SCRIPT
    @Conditional(LocalAndTestProfileCondition::class)
    @Bean
    fun getCouchbaseBucket(cluster: Cluster): Bucket {
        if (cluster.buckets().allBuckets.containsKey(bucketName)) {
            cluster.buckets().dropBucket(bucketName)
        }

        cluster.buckets().createBucket(
            BucketSettings.create(bucketName)
                .bucketType(BucketType.COUCHBASE)
                .ramQuotaMB(4096)
//                .replicaIndexes(true)
//                .numReplicas(3)
                .minimumDurabilityLevel(DurabilityLevel.NONE)
                .flushEnabled(true)
        )

        // BUCKET
        val bucket = cluster.bucket(bucketName)


        // SCOPE: MEMBERS
        bucket.collections().createScope(CouchbaseNaming.SCOPE_MEMBERS)

        // COLLECTION: MEMBERS.USERS
        bucket.collections()
            .createCollection(CollectionSpec.create(CouchbaseNaming.COLLECTION_USERS, CouchbaseNaming.SCOPE_MEMBERS))
//        bucket.scope(CouchbaseNaming.SCOPE_MEMBERS)
//            .collection(CouchbaseNaming.COLLECTION_USERS)
//            .queryIndexes()
//            .createPrimaryIndex(
//                CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().indexName("pk_members_users")
//            )

//        // SCOPE: MESSAGING
        bucket.collections().createScope(CouchbaseNaming.SCOPE_MESSAGING)
//
//        // SCOPE: CONTACTS
        bucket.collections().createScope(CouchbaseNaming.SCOPE_CONTACTS)
//
//        // SCOPE: CALL_LOG
        bucket.collections().createScope(CouchbaseNaming.SCOPE_CALL_LOG)
        return cluster.bucket(bucketName)
    }
}
