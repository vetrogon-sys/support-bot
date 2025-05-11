package ai.support.demo.service

import ai.support.demo.dto.ChatDto
import ai.support.demo.dto.ChatIdDto
import ai.support.demo.entity.Chat
import ai.support.demo.mapper.ChatMapperService
import ai.support.demo.repository.ChatRepository
import ai.support.demo.vectorizer.service.ChunkService
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMapper: ChatMapperService
) {

    fun getAllChats(): List<ChatIdDto> {
        return chatRepository.findAll()
            .map { chat ->
                chatMapper.toIdDto(chat)
            }
    }

    fun getChatById(chatId: UUID): ChatDto {
        return chatRepository.findById(chatId)
            .map { chat -> chatMapper.toDto(chat) }
            .orElseThrow { RuntimeException("Can't find chat with id $chatId") }
    }

    fun createEmptyChat(): ChatDto {
        return chatMapper.toDto(chatRepository.save(Chat()))
    }

    fun deleteChat(chatId: UUID) {
        chatRepository.deleteById(chatId)
    }

}

@Service
class SupportChatMessageService(
    private val modelService: SupportBotModelService,
    private val chatMapper: ChatMapperService,
    private val chatRepository: ChatRepository,
    private val chunkService: ChunkService
) {

    fun handleUserMessage(chatId: UUID, message: String): ChatDto {
        return answerOnRequest(
            SupportLLMRequest(
                chatId.toString(),
                message,
                RequestType.USER
            ),
            chatId
        )
    }

    fun handleSupportMessage(chatId: UUID, message: String): ChatDto {
        chunkService.buildChunks("support_answer_${chatId}", message)
        return answerOnRequest(
            SupportLLMRequest(
                chatId.toString(),
                message,
                RequestType.SUPPORT
            ),
            chatId
        )
    }

    private fun answerOnRequest(request: SupportLLMRequest, chatId: UUID): ChatDto {
        return chatRepository.findById(chatId)
            .map { chat ->
                modelService.getAnswer(
                    request
                )
                chatMapper.toDto(chat)
            }.orElseThrow { RuntimeException("Can't find chat with id $chatId") }
    }

}

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val modelService: ChatBotModelService,
    private val chatMapper: ChatMapperService
) {

    fun handleMessage(chatId: UUID, message: String): ChatDto {
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