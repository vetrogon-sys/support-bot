package ai.support.demo.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import org.hibernate.Interceptor
import java.io.Serializable
import java.util.*

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
        targetEntity = Message::class,
        orphanRemoval = true
    )
    var messages: List<Message> = emptyList()
) : Interceptor {
}

@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
class Message(
    @Id
    var id: UUID = UUID.randomUUID(),
    var message: String,
    var time: Long,
    @Enumerated(EnumType.STRING)
    var sender: Sender,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    var chat: Chat
) : Serializable, Interceptor {
}

enum class Sender {
    USER,
    CHAT_BOT
}