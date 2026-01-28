package org.tatrman.llmgateway.async

import io.nats.client.Connection
import org.springframework.stereotype.Component

@Component
class NatsPublisher(private val natsConnection: Connection) {

    fun publishJobCompleted(job: Job) {
        val subject = "jobs.completed"
        // Simple serialization
        val payload = """{"jobId": "${job.id}", "status": "${job.status}"}"""
        natsConnection.publish(subject, payload.toByteArray())
    }
}
