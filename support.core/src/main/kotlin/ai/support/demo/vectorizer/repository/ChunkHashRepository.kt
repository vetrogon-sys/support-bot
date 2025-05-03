package ai.support.demo.vectorizer.repository

import ai.support.demo.vectorizer.model.Chunk
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class ChunkHashRepository {
    private val chunkStore : MutableMap<String, Chunk> = ConcurrentHashMap();

    fun save(chunk: Chunk) {
        chunkStore.put(chunk.hash, chunk);
    }

    fun contains(hash: String) : Boolean {
        return chunkStore.contains(hash);
    }

    fun get(hash: String) : Chunk? {
        return chunkStore[hash];
    }

    fun remove(hash: String) {
        chunkStore.remove(hash);
    }

}