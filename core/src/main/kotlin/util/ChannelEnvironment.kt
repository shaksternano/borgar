package io.github.shaksternano.borgar.core.util

enum class ChannelEnvironment(
    val displayName: String,
    val entityType: String,
) {
    GUILD("Guild", "guild"),

    /**
     * Direct message with this bot.
     */
    DIRECT_MESSAGE("Direct Message", "user"),

    /**
     * Direct message between two other users, not with this bot.
     */
    PRIVATE("Direct Message", "private"),
    GROUP("Group", "group"),
    ;

    companion object {
        val ALL: Set<ChannelEnvironment> = entries.toSet()

        fun fromEntityType(entityType: String): ChannelEnvironment? =
            entries.find { it.entityType == entityType }
    }
}
