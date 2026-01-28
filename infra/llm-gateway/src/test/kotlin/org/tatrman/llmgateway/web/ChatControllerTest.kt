package org.tatrman.llmgateway.web

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.tatrman.llmgateway.model.Model
import org.tatrman.llmgateway.model.ModelService
import org.tatrman.llmgateway.observability.ObservabilityService
import org.tatrman.llmgateway.rules.RuleEngine

class ChatControllerTest :
        StringSpec({
                val ruleEngine = mockk<RuleEngine>()
                val modelService = mockk<ModelService>()
                val observabilityService = mockk<ObservabilityService>(relaxed = true)

                val builder = mockk<ChatClient.Builder>()
                val chatClient = mockk<ChatClient>()

                every { builder.build() } returns chatClient

                val controller =
                        ChatController(ruleEngine, modelService, builder, observabilityService)

                "chat should delegate to selected model" {
                        val request =
                                ChatCompletionRequest(
                                        model = "gpt-4",
                                        messages = listOf(ChatMessageDto("user", "hi"))
                                )

                        val model = Model(1, "gpt-4", "desc", "openai", "chat", emptyArray(), "{}")
                        every { modelService.findAll() } returns listOf(model)
                        every { ruleEngine.selectModel(any(), any()) } returns model

                        // Mock Fluent API chain
                        every { chatClient.prompt(any<Prompt>()) } returns
                                mockk {
                                        every { call() } returns
                                                mockk { every { content() } returns "Hello World" }
                                }

                        val response = controller.chat(request)

                        response.content shouldBe "Hello World"

                        verify(exactly = 1) { observabilityService.recordInteraction(any()) }
                }
        })
