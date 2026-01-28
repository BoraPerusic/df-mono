package org.tatrman.llmgateway

import com.github.tomakehurst.wiremock.client.WireMock.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.tatrman.llmgateway.model.Model
import org.tatrman.llmgateway.model.ModelRepository
import org.tatrman.llmgateway.web.ChatCompletionRequest
import org.tatrman.llmgateway.web.ChatMessageDto

class LlmGatewayIntegrationTest : BaseIntegrationTest() {

    @Autowired lateinit var restTemplate: TestRestTemplate

    @Autowired lateinit var modelRepository: ModelRepository

    @Test
    fun `Happy Path - Full Chat Flow`() {
        // 1. Setup Data: Ensure a model exists matching our request
        // The migration V1 inserts generic models, but let's ensure we have 'gpt-4' mapped.
        // Or specific one. Let's insert one for test.
        val model =
                Model(
                        name = "integration-test-model",
                        provider = "openai",
                        modelType = "chat",
                        tags = arrayOf("test"),
                        config = "{}"
                )
        modelRepository.save(model)

        // 2. Mock External Services
        // a) OAuth2 JWKS is auto-stubbed by BaseIntegrationTest.resetWireMock()
        // b) OpenAI Completions
        // Path depends on Spring AI's default. usually /v1/chat/completions
        wireMockServer.stubFor(
                post(urlPathMatching("/v1/chat/completions"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            {
                              "id": "chatcmpl-123",
                              "object": "chat.completion",
                              "created": 1677652288,
                              "model": "gpt-3.5-turbo",
                              "choices": [{
                                "index": 0,
                                "message": {
                                  "role": "assistant",
                                  "content": "I am a mocked generic integration test response."
                                },
                                "finish_reason": "stop"
                              }],
                              "usage": {
                                "prompt_tokens": 9,
                                "completion_tokens": 12,
                                "total_tokens": 21
                              }
                            }
                        """.trimIndent()
                                        )
                        )
        )

        // 3. Prepare Request
        val request =
                ChatCompletionRequest(
                        model = "integration-test-model",
                        messages = listOf(ChatMessageDto("user", "Hello Integration!"))
                )
        val token = createTestToken("test-user")
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity(request, headers)

        // 4. Execute
        val response =
                restTemplate.exchange(
                        "/api/v1/chat/completions",
                        HttpMethod.POST,
                        entity,
                        String::class
                                .java // ChatCompletionResponse is internal DTO, getting raw JSON or
                        // mapping it
                        )

        // 5. Verify
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldContain "I am a mocked generic integration test response"

        // Check DB prompt log present? (Optional, requires PromptLogRepository injection)
    }

    @Test
    fun `Resilience - OpenAI Down`() {
        // 1. Setup Data
        val model =
                Model(
                        name = "resilience-test-model", // Use a fresh name to avoid conflict
                        provider = "openai",
                        modelType = "chat",
                        config = "{}"
                )
        modelRepository.save(model)

        // 2. Mock Failure (500 or Connection Refused)
        wireMockServer.stubFor(
                post(urlPathMatching("/v1/chat/completions"))
                        .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
        )

        // 3. Execute
        val request =
                ChatCompletionRequest(
                        model = "resilience-test-model",
                        messages = listOf(ChatMessageDto("user", "Hello?"))
                )
        val token = createTestToken("test-user")
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity(request, headers)

        val response =
                restTemplate.exchange(
                        "/api/v1/chat/completions",
                        HttpMethod.POST,
                        entity,
                        String::class.java
                )

        // 4. Verify Graceful Handling
        // Should be 503 or 500, but definitely actual error response, not crash.
        // Our controller throws exception, default Spring Boot handler returns 500 usually.
        // Or if we mapped it, specific code. For now accepting 500.

        response.statusCode.is5xxServerError shouldBe true
    }
}
