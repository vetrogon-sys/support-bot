package ai.support.demo.vectorizer.service

import ai.support.demo.vectorizer.model.Chunk
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

//https://api.qdrant.tech/api-reference
@Service
class QdrantService(
    private val rest: RestTemplate,
    private val objectMapper: ObjectMapper
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

    fun search(embedding: FloatArray, topK: Int = 5): List<String> {
        val url = "${qdrantUrl}/collections/${collectionName}/points/search"
        val requestBody = mapOf(
            "vector" to embedding,
            "top" to topK,
            "with_payload" to true
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }

        val searchResponse = rest.postForEntity(
            url,
            HttpEntity(requestBody, headers),
            String::class.java
        );
        if (!searchResponse.statusCode.is2xxSuccessful) {
            throw RuntimeException("Qdrant search failed: ${searchResponse.statusCode}")
        }

        val responseJson = searchResponse.body ?: return emptyList()
        log.info(responseJson)
        val parsed = objectMapper.readValue(responseJson, QdrantSearchResponse::class.java)
        return parsed.result.mapNotNull { it.payload?.text }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class QdrantSearchResponse(
        val result: List<ResultItem>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ResultItem(
        val payload: Payload?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Payload(
        val text: String?
    )

}