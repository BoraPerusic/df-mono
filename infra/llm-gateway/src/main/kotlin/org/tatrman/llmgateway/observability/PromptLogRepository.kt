package org.tatrman.llmgateway.observability

import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository

@Table("prompt_logs")
data class PromptLog(
        @Id val id: Long? = null,
        val userId: String?,
        val modelName: String?,
        val provider: String?,
        val promptText: String?,
        val responseText: String?,
        val tokensPrompt: Int? = 0,
        val tokensCompletion: Int? = 0,
        val durationMs: Long?,
        val status: String,
        val createdAt: Instant? = null
)

interface PromptLogRepository : CrudRepository<PromptLog, Long> {
    // Basic queries, specialized search will be Custom Implementation or @Query
}
