package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object TulinCommand : SimpleCommand(
    "tulin",
    "Tulin",
) {

    override fun response(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): String = "https://media.discordapp.net/attachments/964551969509347331/1119756264633798729/BotW_Tulin_Model.gif"
}
