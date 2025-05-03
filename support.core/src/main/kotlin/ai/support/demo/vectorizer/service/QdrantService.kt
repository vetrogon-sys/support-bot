package ai.support.demo.vectorizer.service

import ai.support.demo.vectorizer.model.Chunk
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

//https://api.qdrant.tech/api-reference
@Service
class QdrantService(
    private val rest: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass);

    @Value("\${vectorizer.collectionName}")
    lateinit var collectionName: String;
    @Value("\${vectorizer.qdrantUrl}")
    lateinit var qdrantUrl: String;
    @Value("\${vectorizer.vectorSize}")
    lateinit var vectorSize: Integer;

    @PostConstruct
    fun ensureCollection() {
        val url = "${qdrantUrl}/collections/${collectionName}"
        val request = mapOf(
            "vectors" to mapOf(
                "size" to 1024,
                "distance" to "Cosine"
            )
        )

        val headers = HttpHeaders();
        headers.plus(HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON)

        try {
            val entity = HttpEntity(request, headers)
            rest.exchange(url, HttpMethod.PUT, entity, String::class.java)
            log.info("Qdrant collection '${collectionName}' ensured")
        } catch (e: Exception) {
            log.warn("Failed to create collection: ${e.message}")
        }
    }

    fun save(chunks: Collection<Chunk>) {
        val url = "${qdrantUrl}/collections/${collectionName}/points"

        val points = chunks.mapIndexed { index, chunk ->
            mapOf(
                "id" to UUID.nameUUIDFromBytes(chunk.hash.toByteArray()).toString(),
                "vector" to chunk.embedding,
                "payload" to mapOf(
                    "text" to chunk.text,
                    "source" to chunk.filename,
                    "hash" to chunk.hash
                )
            )
        }

        val body = mapOf("points" to points)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        try {
            val response = rest.exchange(url, HttpMethod.PUT, HttpEntity(body, headers), String::class.java)
            log.info("Uploaded ${points.size} chunks to Qdrant: ${response.body}")
        } catch (e: Exception) {
            log.warn("Qdrant upload failed: ${e.message}")
        }
    }

}