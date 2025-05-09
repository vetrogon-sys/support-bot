package ai.support.demo.service

import ai.support.demo.vectorizer.service.QdrantService
import org.springframework.stereotype.Service

@Service
class ContextBuilder(
    private val embeddingService: EmbeddingService,
    private val qdrantService: QdrantService
) {

    fun buildContext(question: String) : String {
        val embedding = embeddingService.embed(listOf(question))
        val chunks = qdrantService.search(embedding);

        return chunks.joinToString(separator = "\n\n") { "- $it" };
    }

}