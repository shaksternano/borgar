package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.data.databaseConnection
import io.github.shaksternano.borgar.media.graphics.Position
import io.github.shaksternano.borgar.media.graphics.TextAlignment
import io.github.shaksternano.borgar.media.template.CustomTemplateInfo
import io.github.shaksternano.borgar.media.template.TemplateInfo
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Font

object TemplateRepository {

    private object TemplateTable : Table(name = "template") {
        val commandName = varchar("command_name", 100)

        // Either a guild ID or a user ID (for DMs)
        val entityId = long("entity_id")
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

        val isTemplateBackground = bool("is_template_background")
        val fillColor = integer("fill_color").nullable()

        override val primaryKey = PrimaryKey(commandName, entityId, name = "template_pk")
    }

    init {
        transaction(databaseConnection()) {
            SchemaUtils.create(TemplateTable)
        }
    }

    private suspend fun <T> dbQuery(block: suspend TemplateTable.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block(TemplateTable) }

    suspend fun create(template: TemplateInfo, commandName: String, mediaUrl: String, entityId: Long): Unit = dbQuery {
        insert {
            it[this.commandName] = commandName
            it[this.entityId] = entityId
            it[this.mediaUrl] = mediaUrl
            it[format] = template.format
            it[resultName] = template.resultName

            it[imageX] = template.imageContentX
            it[imageY] = template.imageContentX
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

            it[isTemplateBackground] = template.isBackground
            it[fillColor] = template.fill.orElse(null)?.rgb
        }
    }

    suspend fun read(commandName: String, entityId: Long): TemplateInfo? = dbQuery {
        select { (TemplateTable.commandName eq commandName) and (TemplateTable.entityId eq entityId) }
            .map {
                val mediaUrl = it[mediaUrl]
                CustomTemplateInfo(
                    mediaUrl,
                    it[format],
                    it[resultName],

                    it[imageX],
                    it[imageY],
                    it[imageWidth],
                    it[imageHeight],
                    it[imagePosition],

                    it[textX],
                    it[textY],
                    it[textWidth],
                    it[textHeight],
                    it[textPosition],
                    it[textAlignment],
                    Font(it[textFont], Font.PLAIN, it[textMaxSize]),
                    Color(it[textColorRgb]),

                    it[isTemplateBackground],
                    it[fillColor]?.let(::Color),
                )
            }.singleOrNull()
    }

    suspend fun exists(commandName: String, entityId: Long): Boolean = dbQuery {
        select { (TemplateTable.commandName eq commandName) and (TemplateTable.entityId eq entityId) }.any()
    }

    suspend fun delete(commandName: String, entityId: Long): Unit = dbQuery {
        deleteWhere { (TemplateTable.commandName eq commandName) and (TemplateTable.entityId eq entityId) }
    }
}
