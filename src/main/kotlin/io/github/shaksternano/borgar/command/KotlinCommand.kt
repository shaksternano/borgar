package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import io.github.shaksternano.borgar.command.util.CommandResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.concurrent.CompletableFuture

abstract class KotlinCommand<T>(name: String, description: String) : BaseCommand<T>(name, description) {

    @OptIn(DelicateCoroutinesApi::class)
    final override fun execute(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CompletableFuture<CommandResponse<T>> = GlobalScope.future {
        executeSuspend(arguments, extraArguments, event)
    }

    protected abstract suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<T>
}
