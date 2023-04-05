package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.AccessAndRefreshTokenDto
import com.nanoit.hub.dto.AccessTokenDto
import com.nanoit.hub.dto.DeviceDto
import com.nanoit.hub.dto.MessengerPrincipal
import com.nanoit.hub.dto.RefreshTokenDto
import com.nanoit.hub.exceptions.FailedCreateTokenException
import com.nanoit.hub.exceptions.MessengerErrorCodeException
import com.nanoit.hub.r2dbc.entity.DevicesEntity
import com.nanoit.hub.r2dbc.repository.AuthoritiesRepository
import com.nanoit.hub.r2dbc.repository.DevicesRepository
import com.nanoit.hub.r2dbc.repository.UsersRepository
import com.nanoit.hub.security.jwt.JsonWebTokenProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TokenServiceImpl(
    val jsonWebTokenProvider: JsonWebTokenProvider,
    val usersRepository: UsersRepository,
    val authoritiesRepository: AuthoritiesRepository,
    val devicesRepository: DevicesRepository,
) : TokenService {
    private val log = LoggerFactory.getLogger(TokenServiceImpl::class.java)

    override fun issueSignInToken(
        messengerPrincipal: MessengerPrincipal,
        deviceDto: DeviceDto
    ): Mono<AccessAndRefreshTokenDto> {
        return devicesRepository.findByUsersIdAndGuid(messengerPrincipal.id, deviceDto.guid)
            .switchIfEmpty(devicesRepository.save(DevicesEntity.defaults(messengerPrincipal.id, deviceDto)))
            .flatMap { Mono.just(jsonWebTokenProvider.generate(messengerPrincipal, deviceDto.guid)) }
            .onErrorMap {
                log.error("[#TOEKN_SERVICE#] failed create token", it)
                when (it) {
                    is MessengerErrorCodeException -> it
                    else -> FailedCreateTokenException()
                }
            }
    }

    override fun refreshAccessToken(refreshTokenDto: RefreshTokenDto): Mono<AccessTokenDto> {
        return Mono.just(jsonWebTokenProvider.refreshAccessToken(refreshTokenDto.refreshToken))
    }
}
