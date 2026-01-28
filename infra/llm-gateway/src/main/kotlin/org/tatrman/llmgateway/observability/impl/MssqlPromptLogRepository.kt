package org.tatrman.llmgateway.observability.impl

import org.tatrman.llmgateway.observability.PromptLog
import org.tatrman.llmgateway.observability.PromptLogRepository
import java.util.Optional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(name = ["llm.storage.type"], havingValue = "mssql")
class MssqlPromptLogRepository(private val jdbcTemplate: JdbcTemplate) : PromptLogRepository {

    // Note: This implements the interface manually because Spring Data JDBC
    // autoconfiguration usually picks one dialect.
    // To support multi-db switching at runtime for the same Repository interface,
    // typically we'd use profiles or separate configurations.
    // Here we assume if 'mssql' is enabled, the primary DataSource is MSSQL.

    override fun <S : PromptLog> save(entity: S): S {
        val sql =
                """
            INSERT INTO prompt_logs (user_id, model_name, provider, prompt_text, response_text, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()
        // Simplified insert (missing fields for brevity in this example)
        jdbcTemplate.update(
                sql,
                entity.userId,
                entity.modelName,
                entity.provider,
                entity.promptText,
                entity.responseText,
                entity.status
        )
        return entity
    }

    override fun <S : PromptLog?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Optional<PromptLog> {
        TODO("Not yet implemented")
    }

    override fun existsById(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableIterable<PromptLog> {
        TODO("Not yet implemented")
    }

    override fun findAllById(ids: MutableIterable<Long>): MutableIterable<PromptLog> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        return 0
    }

    override fun deleteById(id: Long) {}

    override fun delete(entity: PromptLog) {}

    override fun deleteAllById(ids: MutableIterable<Long>) {}

    override fun deleteAll(entities: MutableIterable<PromptLog>) {}

    override fun deleteAll() {}
}
