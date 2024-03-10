package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.messaging.event.CommandEvent

object DeleteTemplateCommand : NonChainableCommand() {

    override val name: String = "removetemplate"
    override val description: String = "Deletes a custom image template for this server or DM."
    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "template",
            description = "The name of the template to delete.",
            type = CommandArgumentType.String,
            required = true,
        )
    )
    override val requiredPermissions: Set<Permission> = setOf(Permission.MANAGE_GUILD_EXPRESSIONS)
    override val deferReply: Boolean = true
    override val ephemeralReply: Boolean = true

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> {
        val commandName = arguments.getRequired("template", CommandArgumentType.String).lowercase()
        val guild = event.getGuild()
        val entityId = guild?.id ?: event.getAuthor().id
        if (!TemplateRepository.exists(commandName, entityId)) {
            return CommandResponse("No template with the command name **$commandName** exists!").asSingletonList()
        }
        TemplateRepository.delete(commandName, entityId)
        HelpCommand.removeCachedMessage(entityId)
        guild?.deleteCommand(commandName)
        return CommandResponse("Template **$commandName** deleted!").asSingletonList()
    }
}
