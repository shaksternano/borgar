package io.github.shaksternano.borgar.messaging.entity

abstract class BaseEntity : Entity {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Entity) return id == other.id
        return false
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName ?: "Entity"}(id='$id')"
    }
}
