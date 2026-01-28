package org.tatrman.llmgateway.web

import kotlinx.serialization.Serializable
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.tatrman.llmgateway.model.ModelService
import org.tatrman.llmgateway.observability.ObservabilityService
import org.tatrman.llmgateway.observability.PromptLog
import org.tatrman.llmgateway.rules.RequestMetadata
import org.tatrman.llmgateway.rules.RuleEngine

// Simple DTOs to mirror OpenAI structure roughly
@Serializable
data class ChatCompletionRequest(val model: String? = null, val messages: List<ChatMessageDto>)

@Serializable data class ChatMessageDto(val role: String, val content: String)

@Serializable data class ChatCompletionResponse(val content: String)

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
        private val ruleEngine: RuleEngine,
        private val modelService: ModelService,
        chatClientBuilder: ChatClient.Builder,
        private val observabilityService: ObservabilityService
) {
    private val defaultChatClient: ChatClient = chatClientBuilder.build()

    @PostMapping("/completions")
    fun chat(@RequestBody request: ChatCompletionRequest): ChatCompletionResponse {
        val start = System.currentTimeMillis()

        val allModels = modelService.findAll()
        val metadata = RequestMetadata(modelName = request.model)
        val selectedModel = ruleEngine.selectModel(metadata, allModels)

        println("Selected Model: ${selectedModel?.name} (Provider: ${selectedModel?.provider})")

        val messages = request.messages.map { toAiMessage(it) }
        val prompt = Prompt(messages)

        var responseContent = ""
        var status = "SUCCESS"
        val promptTokens = 0
        var completionTokens = 0

        try {
            // Fluent API usage
            responseContent = defaultChatClient.prompt(prompt).call().content() ?: ""
            return ChatCompletionResponse(responseContent)
        } catch (e: Exception) {
            status = "ERROR"
            responseContent = e.message ?: "Unknown Error"
            throw e
        } finally {
            val duration = System.currentTimeMillis() - start
            observabilityService.recordInteraction(
                    PromptLog(
                            userId = "user-placeholder", // Extract from SecurityContext
                            modelName = selectedModel?.name ?: request.model,
                            provider = selectedModel?.provider ?: "unknown",
                            promptText = request.messages.lastOrNull()?.content ?: "",
                            responseText = responseContent,
                            tokensPrompt = promptTokens,
                            tokensCompletion = completionTokens,
                            durationMs = duration,
                            status = status
                    )
            )
        }
    }

    private fun toAiMessage(dto: ChatMessageDto): Message {
        return when (dto.role.lowercase()) {
            "user" -> UserMessage(dto.content)
            "system" -> SystemMessage(dto.content)
            "assistant" -> AssistantMessage(dto.content)
            else -> UserMessage(dto.content) // Fallback
        }
    }
}
