package org.tatrman.llmgateway.observability

import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.TimeUnit
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ObservabilityService(
        private val promptLogRepository: PromptLogRepository,
        private val meterRegistry: MeterRegistry
) {

    // Record Log & Metrics asynchronously to avoid blocking response
    // (Though for metrics, usually you want synchronous recording, logs can be async)
    @Async
    fun recordInteraction(logEntry: PromptLog) {
        // 1. Save Log
        promptLogRepository.save(logEntry)

        // 2. Metrics
        meterRegistry
                .counter(
                        "llm.requests.count",
                        "model",
                        logEntry.modelName ?: "unknown",
                        "provider",
                        logEntry.provider ?: "unknown",
                        "status",
                        logEntry.status
                )
                .increment()

        if (logEntry.durationMs != null) {
            meterRegistry
                    .timer("llm.requests.latency", "model", logEntry.modelName ?: "unknown")
                    .record(logEntry.durationMs, TimeUnit.MILLISECONDS)
        }

        if (logEntry.tokensPrompt != null) {
            meterRegistry
                    .summary("llm.tokens.prompt", "model", logEntry.modelName ?: "unknown")
                    .record(logEntry.tokensPrompt.toDouble())
        }
        if (logEntry.tokensCompletion != null) {
            meterRegistry
                    .summary("llm.tokens.completion", "model", logEntry.modelName ?: "unknown")
                    .record(logEntry.tokensCompletion.toDouble())
        }
    }
}
