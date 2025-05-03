package ai.support.demo.vectorizer.service

import ai.support.demo.vectorizer.model.Chunk
import ai.support.demo.vectorizer.repository.ChunkHashRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ChunkService(
    private val splitterService: ChunkSplitterService,
    private val fileScannerService: CrawlerFileScannerService,
    private val chunkHashRepository: ChunkHashRepository,
    private val qdrantService: QdrantService
) {
    private val log = LoggerFactory.getLogger(javaClass);
    private val collectedFiles: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @Scheduled(fixedDelay = 2_000L)
    fun processChunks() {
        val newChunks: MutableSet<Chunk> = HashSet()
        fileScannerService.readDataLake().entries
            .parallelStream()
            .forEach { dataEntry ->
                val filename = dataEntry.key;
                val text = dataEntry.value
                if (collectedFiles.add(filename)) {
                    val fileChunks = runBlocking {
                        splitterService.buildChunks(filename, text)
                    };
                    log.info("Build {} chunks from file {}", fileChunks.size, filename)
                    fileChunks.forEach { chunk ->
                        chunkHashRepository.save(chunk)
                        newChunks.add(chunk)
                    }
                }
            }

        if (newChunks.isEmpty().not()) {
            qdrantService.save(newChunks);
        }
    }

}