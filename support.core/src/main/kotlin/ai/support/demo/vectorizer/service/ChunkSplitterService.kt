package ai.support.demo.vectorizer.service

import ai.support.demo.service.EmbeddingService
import ai.support.demo.vectorizer.model.Chunk
import ai.support.demo.vectorizer.repository.ChunkHashRepository
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class ChunkSplitterService(
    private val chunkHashRepository: ChunkHashRepository,
    private val hashService: HashService,
    private val embeddingService: EmbeddingService
) {

    suspend fun buildChunks(filename: String, content: String): List<Chunk> = coroutineScope {
        val deferredChunks = content
            .split(Regex("\n{2,}"))
            .map { it.trim() }
            .filter { it.length > 20 }
            .distinct()
            .stream()
            .flatMap { text -> splitTextIntoChunks(text).stream() }
            .filter { text -> isValidSentence(text) }
            .parallel()
            .map { part ->
                val contentHash = hashService.hashString(part)
                if (chunkHashRepository.contains(contentHash)) return@map null

                val embedding = embeddingService.embed(part.split("\n").toList())
                Chunk(part, contentHash, filename, embedding)
            }
            .toList();

        deferredChunks.mapNotNull { it }
    }

    fun splitTextIntoChunks(text: String, maxChunkSize: Int = 500): List<String> {
        val chunks = mutableListOf<String>()
        var position = 0

        while (position < text.length) {
            val end = (position + maxChunkSize).coerceAtMost(text.length)
            var splitPos = end

            if (end < text.length) {
                // Try to find the end of the sentence or paragraph
                val nextSentenceEnd = Regex("""[.!?]\s+""").find(text, end)
                val nextParagraphEnd = text.indexOf("\n\n", end)

                splitPos = when {
                    nextParagraphEnd in (end + 1)..(end + 300) -> nextParagraphEnd + 2
                    nextSentenceEnd != null && nextSentenceEnd.range.first < end + 300 -> nextSentenceEnd.range.last + 1
                    else -> end
                }
            }

            chunks.add(text.substring(position, splitPos).trim())
            position = splitPos
        }

        return chunks
    }

    fun isValidSentence(input: String): Boolean {
        val trimmed = input.trim()

        // Check if it's not empty or blank
        if (trimmed.isEmpty()) return false

        // Check if it contains at least two words
        val words = trimmed.split("\\s+".toRegex())
        return words.size >= 2
    }


}