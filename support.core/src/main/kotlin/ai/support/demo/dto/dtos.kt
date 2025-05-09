package ai.support.demo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ChatIdDto(val id: UUID);

data class ChatDto(
    val id: UUID,
    val messages: Set<MessageDto>?
)

data class MessageDto(
    var id: UUID,
    var message: String?,
    var time: Long,
    @get:JsonProperty("message_type")
    var messageType: String,
    @get:JsonProperty("chat_id")
    var chatId: String
)