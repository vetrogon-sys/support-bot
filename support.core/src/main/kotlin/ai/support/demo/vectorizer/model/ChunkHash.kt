package ai.support.demo.vectorizer.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("chunk_hashes")
data class ChunkHash(
    @Id val hash: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkHash) return false

        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }
}
