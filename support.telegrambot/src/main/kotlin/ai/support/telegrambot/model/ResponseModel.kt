package ai.support.telegrambot.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class MessageResponse(val message: String)

data class ChatCreationResponse(val id: String)

data class ChatMessage @JsonCreator constructor(
    val id: String,
    val message: String,
    val time: Long,
    @get:JsonProperty("message_type") val messageType: String,
    @get:JsonProperty("chat_id") val chatId: String
)

data class ChatResponse(
    val id: String,
    val messages: List<ChatMessage>
)

data class TicketResponse(
    val id: String,
    val status: String,
    val createdAt: String,
    val lastUpdated: String,
    val description: String
) {
    fun formattedResponse(): String {
        return """
            Ticket #$id
            Status: $status
            Created: $createdAt
            Last Updated: $lastUpdated
            
            Description: $description
        """.trimIndent()
    }
}

data class EscalateResponse(val message: String)

