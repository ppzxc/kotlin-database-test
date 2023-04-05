package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.ConfigsDto
import reactor.core.publisher.Mono

interface ConfigsService {
    fun getConfig(usersId: Long): Mono<ConfigsDto>
}
