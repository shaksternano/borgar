package io.github.shaksternano.borgar.discord.data.repository

import io.github.shaksternano.borgar.discord.media.graphics.Position
import io.github.shaksternano.borgar.discord.media.graphics.TextAlignment
import io.github.shaksternano.borgar.discord.media.template.CustomTemplate
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.awt.Color
import java.awt.Font
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

object TemplateRepository : Repository<TemplateRepository.TemplateTable>() {

    object TemplateTable : Table(name = "template") {
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
        val imagePosition = enumeration<Position>("image_position")

        val textX = integer("text_x")
        val textY = integer("text_y")
        val textWidth = integer("text_width")
        val textHeight = integer("text_height")
        val textPosition = enumeration<Position>("text_position")
        val textAlignment = enumeration<TextAlignment>("text_alignment")
        val textFont = varchar("text_font", 100)
        val textColorRgb = integer("text_color_rgb")
        val textMaxSize = integer("text_max_size")

        val rotationRadians = double("rotation_radians")
        val isTemplateBackground = bool("is_template_background")
        val fillColor = integer("fill_color").nullable()

        override val primaryKey = PrimaryKey(commandName, entityId, name = "template_pk")
    }

    override fun table(): TemplateTable = TemplateTable

    private fun ResultRow.read(): CustomTemplate = CustomTemplate(
        this[TemplateTable.commandName],
        this[TemplateTable.entityId],

        this[TemplateTable.description],
        this[TemplateTable.mediaUrl],
        this[TemplateTable.format],
        this[TemplateTable.resultName],

        this[TemplateTable.imageX],
        this[TemplateTable.imageY],
        this[TemplateTable.imageWidth],
        this[TemplateTable.imageHeight],
        this[TemplateTable.imagePosition],

        this[TemplateTable.textX],
        this[TemplateTable.textY],
        this[TemplateTable.textWidth],
        this[TemplateTable.textHeight],
        this[TemplateTable.textPosition],
        this[TemplateTable.textAlignment],
        Font(this[TemplateTable.textFont], Font.PLAIN, this[TemplateTable.textMaxSize]),
        Color(this[TemplateTable.textColorRgb]),

        this[TemplateTable.rotationRadians],
        this[TemplateTable.isTemplateBackground],
        this[TemplateTable.fillColor]?.let { Color(it) }
    )

    private fun UpdateBuilder<*>.write(template: CustomTemplate) {
        this[TemplateTable.commandName] = template.commandName
        this[TemplateTable.entityId] = template.entityId

        this[TemplateTable.description] = template.description
        this[TemplateTable.mediaUrl] = template.mediaUrl
        this[TemplateTable.format] = template.format
        this[TemplateTable.resultName] = template.resultName

        this[TemplateTable.imageX] = template.imageContentX
        this[TemplateTable.imageY] = template.imageContentY
        this[TemplateTable.imageWidth] = template.imageContentWidth
        this[TemplateTable.imageHeight] = template.imageContentHeight
        this[TemplateTable.imagePosition] = template.imageContentPosition

        this[TemplateTable.textX] = template.textContentX
        this[TemplateTable.textY] = template.textContentY
        this[TemplateTable.textWidth] = template.textContentWidth
        this[TemplateTable.textHeight] = template.textContentHeight
        this[TemplateTable.textPosition] = template.textContentPosition
        this[TemplateTable.textAlignment] = template.textContentAlignment
        this[TemplateTable.textFont] = template.font.name
        this[TemplateTable.textColorRgb] = template.textColor.rgb
        this[TemplateTable.textMaxSize] = template.font.size

        this[TemplateTable.rotationRadians] = template.contentRotation
        this[TemplateTable.isTemplateBackground] = template.isBackground
        this[TemplateTable.fillColor] = template.fill.getOrNull()?.rgb
    }

    suspend fun create(template: CustomTemplate): Unit = dbQuery {
        insert {
            it.write(template)
        }
    }

    suspend fun read(commandName: String, entityId: Long): CustomTemplate? = dbQuery {
        select {
            TemplateTable.commandName eq commandName and (TemplateTable.entityId eq entityId)
        }.map {
            it.read()
        }.firstOrNull()
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun readFuture(
        commandName: String,
        entityId: Long,
    ): CompletableFuture<Optional<CustomTemplate>> = GlobalScope.future {
        Optional.ofNullable(read(commandName, entityId))
    }

    suspend fun readAll(entityId: Long): List<CustomTemplate> = dbQuery {
        select {
            TemplateTable.entityId eq entityId
        }.map {
            it.read()
        }
    }

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun readAllFuture(entityId: Long): CompletableFuture<List<CustomTemplate>> = GlobalScope.future {
        readAll(entityId)
    }

    suspend fun exists(commandName: String, entityId: Long): Boolean = dbQuery {
        select { TemplateTable.commandName eq commandName and (TemplateTable.entityId eq entityId) }.any()
    }

    suspend fun delete(commandName: String, entityId: Long): Unit = dbQuery {
        deleteWhere { TemplateTable.commandName eq commandName and (TemplateTable.entityId eq entityId) }
    }
}
