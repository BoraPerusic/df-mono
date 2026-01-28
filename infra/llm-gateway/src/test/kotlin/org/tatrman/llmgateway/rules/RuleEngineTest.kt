package org.tatrman.llmgateway.rules

import com.typesafe.config.ConfigFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.tatrman.llmgateway.model.Model

class RuleEngineTest :
        StringSpec({
            val configString =
                    """
        rules {
            aliases {
                "gpt-4-alias" = "gpt-4-turbo"
                "fast-model" = "gpt-3.5-turbo"
            }
        }
    """.trimIndent()

            val config = ConfigFactory.parseString(configString)
            val ruleEngine = RuleEngine(config)

            val availableModels =
                    listOf(
                            Model(
                                    id = 1,
                                    name = "gpt-4-turbo",
                                    provider = "openai",
                                    modelType = "chat",
                                    tags = arrayOf("smart"),
                                    config = "{}"
                            ),
                            Model(
                                    id = 2,
                                    name = "gpt-3.5-turbo",
                                    provider = "openai",
                                    modelType = "chat",
                                    tags = arrayOf("fast"),
                                    config = "{}"
                            ),
                            Model(
                                    id = 3,
                                    name = "claude-3-opus",
                                    provider = "anthropic",
                                    modelType = "chat",
                                    tags = arrayOf("smart"),
                                    config = "{}"
                            )
                    )

            "selectModel should return exact match if exists" {
                val metadata = RequestMetadata(modelName = "gpt-4-turbo")
                val result = ruleEngine.selectModel(metadata, availableModels)

                result shouldNotBe null
                result?.name shouldBe "gpt-4-turbo"
            }

            "selectModel should apply alias mapping" {
                val metadata = RequestMetadata(modelName = "gpt-4-alias")
                val result = ruleEngine.selectModel(metadata, availableModels)

                result shouldNotBe null
                result?.name shouldBe "gpt-4-turbo"
            }

            "selectModel should apply alias mapping for fast-model" {
                val metadata = RequestMetadata(modelName = "fast-model")
                val result = ruleEngine.selectModel(metadata, availableModels)

                result shouldNotBe null
                result?.name shouldBe "gpt-3.5-turbo"
            }

            "selectModel should return null if model not found" {
                val metadata = RequestMetadata(modelName = "non-existent-model")
                val result = ruleEngine.selectModel(metadata, availableModels)

                result shouldBe null
            }
        })
