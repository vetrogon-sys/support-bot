package ai.support.demo.repository

import ai.support.demo.entity.Chat
import ai.support.demo.entity.ChatMessage
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.Optional
import java.util.UUID

@RepositoryRestResource
interface ChatRepository : CrudRepository<Chat, UUID> {

    @EntityGraph(value = "chat-with-messages")
    fun findWithMessagesById(id: UUID) : Optional<Chat>;

}

interface MessageRepository : JpaRepository<ChatMessage, UUID> {}
