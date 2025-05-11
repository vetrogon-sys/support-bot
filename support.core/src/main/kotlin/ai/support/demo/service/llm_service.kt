package ai.support.demo.service

import ai.support.demo.entity.Chat
import ai.support.demo.entity.ChatMessage
import ai.support.demo.entity.MessageType.ASSISTANT
import ai.support.demo.entity.MessageType.USER
import ai.support.demo.mapper.ChatMessageToSpringAiMapper
import ai.support.demo.repository.ChatRepository
import ai.support.demo.repository.MessageRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.model.ChatModel
import org.springframework.stereotype.Service
import java.util.*

interface LLMService {
    fun getAnswer(request: LLMRequest): LLMAnswer
}

@Service
class SupportBotModelService(
    private val contextBuilder: ContextBuilder,
    private val chatModel: ChatModel,
    private val chatMessageRepository: ChatMessageRepository
) : LLMService {
    private val log = LoggerFactory.getLogger(javaClass);

    private lateinit var chatClient: ChatClient;

    @PostConstruct
    private fun initialize() {
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(
                """
                Role:
                You are an AI assistant supporting employees. Your task is to either draft user replies or evaluate support responses based on the input format. 
                Input Formats and Instructions:
                    USER: <message>
                      - This is a customer inquiry.
                      - Your task: Draft a polite, professional, and concise reply prototype in the same language as the user.
                      - Keep responses clear, solution-oriented, and aligned with support best practices.
                    SUPPORT: <message>
                      - This is a support agent’s reply to a user.
                      - Your task: Rate the response on a scale of 1-5 (5 = excellent) based on:
                          - Clarity and professionalism
                          - Accuracy and helpfulness
                          - Language consistency (matches user's language)
                      - Rating format: ASSISTANT: <rating>/5 - <brief reason> (e.g., "4/5 - Clear but could be more concise.")
                Rules:
                    Respond strictly in the format: ASSISTANT: <message>.
                    No extra text, explanations, or deviations from the requested task.
                    For USER messages, only provide the prototype reply.
                    For SUPPORT messages, only provide the rating and a brief (1-sentence) rationale.
                    """.trimMargin()
            )
            .defaultAdvisors(
                MessageChatMemoryAdvisor(
                    MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMessageRepository)
                        .build()
                )
            )
            .build();
    }

    override fun getAnswer(request: LLMRequest): LLMAnswer {
        val prompt: String
        if (request is SupportLLMRequest) {
            prompt = when (request.getRequestType()) {
                RequestType.USER -> {
                    val context = contextBuilder.buildContext(request.getQuestion());
                    buildPrompt(request.getQuestion(), context)
                }
                RequestType.SUPPORT -> {
                    buildSupportPrompt(request.getQuestion())
                }
            }
        } else {
            val context = contextBuilder.buildContext(request.getQuestion());
            prompt = buildPrompt(request.getQuestion(), context)
        }

        log.debug(prompt)

        val response = chatClient
            .prompt(prompt)
            .advisors { a ->
                a
                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.getChatId())
                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
            }
            .call()

        return LLMAnswer(response.content());
    }

    private fun buildPrompt(question: String, context: String): String {
        return """
            Use the following knowledge base context to answer the question.
            Context:
            $context
            Question:
            "$question"
        """.trimIndent()
    }

    private fun buildSupportPrompt(message: String): String {
        return """
            Analise support response.
            Response:
            "$message"
        """.trimIndent()
    }

}

@Service
class ChatBotModelService(
    private val chatModel: ChatModel,
    private val chatMessageRepository: ChatMessageRepository
) : LLMService {

    private lateinit var chatClient: ChatClient;

    @PostConstruct
    private fun initialize() {
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem(
                "You are a chat bot for socializing."
            )
            .defaultAdvisors(
                MessageChatMemoryAdvisor(
                    MessageWindowChatMemory.builder()
                        .chatMemoryRepository(chatMessageRepository)
                        .build()
                )
            )
            .build();
    }

    override fun getAnswer(request: LLMRequest): LLMAnswer {

        val answer = chatClient
            .prompt(request.getQuestion())
            .advisors { a ->
                a
                    .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.getChatId())
                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
            }.call()

        return LLMAnswer(answer.content());
    }
}

@Service
class ChatMessageRepository(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val messageMapper: ChatMessageToSpringAiMapper
) : ChatMemoryRepository {

    override fun findConversationIds(): List<String?> {
        return chatRepository.findAll()
            .map { chat -> chat.id.toString() }
    }

    override fun findByConversationId(conversationId: String): List<Message?> {
        return chatRepository.findWithMessagesById(UUID.fromString(conversationId))
            .map { chat ->
                chat.messages
                    .map { message ->
                        messageMapper.toSpringAiMessage(message)
                    }
                    .toList()
            }
            .orElse(emptyList())
    }

    override fun saveAll(
        conversationId: String,
        messages: List<Message?>
    ) {
        chatRepository.findById(UUID.fromString(conversationId))
            .ifPresent { chat ->
                val chatMessages = messages
                    .mapNotNull { message ->

                        if (message == null) {
                            return@mapNotNull null;
                        }

                        val messageId = message.metadata?.get("id")

                        if (messageId != null) {
                            return@mapNotNull null;
                        }

                        val messageType = message.metadata?.get("messageType")
                        val chatMessageType = if (messageType != null) {
                            if (messageType == MessageType.USER) USER else ASSISTANT
                        } else {
                            ASSISTANT;
                        }

                        ChatMessage(
                            message = message.text,
                            time = System.currentTimeMillis(),
                            messageType = chatMessageType,
                            chat = chat
                        );
                    }.toList()

                chat.messages.addAll(chatMessages);
                messageRepository.saveAll(chatMessages)
                chatRepository.save(chat);
            }
    }

    override fun deleteByConversationId(conversationId: String) {
        chatRepository.deleteById(UUID.fromString(conversationId))
    }

}

class SupportLLMRequest(
    private val chatId: String,
    private val question: String,
    private val requestType: RequestType
) : LLMRequest {

    override fun getQuestion(): String = "${requestType.name}: $question"

    override fun getChatId(): String = chatId

    fun getRequestType(): RequestType = requestType

}

enum class RequestType {
    USER, SUPPORT
}

class DefaultLLMRequest(
    private val chat: Chat,
    private val question: String
) : LLMRequest {

    override fun getQuestion(): String {
        return question;
    }

    override fun getChatId(): String {
        return chat.id.toString();
    }

}

interface LLMRequest {
    fun getQuestion(): String;

    fun getChatId(): String
}

data class LLMAnswer(val answer: String?)