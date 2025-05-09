package ai.support.demo.vectorizer.service

import ai.support.demo.service.EmbeddingService
import ai.support.demo.vectorizer.model.Chunk
import ai.support.demo.vectorizer.model.ChunkHash
import ai.support.demo.vectorizer.repository.ChunkHashRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class ChunkService(
    private val splitterService: ChunkSplitterService,
    private val fileScannerService: CrawlerFileScannerService,
    private val chunkHashRepository: ChunkHashRepository,
    private val qdrantService: QdrantService,
    private val hashService: HashService,
    private val embeddingService: EmbeddingService
) {

    @PostConstruct
    fun processFileChunks() {
        fileScannerService.readDataLake().entries
            .parallelStream()
            .forEach { dataEntry ->
                val filename = dataEntry.key;
                val text = dataEntry.value
                buildChunks(filename, text)
            }
    }

    fun buildChunks(filename: String, content: String) {
        return content
            .split(Regex("\n{2,}"))
            .map { it.trimIndent() }
            .filter { it.length > 10 }
            .distinct()
            .stream()
            .flatMap { text -> splitterService.splitTextIntoChunks(text).stream() }
            .filter { text -> splitterService.isValidSentence(text) }
            .parallel()
            .forEach { part ->
                val contentHash = hashService.hashString(part)
                if (chunkHashRepository.existsById(contentHash)) return@forEach

                val embedding = embeddingService.embed(part.split("\n").toList())
                val chunk = Chunk(part, ChunkHash(contentHash), filename, embedding)
                chunkHashRepository.save(chunk.hash)
                qdrantService.save(chunk);
            }
    }


}