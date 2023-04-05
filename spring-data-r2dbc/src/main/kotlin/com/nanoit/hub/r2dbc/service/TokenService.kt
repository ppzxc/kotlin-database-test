package com.nanoit.hub.r2dbc.service

import com.nanoit.hub.dto.AccessAndRefreshTokenDto
import com.nanoit.hub.dto.AccessTokenDto
import com.nanoit.hub.dto.DeviceDto
import com.nanoit.hub.dto.MessengerPrincipal
import com.nanoit.hub.dto.RefreshTokenDto
import reactor.core.publisher.Mono

interface TokenService {
    fun issueSignInToken(messengerPrincipal: MessengerPrincipal, deviceDto: DeviceDto): Mono<AccessAndRefreshTokenDto>
    fun refreshAccessToken(refreshTokenDto: RefreshTokenDto): Mono<AccessTokenDto>
}
