package io.github.shaksternano.borgar.revolt.entity

import io.github.shaksternano.borgar.revolt.RevoltManager

const val SYSTEM_USER_ID: String = "00000000000000000000000000"

class RevoltSystemUser(manager: RevoltManager) : RevoltUser(
    manager = manager,
    id = "00000000000000000000000000",
    name = "System",
    effectiveName = "System",
    effectiveAvatarUrl = "https://autumn.revolt.chat/attachments/7HzJPSqop6nEMrnlH3tpqiWe31gX8pmeQxiUxkGxPn/revolt.png",
    isBot = false,
)
