package ai.support.demo.mapper

import ai.support.demo.dto.ChatDto
import ai.support.demo.dto.MessageDto
import ai.support.demo.entity.Chat
import ai.support.demo.entity.Message
import ai.support.demo.entity.Sender
import org.springframework.stereotype.Service

@Service
class MessageMapperService {
    fun toDto(message: Message): MessageDto {
        return MessageDto(
            id = message.id,
            message = message.message,
            time = message.time,
            sender = message.sender.name,
            chatId = message.chat.id.toString()
        )
    }

    fun toModel(messageDto: MessageDto, chat: Chat): Message {
        return Message(
            id = messageDto.id,
            message = messageDto.message,
            time = messageDto.time,
            sender = Sender.valueOf(messageDto.sender),
            chat = chat
        )
    }

    fun toDtoList(messages: List<Message>): List<MessageDto> {
        return messages.map { toDto(it) }
    }

    fun toModelList(messageDtos: List<MessageDto>, chat: Chat): List<Message> {
        return messageDtos.map { toModel(it, chat) }
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

    fun toModel(chatDto: ChatDto): Chat {
        return Chat(
            id = chatDto.id,
            messages = chatDto.messages?.let {
                messageMapperService.toModelList(it, Chat(id = chatDto.id))
            } ?: emptyList()
        )
    }

    fun toDtoList(chats: List<Chat>): List<ChatDto> {
        return chats.map { toDto(it) }
    }

    fun toModelList(chatDtos: List<ChatDto>): List<Chat> {
        return chatDtos.map { toModel(it) }
    }
}