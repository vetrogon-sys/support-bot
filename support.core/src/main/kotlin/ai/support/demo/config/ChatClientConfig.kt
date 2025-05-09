package ai.support.demo.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Value("\${ollama.url}")
    lateinit var baseUrl: String;

    @Value("\${ollama.model.chat}")
    lateinit var chatModel: String;

    @Value("\${ollama.model.embedding}")
    lateinit var embeddingModel: String;

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.create(chatModel)
    }

    @Bean
    fun embeddingModel(api: OllamaApi): EmbeddingModel {
        return OllamaEmbeddingModel.builder()
            .ollamaApi(api)
            .defaultOptions(
                OllamaOptions.builder()
                    .model(embeddingModel)
                    .truncate(false)
                    .build()
            )
            .build()
    }

    @Bean
    fun chatModel(api: OllamaApi): ChatModel {
        return OllamaChatModel.builder()
            .ollamaApi(api)
            .defaultOptions(
                OllamaOptions.builder()
                    .model(chatModel)
                    .temperature(0.9)
                    .build()
            )
            .build();
    }

    @Bean
    fun modelApi(): OllamaApi {
        return OllamaApi.builder()
            .baseUrl(baseUrl)
            .build();
    }

}