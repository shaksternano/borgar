package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.core.command.util.CommandResponse
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
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
        val commandName = arguments.firstOrNull()?.lowercase() ?: return CommandResponse(
            "No template name provided!"
        )
        val entityId = if (event.isFromGuild) event.guild.id else event.author.id
        if (!TemplateRepository.exists(commandName, entityId)) {
            return CommandResponse("No template with the command name `$commandName` exists!")
        }
        TemplateRepository.delete(commandName, entityId)
        HelpCommand.removeCachedMessage(entityId)
        return CommandResponse("Template `$commandName` deleted!")
    }

    override fun requiredPermissions(): Set<Permission> = setOf(Permission.MANAGE_GUILD_EXPRESSIONS)
}
