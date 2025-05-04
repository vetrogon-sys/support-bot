package ai.support.demo.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val chatClient: ChatClient,
    private val contextBuilder: ContextBuilder
) {

    fun getAnswer(question: String): String? {
        val context = contextBuilder.buildContext(question);
        val prompt = buildPrompt(question, context)
        val response = chatClient.prompt(prompt)
        return response.call().content()
    }

    private fun buildPrompt(question: String, context: String): String {
        return """
            You are a helpful support assistant.
            Use the following knowledge base context to answer the question.

            Context:
            $context

            Question:
            $question
        """.trimIndent()
    }

}