package com.shakster.borgar.stoat.entity.channel

enum class StoatChannelType(
    val apiName: String,
) {
    SAVED_MESSAGES("SavedMessages"),
    DIRECT_MESSAGE("DirectMessage"),
    GROUP("Group"),
    TEXT("TextChannel"),
    VOICE("VoiceChannel"),
    UNKNOWN("Unknown"),
    ;

    companion object {
        fun fromApiName(apiName: String): StoatChannelType =
            entries.find { it.apiName == apiName } ?: UNKNOWN
    }

    fun isMessage(): Boolean =
        this == DIRECT_MESSAGE
            || this == GROUP
            || this == TEXT
}
