package com.nanoit.hub.couchbase

import com.nanoit.hub.couchbase.service.UsersService
import com.nanoit.hub.dto.SignUpDto
import java.util.UUID
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Configuration
class TestRouter {
    @Bean
    fun usersRouteNoContent(testsHandler: TestsHandler): RouterFunction<ServerResponse> =
        RouterFunctions.route()
            .GET("/test") { testsHandler.getTests(it) }
            .GET("/test/{uuid}") { testsHandler.getTest(it) }
            .POST("/test") { testsHandler.postTest(it) }
            .build()
}

@Component
class TestsHandler(
    val usersService: UsersService
) {
    fun postTest(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono(SignUpDto::class.java).flatMap {
            usersService.signUp(it)
        }.flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    fun getTests(request: ServerRequest): Mono<ServerResponse> =
        usersService.getAll().flatMap {
            ServerResponse.ok().bodyValue(it)
        }

    fun getTest(request: ServerRequest): Mono<ServerResponse> =
        Mono.just(request.pathVariable("uuid")).flatMap {
            usersService.getOne(UUID.fromString(it))
        }.flatMap { ServerResponse.ok().bodyValue(it) }
}
