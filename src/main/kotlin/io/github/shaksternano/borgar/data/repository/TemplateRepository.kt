package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.data.databaseConnection
import io.github.shaksternano.borgar.media.graphics.Position
import io.github.shaksternano.borgar.media.graphics.TextAlignment
import io.github.shaksternano.borgar.media.template.CustomTemplate
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Font
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

object TemplateRepository {

    private object TemplateTable : Table(name = "template") {
        val commandName = varchar("command_name", 100)

        // Either a guild ID or a user ID (for DMs)
        val entityId = long("entity_id")

        val description = varchar("description", 1000)
        val mediaUrl = varchar("media_url", 2000)
        val format = varchar("format", 100)
        val resultName = varchar("result_name", 100)

        val imageX = integer("image_x")
        val imageY = integer("image_y")
        val imageWidth = integer("image_width")
        val imageHeight = integer("image_height")
        val imagePosition = enumeration("image_position", Position::class)

        val textX = integer("text_x")
        val textY = integer("text_y")
        val textWidth = integer("text_width")
        val textHeight = integer("text_height")
        val textPosition = enumeration("text_position", Position::class)
        val textAlignment = enumeration("text_alignment", TextAlignment::class)
        val textFont = varchar("text_font", 100)
        val textColorRgb = integer("text_color_rgb")
        val textMaxSize = integer("text_max_size")

        val rotationRadians = double("rotation_radians")
        val isTemplateBackground = bool("is_template_background")
        val fillColor = integer("fill_color").nullable()

        override val primaryKey = PrimaryKey(commandName, entityId, name = "template_pk")

        fun create(resultRow: ResultRow): CustomTemplate {
            return CustomTemplate(
                resultRow[commandName],
                resultRow[entityId],

                resultRow[description],
                resultRow[mediaUrl],
                resultRow[format],
                resultRow[resultName],

                resultRow[imageX],
                resultRow[imageY],
                resultRow[imageWidth],
                resultRow[imageHeight],
                resultRow[imagePosition],

                resultRow[textX],
                resultRow[textY],
                resultRow[textWidth],
                resultRow[textHeight],
                resultRow[textPosition],
                resultRow[textAlignment],
                Font.decode(resultRow[textFont]),
                Color(resultRow[textColorRgb]),

                resultRow[rotationRadians],
                resultRow[isTemplateBackground],
                resultRow[fillColor]?.let { Color(it) }
            )
        }
    }

    init {
        transaction(databaseConnection()) {
            SchemaUtils.create(TemplateTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend TemplateTable.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block(TemplateTable) }

    suspend fun create(template: CustomTemplate): Unit = dbQuery {
        insert {
            it[commandName] = template.commandName
            it[entityId] = template.entityId

            it[description] = template.description
            it[mediaUrl] = template.mediaUrl
            it[format] = template.format
            it[resultName] = template.resultName

            it[imageX] = template.imageContentX
            it[imageY] = template.imageContentY
            it[imageWidth] = template.imageContentWidth
            it[imageHeight] = template.imageContentHeight
            it[imagePosition] = template.imageContentPosition

            it[textX] = template.textContentX
            it[textY] = template.textContentY
            it[textWidth] = template.textContentWidth
            it[textHeight] = template.textContentHeight
            it[textPosition] = template.textContentPosition
            it[textAlignment] = template.textContentAlignment
            it[textFont] = template.font.name
            it[textColorRgb] = template.textColor.rgb
            it[textMaxSize] = template.font.size

            it[rotationRadians] = template.contentRotation
            it[isTemplateBackground] = template.isBackground
            it[fillColor] = template.fill.getOrNull()?.rgb
        }
    }

    suspend fun read(commandName: String, entityId: Long, vararg entityIds: Long): CustomTemplate? = dbQuery {
        select {
            val idEq = entityIds.fold(TemplateTable.entityId eq entityId) { expression, id ->
                expression or (TemplateTable.entityId eq id)
            }
            TemplateTable.commandName eq commandName and idEq
        }.map(TemplateTable::create).singleOrNull()
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun readFuture(
        commandName: String,
        entityId: Long,
        vararg entityIds: Long
    ): CompletableFuture<Optional<CustomTemplate>> = GlobalScope.future {
        Optional.ofNullable(read(commandName, entityId, *entityIds))
    }

    suspend fun readAll(entityId: Long): List<CustomTemplate> = dbQuery {
        select { TemplateTable.entityId eq entityId }.map(TemplateTable::create)
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun readAllFuture(entityId: Long): CompletableFuture<List<CustomTemplate>> = GlobalScope.future {
        readAll(entityId)
    }

    suspend fun exists(commandName: String, entityId: Long): Boolean = dbQuery {
        select { (TemplateTable.commandName eq commandName) and (TemplateTable.entityId eq entityId) }.any()
    }

    suspend fun delete(commandName: String, entityId: Long): Unit = dbQuery {
        deleteWhere { (TemplateTable.commandName eq commandName) and (TemplateTable.entityId eq entityId) }
    }
}
