package io.github.shaksternano.borgar.core.util

enum class ChannelEnvironment(
    override val displayName: String,
) : Displayed {
    GUILD("Guild"),
    DIRECT_MESSAGE("Direct Message"),
    GROUP("Group"),
    ;

    companion object {
        val ALL: Set<ChannelEnvironment> = entries.toSet()
    }
}
