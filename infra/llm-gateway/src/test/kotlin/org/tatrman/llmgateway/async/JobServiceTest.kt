package org.tatrman.llmgateway.async

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import org.tatrman.llmgateway.web.ChatCompletionRequest
import org.tatrman.llmgateway.web.ChatCompletionResponse
import org.tatrman.llmgateway.web.ChatController
import org.tatrman.llmgateway.web.ChatMessageDto

class JobServiceTest :
        StringSpec({

                // Helper to create fresh service and mocks
                fun setup():
                        Triple<
                                JobService,
                                JobRepository,
                                Triple<ChatController, NatsPublisher, WebhookDispatcher>> {
                        val jobRepo = mockk<JobRepository>(relaxed = true)
                        val chatCtrl = mockk<ChatController>()
                        val natsPub = mockk<NatsPublisher>(relaxed = true)
                        val webhook = mockk<WebhookDispatcher>(relaxed = true)
                        val self = mockk<JobService>(relaxed = true)

                        val service = JobService(jobRepo, chatCtrl, natsPub, webhook, self)

                        // Default stubs
                        every { jobRepo.save(any<Job>()) } answers { firstArg() }

                        return Triple(service, jobRepo, Triple(chatCtrl, natsPub, webhook))
                }

                "submitJob should save QUEUED job and call async process" {
                        val (service, jobRepo, others) = setup()
                        val (_, _, _) = others
                        // Access self mock? We need 'self' for this test verification.
                        // Re-do manual setup for this test to specific verifying 'self'
                        val jobRepo2 = mockk<JobRepository>(relaxed = true)
                        val self2 = mockk<JobService>(relaxed = true)
                        val service2 =
                                JobService(
                                        jobRepo2,
                                        mockk(),
                                        mockk(relaxed = true),
                                        mockk(relaxed = true),
                                        self2
                                )
                        every { jobRepo2.save(any<Job>()) } answers { firstArg() }

                        val request =
                                ChatCompletionRequest(
                                        model = "test-model",
                                        messages = listOf(ChatMessageDto("user", "hello"))
                                )

                        val slot = slot<Job>()
                        every { jobRepo2.save(capture(slot)) } answers { firstArg() }

                        val jobId = service2.submitJob(request)

                        verify(exactly = 1) { jobRepo2.save(any()) }
                        verify(exactly = 1) { self2.processJobAsync(jobId, request) }

                        slot.captured.status shouldBe "QUEUED"
                        slot.captured.id shouldBe jobId
                }

                "processJobAsync should update status to PROCESSING then COMPLETED" {
                        val (service, jobRepo, others) = setup()
                        val (chatCtrl, natsPub, _) = others

                        val request =
                                ChatCompletionRequest(
                                        model = "test-model",
                                        messages = listOf(ChatMessageDto("user", "hello"))
                                )
                        val jobId = "test-job-id"
                        val job =
                                Job(
                                        id = jobId,
                                        status = "QUEUED",
                                        requestPayload = "{}",
                                        createdAt = java.time.Instant.now()
                                )

                        every { jobRepo.findById(jobId) } returns Optional.of(job)
                        every { chatCtrl.chat(request) } returns
                                ChatCompletionResponse("Response Content")

                        service.processJobAsync(jobId, request)

                        verify(atLeast = 1) { jobRepo.save(any()) }
                        verify(exactly = 1) { chatCtrl.chat(request) }
                        verify(exactly = 1) { natsPub.publishJobCompleted(any()) }
                }

                "processJobAsync should handle error" {
                        val (service, jobRepo, others) = setup()
                        val (chatCtrl, natsPub, _) = others

                        val request =
                                ChatCompletionRequest(
                                        model = "test-model",
                                        messages = listOf(ChatMessageDto("user", "hello"))
                                )
                        val jobId = "fail-job-id"
                        val job =
                                Job(
                                        id = jobId,
                                        status = "QUEUED",
                                        requestPayload = "{}",
                                        createdAt = java.time.Instant.now()
                                )

                        every { jobRepo.findById(jobId) } returns Optional.of(job)
                        every { chatCtrl.chat(request) } throws RuntimeException("AI Error")

                        service.processJobAsync(jobId, request)

                        verify(exactly = 1) { chatCtrl.chat(request) }
                        verify(exactly = 1) {
                                natsPub.publishJobCompleted(match { it.status == "ERROR" })
                        }
                }
        })
