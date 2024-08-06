package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.messaging.BotManager

sealed interface CommandAutoCompleteHandler<T> {

    suspend fun handleAutoComplete(
        command: kotlin.String,
        argument: kotlin.String,
        currentValue: T,
        manager: BotManager,
    ): List<T>

    fun interface Long : CommandAutoCompleteHandler<kotlin.Long>

    fun interface Double : CommandAutoCompleteHandler<kotlin.Double>

    fun interface String : CommandAutoCompleteHandler<kotlin.String>
}
