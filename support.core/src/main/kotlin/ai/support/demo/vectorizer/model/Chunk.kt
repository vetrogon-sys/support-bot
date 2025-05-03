package ai.support.demo.vectorizer.model

data class Chunk(val text: String, val hash: String, val filename: String, val embedding: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chunk) return false

        if (hash != other.hash) return false
        if (filename != other.filename) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + filename.hashCode()
        return result
    }
}