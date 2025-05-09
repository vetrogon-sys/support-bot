package ai.support.demo.vectorizer.service

import org.springframework.stereotype.Service

@Service
class ChunkSplitterService {

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