package org.tatrman.llmgateway.rules

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import org.springframework.stereotype.Service
import org.tatrman.llmgateway.model.Model

data class RequestMetadata(
        val modelName: String? = null,
        val requiredTags: List<String> = emptyList(),
        val user: String? = null
)

@Service
class RuleEngine(private var rulesConfig: Config = ConfigFactory.load("rules.conf")) {

    fun reloadRules(path: String) {
        val file = File(path)
        if (file.exists()) {
            rulesConfig = ConfigFactory.parseFile(file).resolve()
        }
    }

    /**
     * strict: If true, ONLY return models matching all criteria. If false, return best guess (not
     * implemented yet, just first match).
     */
    fun selectModel(metadata: RequestMetadata, availableModels: Iterable<Model>): Model? {
        var targetName = metadata.modelName

        // 1. Alias Resolution
        if (targetName != null && rulesConfig.hasPath("rules.aliases")) {
            val aliases = rulesConfig.getConfig("rules.aliases")
            val quotedName = "\"$targetName\"" // Config requires quoting for keys with dots/dashes
            if (aliases.hasPath(quotedName)) {
                targetName = aliases.getString(quotedName)
            }
        }

        // 2. Direct Name Match (with resolved alias)
        if (targetName != null) {
            val direct = availableModels.find { it.name.equals(targetName, ignoreCase = true) }
            if (direct != null) return direct

            // If model name was explicitly requested but not found (and not filtered by tags yet),
            // and we treat name request as strict:
            return null
        }

        // 3. Tag Matching (fallback for generic requests)
        var candidates = availableModels.toList()
        if (metadata.requiredTags.isNotEmpty()) {
            candidates =
                    candidates.filter { model ->
                        metadata.requiredTags.all { reqTag -> model.tags?.contains(reqTag) == true }
                    }
        }

        return candidates.firstOrNull()
    }
}
