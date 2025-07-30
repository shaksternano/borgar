package com.shakster.borgar.messaging.entity

interface Entity : Managed {

    val id: String
    val name: String?
}
