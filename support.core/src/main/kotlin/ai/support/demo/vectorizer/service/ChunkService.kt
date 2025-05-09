package ai.support.demo.vectorizer.service

import ai.support.demo.vectorizer.model.Chunk
import ai.support.demo.vectorizer.repository.ChunkHashRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChunkService(
    private val splitterService: ChunkSplitterService,
    private val fileScannerService: CrawlerFileScannerService,
    private val chunkHashRepository: ChunkHashRepository,
    private val qdrantService: QdrantService
) {
    private val log = LoggerFactory.getLogger(javaClass);

    @PostConstruct
    fun processFileChunks() {
        val newChunks: MutableSet<Chunk> = HashSet()
        fileScannerService.readDataLake().entries
            .parallelStream()
            .forEach { dataEntry ->
                val filename = dataEntry.key;
                val text = dataEntry.value
                val fileChunks = runBlocking {
                    splitterService.buildChunks(filename, text)
                };
                log.info("Build {} chunks from file {}", fileChunks.size, filename)
                fileChunks.forEach { chunk ->
                    chunkHashRepository.save(chunk)
                    qdrantService.save(newChunks);
                }
            }
    }

}