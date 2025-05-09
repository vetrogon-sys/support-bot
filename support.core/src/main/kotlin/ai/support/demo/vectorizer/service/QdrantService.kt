package ai.support.demo.vectorizer.service

import ai.support.demo.vectorizer.model.Chunk
import ai.support.demo.vectorizer.repository.Payload
import ai.support.demo.vectorizer.repository.Point
import ai.support.demo.vectorizer.repository.QdrantRepository
import ai.support.demo.vectorizer.repository.SearchRequest
import org.springframework.stereotype.Service
import java.util.*

//https://api.qdrant.tech/api-reference
@Service
class QdrantService(
    private val qdrantRepository: QdrantRepository
) {
    fun save(chunk: Chunk) {
        saveAll(listOf(chunk))
    }

    fun saveAll(chunks: Collection<Chunk>) {
        if (chunks.isEmpty()) {
            return
        }
        val points = chunks.stream()
            .map { chunk ->
                Point(
                    UUID.nameUUIDFromBytes(chunk.hash.hash.toByteArray()).toString(),
                    chunk.embedding,
                    Payload(
                        chunk.text,
                        chunk.filename,
                        chunk.hash.hash
                    )
                )
            }.toList()

        qdrantRepository.saveAll(points)
    }

    fun search(embedding: FloatArray, topK: Int = 5): List<String> {
        val searchRequest = SearchRequest(
            embedding,
            topK
        );

        return qdrantRepository.find(searchRequest)
    }

}