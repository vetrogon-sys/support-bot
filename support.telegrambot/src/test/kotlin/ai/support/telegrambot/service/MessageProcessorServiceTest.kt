package ai.support.telegrambot.service

import ai.support.telegrambot.model.EscalateResponse
import ai.support.telegrambot.model.MessageResponse
import ai.support.telegrambot.model.TicketResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

class MessageProcessorServiceTest {

    private lateinit var messageProcessor: MessageProcessorService
    private lateinit var mockWebClient: WebClient
    private lateinit var mockRequestBodyUriSpec: WebClient.RequestBodyUriSpec
    private lateinit var mockRequestHeadersSpec: WebClient.RequestHeadersSpec<*>
    private lateinit var mockResponseSpec: WebClient.ResponseSpec

    @BeforeEach
    fun setup() {
        mockWebClient = mockk()
        mockRequestBodyUriSpec = mockk()
        mockRequestHeadersSpec = mockk()
        mockResponseSpec = mockk()

        messageProcessor = MessageProcessorService(mockWebClient)
    }

    @Test
    fun `processTextMessage should return successful response`() = runBlocking {
        // Arrange
        val chatId = "123"
        val text = "Test message"
        val expectedResponse = MessageResponse("We've received your message and created ticket #54321")

        setupWebClientMockForPost("/api/messages", expectedResponse)

        // Act
        val result = messageProcessor.processTextMessage(chatId, text)

        // Assert
        assertEquals("We've received your message and created ticket #54321", result)
    }

    @Test
    fun `processTextMessage should handle error`() = runBlocking {
        // Arrange
        val chatId = "123"
        val text = "Test message"

        setupWebClientMockForPostWithError("/api/messages")

        // Act
        val result = messageProcessor.processTextMessage(chatId, text)

        // Assert
        assertEquals("We've encountered a problem processing your message. Please try again later.", result)
    }

    @Test
    fun `getTicketStatus should return formatted ticket response`() = runBlocking {
        // Arrange
        val ticketId = "12345"
        val ticketResponse = TicketResponse(
            id = "12345",
            status = "Open",
            createdAt = "2023-01-01",
            lastUpdated = "2023-01-02",
            description = "Test ticket"
        )

        setupWebClientMockForGet("/api/tickets/$ticketId", ticketResponse)

        // Act
        val result = messageProcessor.getTicketStatus(ticketId)

        // Assert
        assertEquals(
            """
            Ticket #12345
            Status: Open
            Created: 2023-01-01
            Last Updated: 2023-01-02
            
            Description: Test ticket
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `getTicketStatus should handle error`() = runBlocking {
        // Arrange
        val ticketId = "12345"

        setupWebClientMockForGetWithError("/api/tickets/$ticketId")

        // Act
        val result = messageProcessor.getTicketStatus(ticketId)

        // Assert
        assertEquals("Unable to retrieve the status for ticket #12345. Please verify the ticket ID and try again.", result)
    }

    @Test
    fun `escalateTicket should return success message`() = runBlocking {
        // Arrange
        val chatId = "123"
        val expectedResponse = EscalateResponse("Your ticket has been escalated to our senior support team.")

        setupWebClientMockForPost("/api/tickets/escalate", expectedResponse)

        // Act
        val result = messageProcessor.escalateTicket(chatId)

        // Assert
        assertEquals("Your ticket has been escalated to our senior support team.", result)
    }

    @Test
    fun `escalateTicket should handle error`() = runBlocking {
        // Arrange
        val chatId = "123"

        setupWebClientMockForPostWithError("/api/tickets/escalate")

        // Act
        val result = messageProcessor.escalateTicket(chatId)

        // Assert
        assertEquals("We encountered a problem while trying to escalate your ticket. Please try again later.", result)
    }

    private fun <T> setupWebClientMockForPost(uri: String, response: T) {
        every { mockWebClient.post() } returns mockRequestBodyUriSpec
        every { mockRequestBodyUriSpec.uri(uri) } returns mockRequestBodyUriSpec
        every { mockRequestBodyUriSpec.bodyValue(any()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
//        every { mockResponseSpec.awaitBody<T>() } returns response
    }

    private fun setupWebClientMockForPostWithError(uri: String) {
        every { mockWebClient.post() } returns mockRequestBodyUriSpec
        every { mockRequestBodyUriSpec.uri(uri) } returns mockRequestBodyUriSpec
        every { mockRequestBodyUriSpec.bodyValue(any()) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
//        every { mockResponseSpec.awaitBody<Any>() } throws WebClientResponseException(500, "Server Error", null, null, null, null)
    }

    private fun <T> setupWebClientMockForGet(uri: String, response: T) {
        every { mockWebClient.get() } returns mockRequestBodyUriSpec
//        every { mockRequestBodyUriSpec.uri(uri) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
//        every { mockResponseSpec.awaitBody<T>() } returns response
    }

    private fun setupWebClientMockForGetWithError(uri: String) {
        every { mockWebClient.get() } returns mockRequestBodyUriSpec
//        every { mockRequestBodyUriSpec.uri(uri) } returns mockRequestHeadersSpec
        every { mockRequestHeadersSpec.retrieve() } returns mockResponseSpec
//        every { mockResponseSpec.awaitBody<Any>() } throws WebClientResponseException(404, "Not Found", null, null, null, null)
    }
}