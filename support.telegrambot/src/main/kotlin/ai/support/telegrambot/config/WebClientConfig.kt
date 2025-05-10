package ai.support.telegrambot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(private val backendConfig: BackendConfig) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(backendConfig.baseUrl)
            .build()
    }
}