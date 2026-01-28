package org.tatrman.llmgateway.web

import org.tatrman.llmgateway.async.Job
import org.tatrman.llmgateway.async.JobService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class JobSubmitResponse(val jobId: String)

@RestController
@RequestMapping("/api/v1/async/chat")
class AsyncChatController(private val jobService: JobService) {

    @PostMapping("/completions")
    fun submit(@RequestBody request: ChatCompletionRequest): JobSubmitResponse {
        val jobId = jobService.submitJob(request)
        return JobSubmitResponse(jobId)
    }

    @GetMapping("/jobs/{id}")
    fun poll(@PathVariable id: String): Job? {
        return jobService.getJob(id)
    }
}
