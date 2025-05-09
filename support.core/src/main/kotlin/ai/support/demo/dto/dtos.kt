package ai.support.demo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ChatDto(
    val id: UUID,
    val messages: List<MessageDto>?
)

data class MessageDto(
    var id: UUID,
    var message: String,
    var time: Long,
    var sender: String,
    @get:JsonProperty("chat_id")
    var chatId: String
)