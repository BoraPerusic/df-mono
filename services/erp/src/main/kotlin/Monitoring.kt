package cz.dfpartner.erp_service

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry

fun Application.configureMonitoring() {
    val openTelemetry = io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk
    
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
