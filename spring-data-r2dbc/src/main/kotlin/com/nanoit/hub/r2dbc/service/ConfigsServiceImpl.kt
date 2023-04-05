package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.ConfigsDto
import com.nanoit.hub.exceptions.NotFoundUserException
import com.nanoit.hub.r2dbc.entity.ConfigsEntity
import com.nanoit.hub.r2dbc.entity.ConfigsEntityMapper
import com.nanoit.hub.r2dbc.entity.UsersEntity
import com.nanoit.hub.r2dbc.repository.ConfigsRepository
import com.nanoit.hub.r2dbc.repository.UsersRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class ConfigsServiceImpl(
    val usersRepository: UsersRepository,
    val configsRepository: ConfigsRepository,
    val configsEntityMapper: ConfigsEntityMapper,
) : ConfigsService {

    override fun getConfig(usersId: Long): Mono<ConfigsDto> = findUser(usersId)
        .flatMap { findConfig(it.configsId) }
        .flatMap { Mono.just(configsEntityMapper.toDto(it)) }

    private fun findConfig(configsId: Long): Mono<ConfigsEntity> = configsRepository.findById(configsId)
        .switchIfEmpty(Mono.error(NotFoundUserException("configsId=$configsId")))

    private fun findUser(usersId: Long): Mono<UsersEntity> = usersRepository.findById(usersId)
        .switchIfEmpty(Mono.error(NotFoundUserException("usersId=$usersId")))
}
