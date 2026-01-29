package org.tatrman.llmgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import io.grpc.Status
import org.springframework.boot.runApplication
import org.springframework.grpc.server.exception.GrpcExceptionHandler

@SpringBootApplication @EnableAsync class LlmGatewayApplication {
    @Bean
    open fun globalInterceptor(): GrpcExceptionHandler = GrpcExceptionHandler { exception ->
        when (exception) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(exception.message).asException();
            else -> null
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LlmGatewayApplication>(*args)
}
