package org.tatrman.llmgateway.model

import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository

@Table("models")
data class Model(
        @Id val id: Int? = null,
        val name: String,
        val description: String? = null,
        val provider: String, // openai, ollama, etc.
        val modelType: String, // chat, embedding
        val tags: Array<String>? = emptyArray(),
        val config: String? = null, // JSON blob
        val createdAt: Instant? = null
)

interface ModelRepository : CrudRepository<Model, Int> {
    fun findByName(name: String): Model?
}
