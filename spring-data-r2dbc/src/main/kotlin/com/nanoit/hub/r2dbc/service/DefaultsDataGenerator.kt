package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.Role
import com.nanoit.hub.dto.SignUpDto
import com.nanoit.hub.r2dbc.entity.RolesEntity
import com.nanoit.hub.r2dbc.repository.RolesRepository
import com.nanoit.hub.utility.spring.JacksonHelper
import com.nanoit.hub.utility.spring.LocalProfileCondition
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Conditional
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultsDataGenerator(
    private val usersService: UsersService,
    private val rolesRepository: RolesRepository,
) {
    private val log = LoggerFactory.getLogger(DefaultsDataGenerator::class.java)

    @Conditional(LocalProfileCondition::class)
    @EventListener(ApplicationStartedEvent::class)
    fun started() {
        // ROLE UPDATE
        Role.values().forEach {
            rolesRepository.findByRole(it).block() ?: rolesRepository.save(RolesEntity.defaults(it)).block()
        }

        // admin user
        val signUpDto = usersService.signUp(
            SignUpDto(
                "admin@nanoit.kr",
                "Sksrhd!@34",
                "admin",
                "admin"
            )
        ).block()!!
        usersService.addPermission(signUpDto.id, Role.ADMIN).block()!!
        log.info(
            "LocalAndTestProfileCondition, create admin users: {}",
            JacksonHelper.writeValueAsStringWithPrettyPrint(signUpDto)
        )
    }
}
