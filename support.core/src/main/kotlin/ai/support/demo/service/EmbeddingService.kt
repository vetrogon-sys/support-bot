package ai.support.demo.service

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Service

@Service
class EmbeddingService(
    private val embeddingModel: EmbeddingModel
) {
    fun embed(embeddingParts: List<String>): FloatArray {
        val embeddingResponse = embeddingModel.embedForResponse(embeddingParts)
        return embeddingResponse.result.output
    }
}