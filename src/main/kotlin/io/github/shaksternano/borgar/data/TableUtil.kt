package io.github.shaksternano.borgar.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class VarcharIdTable(name: String = "", columnName: String = "id", length: Int) : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, length).entityId()
    final override val primaryKey = PrimaryKey(id)
}
