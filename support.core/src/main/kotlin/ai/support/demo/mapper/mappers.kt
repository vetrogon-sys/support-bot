package ai.support.demo.mapper

import ai.support.demo.dto.ChatDto
import ai.support.demo.dto.MessageDto
import ai.support.demo.entity.Chat
import ai.support.demo.entity.ChatMessage
import ai.support.demo.entity.MessageType
import com.google.common.base.Strings
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Service

@Service
class MessageMapperService {
    fun toDto(message: ChatMessage): MessageDto {
        return MessageDto(
            id = message.id,
            message = message.message,
            time = message.time,
            chatId = message.chat.id.toString()
        )
    }

    fun toDtoList(messages: MutableSet<ChatMessage>): Set<MessageDto> {
        return messages
            .map { toDto(it) }
            .toSet()
    }

}

@Service
class ChatMapperService(
    private val messageMapperService: MessageMapperService
) {
    fun toDto(chat: Chat): ChatDto {
        return ChatDto(
            id = chat.id,
            messages = chat.messages.let { messageMapperService.toDtoList(it) }
        )
    }

    fun toDtoList(chats: List<Chat>): List<ChatDto> {
        return chats.map { toDto(it) }
    }
}

@Service
class ChatMessageToSpringAiMapper {

    fun toSpringAiMessage(message: ChatMessage): Message {
        return if (message.messageType == MessageType.USER) {
            toUserMessage(message)
        } else {
            toAssistantMessage(message);
        }
    }

    private fun toUserMessage(message: ChatMessage): UserMessage {
        return UserMessage.builder()
            .text(Strings.nullToEmpty(message.message))
            .metadata(mapOf("id" to message.id))
            .build()
    }

    private fun toAssistantMessage(message: ChatMessage): AssistantMessage {
        return AssistantMessage(
            Strings.nullToEmpty(message.message),
            mapOf("id" to message.id)
        )
    }
}