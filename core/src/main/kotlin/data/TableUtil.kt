package com.shakster.borgar.core.data

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

open class VarcharIdTable(name: String = "", columnName: String = "id", length: Int) : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(columnName, length).entityId()
    final override val primaryKey = PrimaryKey(id)
}
