package org.tatrman.llmgateway.grpc

import org.tatrman.llmgateway.web.ChatCompletionRequest
import org.tatrman.llmgateway.web.ChatController
import org.tatrman.llmgateway.web.ChatMessageDto
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class GrpcChatService(private val chatController: ChatController) :
        ChatServiceGrpc.ChatServiceImplBase() {

    override fun chat(request: ChatRequest, responseObserver: StreamObserver<ChatResponse>) {
        try {
            // Check auth (assuming Interceptor handles Context, otherwise manual check needed if
            // not using Spring Security gRPC)

            // Convert Proto -> DTO
            val dto =
                    ChatCompletionRequest(
                            model = request.model,
                            messages =
                                    request.messagesList.map { ChatMessageDto(it.role, it.content) }
                    )

            // Call Logic
            val result = chatController.chat(dto)

            // Convert DTO -> Proto
            val response =
                    ChatResponse.newBuilder()
                            .setContent(result.content)
                            .setModel(request.model) // Echo back
                            .build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(e)
        }
    }
}
