//package com.nanoit.hub.scylla
//
//import com.github.f4b6a3.uuid.codec.UuidCodec
//import com.github.f4b6a3.uuid.codec.base.Base16Codec
//import com.nanoit.hub.dto.PutUsersDto
//import com.nanoit.hub.dto.SignUpDto
//import com.nanoit.hub.scylla.entity.UsersType
//import com.nanoit.hub.scylla.service.UsersService
//import com.nanoit.hub.scylla.service.UsersServiceImpl
//import java.time.LocalDate
//import org.slf4j.LoggerFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.data.domain.PageRequest
//import org.springframework.data.domain.Sort
//import org.springframework.stereotype.Component
//import org.springframework.web.reactive.function.server.RouterFunction
//import org.springframework.web.reactive.function.server.RouterFunctions
//import org.springframework.web.reactive.function.server.ServerRequest
//import org.springframework.web.reactive.function.server.ServerResponse
//import reactor.core.publisher.Mono
//
//
//@Configuration
//class TestRouter {
//    @Bean
//    fun usersRouteNoContent(testsHandler: TestsHandler): RouterFunction<ServerResponse> =
//        RouterFunctions.route()
//            .GET("/users") { testsHandler.getTests(it) }
//            .GET("/users/{uuid}") { testsHandler.getTest(it) }
//            .PUT("/users/{uuid}") { testsHandler.putUsers(it) }
//            .POST("/users") { testsHandler.postTest(it) }
//            .build()
//}
//
//@Component
//class TestsHandler(
//    val usersService: UsersService
//) {
//    private val log = LoggerFactory.getLogger(TestsHandler::class.java)
//    var codec: UuidCodec<String> = Base16Codec()
//
//    fun postTest(request: ServerRequest): Mono<ServerResponse> =
//        request.bodyToMono(SignUpDto::class.java).flatMap {
//            usersService.signUp(it)
//        }.flatMap {
//            ServerResponse.ok().bodyValue(it)
//        }
//
//    fun getTests(request: ServerRequest): Mono<ServerResponse> =
//        Mono.zip(
//            Mono.just(request.queryParam("page").orElse("0").toInt()),
//            Mono.just(request.queryParam("per_page").orElse("10").toInt()),
//            Mono.just(request.queryParam("sort").orElse("id")),
//            Mono.just(request.queryParam("date").orElse(LocalDate.now().toString())),
//        ).flatMap {
//            usersService.getAll(
//                LocalDate.parse(it.t4),
//                PageRequest.of(it.t1, it.t2, Sort.by(Sort.Direction.values().random(), it.t3))
//            )
//        }.flatMap {
//            log.info("########## ROUTER $it")
//            ServerResponse.ok().bodyValue(it)
//        }
//
//    fun putUsers(request: ServerRequest): Mono<ServerResponse> =
//        request.bodyToMono(PutUsersDto::class.java).flatMap {
//            usersService.putOne(codec.decode(request.pathVariable("uuid")), it)
//        }.then(ServerResponse.noContent().build())
//
//    fun getTest(request: ServerRequest): Mono<ServerResponse> =
//        Mono.just(request.pathVariable("uuid")).flatMap {
//            usersService.getOne(codec.decode(it))
//        }.flatMap { ServerResponse.ok().bodyValue(it) }
//}
