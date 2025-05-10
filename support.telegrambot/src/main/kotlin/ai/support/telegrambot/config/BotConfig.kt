package ai.support.telegrambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
data class BotConfig(
    var username: String = "",
    var token: String = ""
)