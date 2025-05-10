package ai.support.telegrambot.integration

import ai.support.telegrambot.service.MessageProcessorService
import ai.support.telegrambot.service.SupportBotService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.telegram.telegrambots.meta.api.objects.Update

@SpringBootTest
@AutoConfigureMockMvc
class TelegramBotIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMessageProcessor: MessageProcessorService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockMessageProcessor() = mockk<MessageProcessorService>(relaxed = true)

        // Mock the bot service to prevent actual Telegram API calls
        @Bean
        @Primary
        fun mockSupportBotService(mockMessageProcessor: MessageProcessorService) = mockk<SupportBotService>(relaxed = true)
    }

    @BeforeEach
    fun setup() {
        coEvery { mockMessageProcessor.processTextMessage(any(), any()) } returns "Test response"
        coEvery { mockMessageProcessor.getTicketStatus(any()) } returns "Test ticket status"
        coEvery { mockMessageProcessor.escalateTicket(any()) } returns "Test escalation response"
    }

    @Test
    fun `test simulated Telegram update with text message`() {
        // Create a sample Update object that simulates a Telegram message
        val updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "chat": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser",
                        "type": "private"
                    },
                    "date": 1609459200,
                    "text": "Hello, bot!"
                }
            }
        """.trimIndent()

        // Simulate an update from Telegram
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun `test simulated Telegram update with help command`() {
        // Create a sample Update object that simulates a Telegram command
        val updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "chat": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser",
                        "type": "private"
                    },
                    "date": 1609459200,
                    "text": "/help"
                }
            }
        """.trimIndent()

        // Simulate an update from Telegram
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun `test simulated Telegram update with status command`() {
        // Create a sample Update object that simulates a Telegram command
        val updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "chat": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser",
                        "type": "private"
                    },
                    "date": 1609459200,
                    "text": "/status 12345"
                }
            }
        """.trimIndent()

        // Simulate an update from Telegram
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun `test simulated Telegram update with non-text message`() {
        // Create a sample Update object that simulates a Telegram photo message
        val updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "chat": {
                        "id": 123456,
                        "first_name": "Test",
                        "username": "testuser",
                        "type": "private"
                    },
                    "date": 1609459200,
                    "photo": [
                        {
                            "file_id": "test_file_id",
                            "file_size": 1234,
                            "width": 100,
                            "height": 100
                        }
                    ]
                }
            }
        """.trimIndent()

        // Simulate an update from Telegram
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        ).andExpect(status().isOk)
    }
}