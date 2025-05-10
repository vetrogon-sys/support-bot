package ai.support.telegrambot.commands

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand

object CommandRegistry {
    const val HELP = "/help"
    const val STATUS = "/status"
    const val ESCALATE = "/escalate"
    const val REMOVE = "/remove"
    val commands = listOf(
        BotCommand(HELP, "Show the list of available commands"),
        BotCommand(STATUS, "Check the status of a ticket"),
        BotCommand(ESCALATE, "Escalate your current ticket"),
        BotCommand(REMOVE, "Remove last chat information")
    )
}