package com.shakster.borgar.revolt.websocket

@Suppress("unused")
enum class WebSocketMessageType(
    val apiName: String,
) {
    READY("Ready"),
    AUTHENTICATED("Authenticated"),
    NOT_FOUND("NotFound"),
    MESSAGE("Message"),
    SERVER_CREATE("ServerCreate"),
    SERVER_DELETE("ServerDelete"),
    SERVER_MEMBER_LEAVE("ServerMemberLeave"),
    CHANNEL_CREATE("ChannelCreate"),
    CHANNEL_GROUP_LEAVE("ChannelGroupLeave"),
}
