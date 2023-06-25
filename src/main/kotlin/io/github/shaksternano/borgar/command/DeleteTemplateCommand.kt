package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.github.shaksternano.borgar.data.repository.TemplateRepository
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object DeleteTemplateCommand : KotlinCommand<Unit>(
    "rmtemplate",
    "Deletes a custom image template for this guild.",
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        val commandName = arguments.firstOrNull() ?: return CommandResponse("No template name provided!")
        val guildId = if (event.isFromGuild) event.guild.idLong else event.author.idLong
        if (!TemplateRepository.exists(commandName, guildId)) {
            return CommandResponse("No template with the command name `$commandName` exists!")
        }
        TemplateRepository.delete(commandName, guildId)
        return CommandResponse("Template `$commandName` deleted!")
    }

    override fun requiredPermissions(): Set<Permission> = setOf(Permission.MANAGE_GUILD_EXPRESSIONS)
}
