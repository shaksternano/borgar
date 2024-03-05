package io.github.shaksternano.borgar.chat.util

enum class ChannelEnvironment {
    GUILD,
    DIRECT_MESSAGE,
    GROUP,
    ;

    companion object {
        val ALL: Set<ChannelEnvironment> = entries.toSet()
    }
}
