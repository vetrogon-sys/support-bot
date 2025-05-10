package ai.support.telegrambot.service

import ai.support.telegrambot.commands.CommandRegistry
import ai.support.telegrambot.config.BotConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Service
class SupportBotService(
    private val botConfig: BotConfig,
    private val messageProcessor: MessageProcessorService
) : TelegramLongPollingBot(botConfig.token), ApplicationRunner {

    private val logger = LoggerFactory.getLogger(SupportBotService::class.java)
    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    @PostConstruct
    fun registerBotCommands() {
        try {
            val setCommands = SetMyCommands().apply {
                commands = CommandRegistry.commands
            }
            this.execute(setCommands)
        } catch (e: TelegramApiException) {
            logger.warn("Failed to register commands: ${e.message}")
        }
    }

    override fun getBotUsername(): String = botConfig.username

    override fun onUpdateReceived(update: Update) {
        coroutineScope.launch {
            try {
                logger.info("Received update with ID: ${update.updateId}")

                if (update.hasMessage()) {
                    val message = update.message
                    val chatId = message.chatId.toString()

                    when {
                        message.hasText() -> {
                            val text = message.text

                            when {
                                text.startsWith(CommandRegistry.HELP) -> {
                                    sendResponse(chatId, getHelpMessage())
                                }

                                text.startsWith(CommandRegistry.STATUS) -> {
                                    val ticketId = text.substringAfter("/status").trim()
                                    if (ticketId.isBlank()) {
                                        sendResponse(
                                            chatId,
                                            "Please provide a ticket ID with the status command. For example: /status 12345"
                                        )
                                    } else {
                                        val statusResponse =
                                            messageProcessor.getTicketStatus(ticketId)
                                        sendResponse(chatId, statusResponse)
                                    }
                                }

                                text.startsWith(CommandRegistry.ESCALATE) -> {
                                    val escalateResponse = messageProcessor.escalateTicket(chatId)
                                    sendResponse(chatId, escalateResponse)
                                }

                                text.startsWith("/") -> {
                                    sendResponse(
                                        chatId,
                                        "Unknown command. Type /help for available commands."
                                    )
                                }

                                else -> {
                                    val response = messageProcessor.processTextMessage(chatId, text)
                                    sendResponse(chatId, response)
                                }
                            }
                        }

                        else -> {
                            sendResponse(
                                update.message.chatId.toString(),
                                "Unsupported message type"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing update", e)
            }
        }
    }

    private fun sendResponse(chatId: String, text: String) {
        try {
            val message = SendMessage(chatId, text)
            message.parseMode = "Markdown"
            execute(message)
            logger.info("Response sent to chat ID: $chatId")
        } catch (e: TelegramApiException) {
            logger.error("Error sending message to chat ID: $chatId", e)
        }
    }

    private fun getHelpMessage(): String {
        return """
            Available commands:
            /help - Show this help message
            /status <ticket_id> - Check the status of a ticket
            /escalate - Escalate your current ticket to a higher support tier
            
            You can also send any text message to create a new support ticket.
        """.trimIndent()
    }

    override fun onClosing() {
        super.onClosing()
        job.cancel()
    }

    override fun run(args: ApplicationArguments?) {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(this)
    }
}