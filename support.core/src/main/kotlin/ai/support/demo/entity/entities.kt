package ai.support.demo.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import org.hibernate.Interceptor
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Entity
@Table(name = "chats")
@NamedEntityGraph(
    name = "chat-with-messages",
    attributeNodes = [NamedAttributeNode("messages")]
)
@Data
@AllArgsConstructor
@NoArgsConstructor
class Chat(
    @Id
    var id: UUID = UUID.randomUUID(),
    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.REMOVE],
        mappedBy = "chat",
        targetEntity = ChatMessage::class,
        orphanRemoval = true
    )
    var messages: MutableSet<ChatMessage> = ConcurrentHashMap.newKeySet()
) : Interceptor {
}

@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
class ChatMessage(
    @Id
    var id: UUID = UUID.randomUUID(),
    var message: String?,
    var time: Long,
    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    var messageType: MessageType,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    var chat: Chat
) : Serializable, Interceptor {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChatMessage) return false

        if (time != other.time) return false
        if (id != other.id) return false
        if (message != other.message) return false
        if (messageType != other.messageType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + messageType.hashCode()
        return result
    }
}

enum class MessageType {
    USER,
    ASSISTANT;
}
