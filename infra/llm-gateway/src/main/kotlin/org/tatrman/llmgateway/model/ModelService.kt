package org.tatrman.llmgateway.model

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ModelService(private val modelRepository: ModelRepository) {

    fun findAll(): Iterable<Model> = modelRepository.findAll()

    fun findByName(name: String): Model? = modelRepository.findByName(name)

    fun findById(id: Int): Model? = modelRepository.findById(id).orElse(null)

    @Transactional fun save(model: Model): Model = modelRepository.save(model)

    @Transactional fun deleteById(id: Int) = modelRepository.deleteById(id)
}
