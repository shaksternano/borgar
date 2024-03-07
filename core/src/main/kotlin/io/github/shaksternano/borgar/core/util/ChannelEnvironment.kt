package io.github.shaksternano.borgar.core.util

enum class ChannelEnvironment(
    override val displayName: String,
    val entityType: String,
) : Displayed {
    GUILD("Guild", "guild"),
    GROUP("Group", "group"),
    DIRECT_MESSAGE("Direct Message", "user"),
    ;

    companion object {
        val ALL: Set<ChannelEnvironment> = entries.toSet()

        fun fromEntityType(entityType: String): ChannelEnvironment? =
            entries.find { it.entityType == entityType }
    }
}
