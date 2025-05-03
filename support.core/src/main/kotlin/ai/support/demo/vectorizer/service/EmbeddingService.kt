package ai.support.demo.vectorizer.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import smile.feature.extraction.PCA

@Service
class EmbeddingService(
    private val rest: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass);

    @Value("\${vectorizer.embeddingModel}")
    lateinit var embeddingModel: String;

    @Value("\${vectorizer.ollamaBaseUrl}")
    lateinit var ollamaBaseUrl: String;

    @Value("\${vectorizer.vectorSize}")
    lateinit var vectorSize: Integer;

    fun embed(text: String): FloatArray {
        val request = mapOf(
            "model" to this.embeddingModel,
            "prompt" to text
        );

        val headers = HttpHeaders();
        headers.plus(HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON)

        val entity = HttpEntity(request, headers)

        try {
            val url = "${ollamaBaseUrl}/api/embeddings"
            log.info("POST [{}]", url)
            val response = rest.postForObject(
                url,
                entity,
                EmbeddingResponse::class.java
            )
            val embeddings = (response?.embedding?.toFloatArray()
                ?: throw IllegalStateException("Empty embedding for input text."))
//                performPCA(embeddings, vectorSize.toInt())
            return embeddings
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate embedding: ${e.message}", e)
        }
    }

    fun performPCA(embedding: FloatArray, newSize: Int): FloatArray {
        val data = arrayOf(embedding.map { it.toDouble() }.toDoubleArray())

        // Perform PCA on the input data
        val pca = PCA.fit(data)

        // Transform the data to the new size
        val transformed = pca.projection

        val padded = FloatArray(newSize)
        transformed.row(0).mapIndexed { index, value -> padded[index] = value.toFloat() }
        return padded;
    }


    private class EmbeddingResponse(
        val embedding: List<Float>,
        @JsonProperty("prompt_tokens")
        val promptTokens: Int
    )
}