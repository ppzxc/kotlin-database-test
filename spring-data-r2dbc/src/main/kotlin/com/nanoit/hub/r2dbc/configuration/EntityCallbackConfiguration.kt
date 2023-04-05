package com.nanoit.hub.r2dbc.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class EntityCallbackConfiguration {
    private val log = LoggerFactory.getLogger(com.nanoit.hub.r2dbc.configuration.EntityCallbackConfiguration::class.java)

//    @Bean
//    fun onBeforeSave(): BeforeSaveCallback<AuditEntity> = BeforeSaveCallback { entity, _, _ ->
//        log.debug("onBeforeSave $entity")
//        Mono.just(entity)
//    }
//
//    @Bean
//    fun onAfterSave(): AfterSaveCallback<AuditEntity> = AfterSaveCallback { entity, _, _ ->
//        log.debug("onAfterSave $entity")
//        Mono.just(entity)
//    }
//
//    @Bean
//    fun onBeforeConvert(): BeforeConvertCallback<AuditEntity> = BeforeConvertCallback { entity, _ ->
//        entity.onBeforeSave()
//        log.debug("onBeforeConvert $entity")
//        Mono.just(entity)
//    }
//
//    @Bean
//    fun onAfterConvert(): AfterConvertCallback<AuditEntity> = AfterConvertCallback { entity, _ ->
//        log.debug("onAfterConvert $entity")
//        Mono.just(entity)
//    }
}
