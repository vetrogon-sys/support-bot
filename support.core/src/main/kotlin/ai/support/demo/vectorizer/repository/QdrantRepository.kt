package ai.support.demo.vectorizer.repository

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture

@Repository
class QdrantRepository(
    private val rest: RestTemplate,
    private val requestMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass);

    @Value("\${qdrant.url}")
    lateinit var qdrantUrl: String;

    @Value("\${qdrant.collection.name}")
    lateinit var collectionName: String;

    @Value("\${qdrant.collection.vectorSize}")
    lateinit var vectorSize: Integer;

    @PostConstruct
    fun initQdrant() {
        val url = getQdrantUrl()

        CompletableFuture.supplyAsync {
            return@supplyAsync rest
                .getForEntity("${url}/exists", ExistResponse::class.java)
                .body
        }.thenAccept { response ->
            if (response == null || response.result.exists.not()) {
                initCollection(url)
            }
        }
    }

    private fun initCollection(url: String) {
        val request = mapOf(
            "vectors" to mapOf(
                "size" to vectorSize,
                "distance" to "Cosine"
            )
        )

        try {
            val entity = HttpEntity(request, getContentJsonHeaders())
            rest.exchange(url, HttpMethod.PUT, entity, String::class.java)
            log.info("Qdrant collection '${collectionName}' ensured")
        } catch (e: Exception) {
            log.warn("Failed to create collection: ${e.message}")
        }
    }

    fun saveAll(points: List<Point>) {
        val body = mapOf("points" to requestMapper.writeValueAsString(points));

        try {
            val url = "${getQdrantUrl()}/points"
            log.info("PUT [${url}] to save ${points.size} points")
            val response = rest.exchange(
                url,
                HttpMethod.PUT,
                HttpEntity(body, getContentJsonHeaders()),
                String::class.java
            )

            log.info("Save ${points.size} to Qdrant: ${response.statusCode}")
            log.debug("Qdrant response: ${response.body}")
        } catch (e: Exception) {
            log.warn(e.message, e)
        }
    }

    fun find(searchRequest: SearchRequest) : List<String> {
        val body = requestMapper.writeValueAsString(searchRequest);

        try {
            val url = "${getQdrantUrl()}/points/search"
            log.info("POST [${url}] to search Qdrant vector")
            val searchResponse = rest.postForEntity(
                url,
                HttpEntity(body, getContentJsonHeaders()),
                QdrantSearchResponse::class.java
            );

            if (searchResponse.statusCode.is2xxSuccessful.not()) {
                return listOf();
            }
            return searchResponse.body!!.result
                .mapNotNull { it.payload?.text }
        } catch (e: Exception) {
            throw RuntimeException(e.message)
        }
    }

    private fun getContentJsonHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun getQdrantUrl(): String = "${qdrantUrl}/collections/${collectionName}"
}

data class Point(
    val id: String,
    val vector: FloatArray,
    val payload: Payload
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (id != other.id) return false
        if (!vector.contentEquals(other.vector)) return false
        if (payload != other.payload) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + vector.contentHashCode()
        result = 31 * result + payload.hashCode()
        return result
    }
}

data class Payload(
    val text: String?,
    val source: String?,
    val hash: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QdrantSearchResponse(
    val result: List<ResultItem>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResultItem(
    val payload: Payload?
)

data class SearchRequest(
    val vector: FloatArray,
    val top: Int = 5,
    @get:JsonProperty("with_payload") val withPayload: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SearchRequest) return false

        if (top != other.top) return false
        if (withPayload != other.withPayload) return false
        if (!vector.contentEquals(other.vector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = top
        result = 31 * result + withPayload.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}

data class ExistResponse(
    val result: Result
) {
    data class Result(
        val exists: Boolean
    )
}