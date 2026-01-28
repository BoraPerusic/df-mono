package cz.dfpartner.erp_service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.openapi.OpenApiInfo
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
import org.jetbrains.exposed.sql.*

fun Application.configureMonitoring() {
    val openTelemetry = getOpenTelemetry(serviceName = "opentelemetry-ktor-sample-server")
    
    install(KtorServerTelemetry) {
        setOpenTelemetry(openTelemetry)
    
        capturedRequestHeaders(HttpHeaders.UserAgent)
    
        spanKindExtractor {
            if (httpMethod == HttpMethod.Post) {
                SpanKind.PRODUCER
            } else {
                SpanKind.CLIENT
            }
        }
    
        attributesExtractor {
            onStart {
                attributes.put("start-time", System.currentTimeMillis())
            }
            onEnd {
                attributes.put("end-time", System.currentTimeMillis())
            }
        }
    }
    routing {
        get("/hello") {
            call.respondText("Hello World!")
        }
        
        post("/post") {
            val postData = call.receiveText()
            call.respondText("Received: $postData")
        }
    }
}
