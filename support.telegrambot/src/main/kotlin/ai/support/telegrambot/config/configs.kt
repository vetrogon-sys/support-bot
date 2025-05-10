package ai.support.telegrambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConfigurationProperties(prefix = "backend.api")
data class BackendConfig(
    var baseUrl: String = ""
)

@Configuration
@ConfigurationProperties(prefix = "bot")
data class BotConfig(
    var username: String = "",
    var token: String = ""
)

@Configuration
class WebClientConfig(private val backendConfig: BackendConfig) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(backendConfig.baseUrl)
            .build()
    }
}