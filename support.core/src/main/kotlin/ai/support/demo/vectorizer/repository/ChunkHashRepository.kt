package ai.support.demo.vectorizer.repository

import ai.support.demo.vectorizer.model.ChunkHash
import org.springframework.data.repository.CrudRepository

interface ChunkHashRepository : CrudRepository<ChunkHash, String> {
}