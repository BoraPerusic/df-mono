package org.tatrman.llmgateway.web

import org.tatrman.llmgateway.model.Model
import org.tatrman.llmgateway.model.ModelService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/models")
class ModelController(private val modelService: ModelService) {

    @GetMapping fun listModels(): Iterable<Model> = modelService.findAll()

    @PostMapping fun createModel(@RequestBody model: Model): Model = modelService.save(model)

    @DeleteMapping("/{id}") fun deleteModel(@PathVariable id: Int) = modelService.deleteById(id)
}
