package ai.support.telegrambot.service

import ai.support.telegrambot.config.BotConfig
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

class SupportBotServiceTest {

    private lateinit var supportBotService: SupportBotService
    private lateinit var mockMessageProcessor: MessageProcessorService
    private val botConfig = BotConfig(username = "test_bot", token = "test_token")

    @BeforeEach
    fun setup() {
        mockMessageProcessor = mockk()

        supportBotService = spyk(
            SupportBotService(botConfig, mockMessageProcessor),
            recordPrivateCalls = true
        )

        // Mock execute method to prevent actual API calls
        every { supportBotService.execute(any<SendMessage>()) } returns mockk()
    }

    @Test
    fun `test help command handling`() {
        // Arrange
        val update = createUpdateWithText("/help")

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text.contains("Available commands")
            })
        }
    }

    @Test
    fun `test status command with ticket id`() = runBlocking {
        // Arrange
        val update = createUpdateWithText("/status 12345")
        val expectedResponse = "Ticket #12345\nStatus: Open\nCreated: 2023-01-01\nLast Updated: 2023-01-02\n\nDescription: Test ticket"

        coEvery { mockMessageProcessor.getTicketStatus("12345") } returns expectedResponse

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text == expectedResponse
            })
        }
    }

    @Test
    fun `test status command without ticket id`() {
        // Arrange
        val update = createUpdateWithText("/status ")

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text.contains("Please provide a ticket ID")
            })
        }
    }

    @Test
    fun `test escalate command`() = runBlocking {
        // Arrange
        val update = createUpdateWithText("/escalate")
        val expectedResponse = "Your ticket has been escalated to our senior support team."

        coEvery { mockMessageProcessor.escalateTicket("123") } returns expectedResponse

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text == expectedResponse
            })
        }
    }

    @Test
    fun `test regular text message`() = runBlocking {
        // Arrange
        val update = createUpdateWithText("I need help with my account")
        val expectedResponse = "We've received your message and created ticket #54321"

        coEvery {
            mockMessageProcessor.processTextMessage("123", "I need help with my account")
        } returns expectedResponse

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text == expectedResponse
            })
        }
    }

    @Test
    fun `test non-text message`() {
        // Arrange
        val update = createUpdateWithNonTextMessage()

        // Act
        supportBotService.onUpdateReceived(update)

        // Assert
        verify(timeout = 1000) {
            supportBotService.execute(match<SendMessage> {
                it.chatId == "123" && it.text == "Unsupported message type"
            })
        }
    }

    private fun createUpdateWithText(text: String): Update {
        val update = Update()
        val message = Message()
        val chat = Chat()

        chat.id = 123L
        message.chat = chat
        message.text = text
        update.message = message

        return update
    }

    private fun createUpdateWithNonTextMessage(): Update {
        val update = Update()
        val message = Message()
        val chat = Chat()

        chat.id = 123L
        message.chat = chat
        // Set a photo list but no text to simulate a photo message
        message.photo = listOf(mockk())
        update.message = message

        return update
    }
}