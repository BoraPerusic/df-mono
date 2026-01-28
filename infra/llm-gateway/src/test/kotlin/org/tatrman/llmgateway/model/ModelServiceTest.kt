package org.tatrman.llmgateway.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class ModelServiceTest :
        StringSpec({
            val modelRepository = mockk<ModelRepository>()
            val modelService = ModelService(modelRepository)

            val sampleModel =
                    Model(
                            id = 1,
                            name = "test-model",
                            provider = "openai",
                            modelType = "chat",
                            tags = arrayOf("test"),
                            config = "{}"
                    )

            "findAll should return all models" {
                every { modelRepository.findAll() } returns listOf(sampleModel)

                val result = modelService.findAll().toList()

                result.size shouldBe 1
                result[0].name shouldBe "test-model"
                verify(exactly = 1) { modelRepository.findAll() }
            }

            "findById should return model if exists" {
                every { modelRepository.findById(1) } returns Optional.of(sampleModel)

                val result = modelService.findById(1)

                result shouldBe sampleModel
            }

            "findById should return null if not exists" {
                every { modelRepository.findById(99) } returns Optional.empty()

                val result = modelService.findById(99)

                result shouldBe null
            }

            "save should save model" {
                every { modelRepository.save(any()) } returns sampleModel

                val result = modelService.save(sampleModel)

                result shouldBe sampleModel
                verify(exactly = 1) { modelRepository.save(sampleModel) }
            }

            "deleteById should delete model" {
                every { modelRepository.deleteById(1) } returns Unit

                modelService.deleteById(1)

                verify(exactly = 1) { modelRepository.deleteById(1) }
            }
        })
