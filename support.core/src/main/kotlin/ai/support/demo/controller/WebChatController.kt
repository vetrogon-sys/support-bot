package ai.support.demo.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebChatController {

    @GetMapping("/chat")
    fun chatPage(@RequestParam(required = false) chatId: String?,
                 model: Model
    ): String {
        model.addAttribute("chatId", chatId)
        return "chat"
    }

}