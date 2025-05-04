package ai.support.demo.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Bean
    fun chatClient() : ChatClient {

        val api = OllamaApi.builder()
            .baseUrl("http://localhost:11434")
            .build()
        val chatModel = OllamaChatModel.builder()
            .ollamaApi(api)
            .defaultOptions(
                OllamaOptions.builder()
                    .model(OllamaModel.LLAMA3_2_1B)
                    .temperature(0.9)
                    .build()
            )
            .build();

        return ChatClient.create(chatModel)
    }

}