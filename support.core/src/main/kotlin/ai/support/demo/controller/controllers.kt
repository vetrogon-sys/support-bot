package ai.support.demo.controller

import ai.support.demo.dto.ChatDto
import ai.support.demo.service.ChatMessageService
import ai.support.demo.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {

    @GetMapping
    fun getAllChats() : ResponseEntity<List<ChatDto>> {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @PostMapping
    fun createChat() : ResponseEntity<ChatDto> {
        val chat = chatService.createEmptyChat()
        return ResponseEntity.created(URI.create("/${chat.id}"))
            .body(chat)
    }

}

@RestController
@RequestMapping("/api/chats/{chatId}/message")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {
    @PostMapping
    fun handleMessage(@PathVariable chatId : UUID,
                      @RequestBody chatMessage: ChatMessage): ResponseEntity<ChatDto> {

        return ResponseEntity.ok(chatMessageService.handleMessage(chatId, chatMessage.message))
    }
}

data class ChatMessage(val message: String)
