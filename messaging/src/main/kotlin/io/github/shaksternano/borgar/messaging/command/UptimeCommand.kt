package io.github.shaksternano.borgar.messaging.command

import io.github.shaksternano.borgar.core.START_TIME
import io.github.shaksternano.borgar.messaging.event.CommandEvent
import kotlin.time.TimeSource

object UptimeCommand : NonChainableCommand() {

    override val name: String = "uptime"
    override val description: String = "Gets the uptime of this bot."

    override suspend fun run(arguments: CommandArguments, event: CommandEvent): List<CommandResponse> {
        val time = TimeSource.Monotonic.markNow()
        val uptime = time - START_TIME
        val uptimeString = uptime.toComponents { days, hours, minutes, seconds, _ ->
            if (days > 0) {
                if (days == 1L) {
                    "1 day"
                } else {
                    "$days days"
                }
            } else if (hours > 0) {
                if (hours == 1) {
                    "1 hour"
                } else {
                    "$hours hours"
                }
            } else if (minutes > 0) {
                if (minutes == 1) {
                    "1 minute"
                } else {
                    "$minutes minutes"
                }
            } else {
                if (seconds == 1) {
                    "1 second"
                } else {
                    "$seconds seconds"
                }
            }
        }
        val message = "This bot has been up for $uptimeString."
        return listOf(CommandResponse(message))
    }
}
