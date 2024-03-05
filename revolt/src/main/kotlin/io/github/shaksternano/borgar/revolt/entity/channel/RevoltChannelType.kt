package io.github.shaksternano.borgar.revolt.entity.channel

enum class RevoltChannelType(
    val apiName: String,
) {
    DIRECT_MESSAGE("DirectMessage"),
    GROUP("Group"),
    TEXT("TextChannel"),
    VOICE("VoiceChannel"),
    UNKNOWN("Unknown"),
    ;

    companion object {
        fun fromApiName(apiName: String): RevoltChannelType =
            entries.find { it.apiName == apiName } ?: UNKNOWN
    }

    fun isMessage(): Boolean =
        this == DIRECT_MESSAGE
            || this == GROUP
            || this == TEXT
}
