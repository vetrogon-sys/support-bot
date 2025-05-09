package ai.support.demo.service

import ai.support.demo.dto.ChatDto
import ai.support.demo.entity.Chat
import ai.support.demo.entity.ChatMessage
import ai.support.demo.mapper.ChatMapperService
import ai.support.demo.repository.ChatRepository
import ai.support.demo.repository.MessageRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMapper: ChatMapperService
) {

    fun getAllChats() : List<ChatDto> {
        return chatRepository.findAll()
            .map { chat ->
                chatMapper.toDto(chat)
            }
    }

    fun createEmptyChat() : ChatDto {
        return chatMapper.toDto(chatRepository.save(Chat()))
    }

}

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val modelService: ChatBotModelService,
    private val chatMapper: ChatMapperService
) {

    fun handleMessage(chatId: UUID, message: String) : ChatDto {
        return chatRepository.findById(chatId)
            .map { chat ->
                modelService.getAnswer(
                    DefaultLLMRequest(
                        chat,
                        message
                    )
                )
                chatMapper.toDto(chat)
            }.orElseThrow { RuntimeException("Can't find chat with id $chatId") }
    }

}