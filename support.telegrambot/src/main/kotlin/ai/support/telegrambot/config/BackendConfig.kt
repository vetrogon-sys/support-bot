package ai.support.telegrambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "backend.api")
data class BackendConfig(
    var baseUrl: String = ""
)