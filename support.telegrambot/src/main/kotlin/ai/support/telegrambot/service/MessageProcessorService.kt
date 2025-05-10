package ai.support.telegrambot.service

import ai.support.telegrambot.model.ChatCreationResponse
import ai.support.telegrambot.model.ChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class MessageProcessorService(private val webClient: WebClient) {

    private val logger = LoggerFactory.getLogger(MessageProcessorService::class.java)

    // Map to store chat IDs and their associated ticketIds/conversations
    private val chatTicketMap = mutableMapOf<String, String>()

    suspend fun processTextMessage(chatId: String, text: String): String {
        logger.info("Processing text message from chat ID: $chatId")

        return try {
            withContext(Dispatchers.IO) {
                // If this is a new chat, create one first
                if (!chatTicketMap.containsKey(chatId)) {
                    val chatResponse = webClient.post()
                        .uri("/api/support/chats")
                        .bodyValue(mapOf<String, String>())
                        .retrieve()
                        .awaitBody<ChatCreationResponse>()

                    chatTicketMap[chatId] = chatResponse.id
                    logger.info("Created new chat with ID: ${chatResponse.id} for Telegram chat ID: $chatId")
                }

                val ticketId = chatTicketMap[chatId]!!

                // Send the message to the chat
                val response = webClient.post()
                    .uri("/api/support/chats/$ticketId/messages")
                    .bodyValue(mapOf("message" to text))
                    .retrieve()
                    .awaitBody<ChatResponse>()

                // Find the most recent assistant response
                val assistantResponse = response.messages
                    .filter { it.messageType == "ASSISTANT" }
                    .maxByOrNull { it.time }

                assistantResponse?.message ?: "Your message has been received. A support agent will respond shortly."
            }
        } catch (e: Exception) {
            logger.error("Error forwarding message to backend", e)
            "We've encountered a problem processing your message. Please try again later."
        }
    }

    suspend fun getTicketStatus(ticketId: String): String {
        logger.info("Getting status for ticket ID: $ticketId")

        return try {
            withContext(Dispatchers.IO) {
                val response = webClient.get()
                    .uri("/api/support/chats/$ticketId")
                    .retrieve()
                    .awaitBody<ChatResponse>()

                // Format the chat information for display
                formatChatStatus(ticketId, response)
            }
        } catch (e: Exception) {
            logger.error("Error fetching ticket status", e)
            "Unable to retrieve the status for ticket #$ticketId. Please verify the ticket ID and try again."
        }
    }

    suspend fun escalateTicket(chatId: String): String {
        logger.info("Escalating ticket for chat ID: $chatId")

        return try {
            withContext(Dispatchers.IO) {
                val ticketId = chatTicketMap[chatId]

                if (ticketId == null) {
                    return@withContext "You don't have an active support ticket to escalate. Please send a message first."
                }

                // For escalation, we'll send a special message to the chat
                webClient.post()
                    .uri("/api/support/chats/$ticketId/messages")
                    .bodyValue(mapOf("message" to "[ESCALATE] This ticket requires urgent attention."))
                    .retrieve()
                    .awaitBody<ChatResponse>()

                "Your ticket has been escalated to our senior support team. They will respond as soon as possible."
            }
        } catch (e: Exception) {
            logger.error("Error escalating ticket", e)
            "We encountered a problem while trying to escalate your ticket. Please try again later."
        }
    }

    private fun formatChatStatus(chatId: String, chatResponse: ChatResponse): String {
        val messages = chatResponse.messages
        val messageCount = messages.size
        val lastUpdated = messages.maxByOrNull { it.time }?.let {
            java.time.Instant.ofEpochMilli(it.time).toString()
        } ?: "Unknown"

        val userMessageCount = messages.count { it.messageType == "USER" }
        val assistantMessageCount = messages.count { it.messageType == "ASSISTANT" }

        return """
            Ticket #$chatId
            Status: ${if (assistantMessageCount > 0) "Active" else "Pending"}
            Messages: $messageCount ($userMessageCount from you, $assistantMessageCount from support)
            Last Updated: $lastUpdated
            
            Last message: ${messages.maxByOrNull { it.time }?.message ?: "No messages yet"}
        """.trimIndent()
    }
}