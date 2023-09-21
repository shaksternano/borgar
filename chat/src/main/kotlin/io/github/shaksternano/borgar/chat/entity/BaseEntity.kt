package io.github.shaksternano.borgar.chat.entity

abstract class BaseEntity : Entity {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Entity) return id == other.id
        return false
    }

    override fun hashCode(): Int = id.hashCode()
}