package ai.support.demo.repository

import ai.support.demo.entity.Chat
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.UUID

@RepositoryRestResource
interface ChatRepository : CrudRepository<Chat, UUID> {
}
