package io.github.shaksternano.borgar.messaging.entity

abstract class BaseEntity : Entity {

    override fun equals(other: Any?): Boolean {
        return if (this === other) true
        else if (other is Entity) id == other.id
        else false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "${this::class.simpleName ?: "Entity"}(id=\"$id\", name=\"$name\")"
    }
}
