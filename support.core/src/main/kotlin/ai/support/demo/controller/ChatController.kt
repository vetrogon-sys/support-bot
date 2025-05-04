package ai.support.demo.controller

import ai.support.demo.service.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun chat(@RequestBody chatMessage: ChatMessage): ResponseEntity<String> {
        return ResponseEntity.ok(chatService.getAnswer(chatMessage.question))
    }

    data class ChatMessage(val question: String)
}