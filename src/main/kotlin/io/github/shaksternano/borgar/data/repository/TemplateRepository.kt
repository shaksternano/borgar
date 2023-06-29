package io.github.shaksternano.borgar.data.repository

import io.github.shaksternano.borgar.media.graphics.Position
import io.github.shaksternano.borgar.media.graphics.TextAlignment
import io.github.shaksternano.borgar.media.template.CustomTemplate
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
    }

    override fun table(): TemplateTable = TemplateTable

    private fun ResultRow.read(): CustomTemplate = CustomTemplate(
        this[table().commandName],
        this[table().entityId],

        this[table().description],
        this[table().mediaUrl],
        this[table().format],
        this[table().resultName],

        this[table().imageX],
        this[table().imageY],
        this[table().imageWidth],
        this[table().imageHeight],
        this[table().imagePosition],

        this[table().textX],
        this[table().textY],
        this[table().textWidth],
        this[table().textHeight],
        this[table().textPosition],
        this[table().textAlignment],
        Font(this[table().textFont], Font.PLAIN, this[table().textMaxSize]),
        Color(this[table().textColorRgb]),

        this[table().rotationRadians],
        this[table().isTemplateBackground],
        this[table().fillColor]?.let { Color(it) }
    )

    private fun UpdateBuilder<*>.write(template: CustomTemplate) {
        this[table().commandName] = template.commandName
        this[table().entityId] = template.entityId

        this[table().description] = template.description
        this[table().mediaUrl] = template.mediaUrl
        this[table().format] = template.format
        this[table().resultName] = template.resultName

        this[table().imageX] = template.imageContentX
        this[table().imageY] = template.imageContentY
        this[table().imageWidth] = template.imageContentWidth
        this[table().imageHeight] = template.imageContentHeight
        this[table().imagePosition] = template.imageContentPosition

        this[table().textX] = template.textContentX
        this[table().textY] = template.textContentY
        this[table().textWidth] = template.textContentWidth
        this[table().textHeight] = template.textContentHeight
        this[table().textPosition] = template.textContentPosition
        this[table().textAlignment] = template.textContentAlignment
        this[table().textFont] = template.font.name
        this[table().textColorRgb] = template.textColor.rgb
        this[table().textMaxSize] = template.font.size

        this[table().rotationRadians] = template.contentRotation
        this[table().isTemplateBackground] = template.isBackground
        this[table().fillColor] = template.fill.getOrNull()?.rgb
    }

    suspend fun create(template: CustomTemplate): Unit = dbQuery {
        insert {
            it.write(template)
        }
    }

    suspend fun read(commandName: String, entityId: Long): CustomTemplate? = dbQuery {
        select {
            table().commandName eq commandName and (table().entityId eq entityId)
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
            table().entityId eq entityId
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
        select { table().commandName eq commandName and (table().entityId eq entityId) }.any()
    }

    suspend fun delete(commandName: String, entityId: Long): Unit = dbQuery {
        deleteWhere { table().commandName eq commandName and (table().entityId eq entityId) }
    }
}
