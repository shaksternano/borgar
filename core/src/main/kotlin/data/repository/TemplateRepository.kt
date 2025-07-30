package com.shakster.borgar.core.data.repository

import com.shakster.borgar.core.graphics.ContentPosition
import com.shakster.borgar.core.graphics.TextAlignment
import com.shakster.borgar.core.io.deleteSilently
import com.shakster.borgar.core.media.template.CustomTemplate
import com.shakster.borgar.core.util.ChannelEnvironment
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.awt.Color
import java.awt.Font
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

object TemplateRepository : Repository() {

    const val COMMAND_NAME_MAX_LENGTH: Int = 32
    const val COMMAND_DESCRIPTION_MAX_LENGTH: Int = 100

    private val cache: MutableMap<String, CustomTemplate> = ConcurrentHashMap()

    override fun table(): TemplateTable = TemplateTable

    suspend fun create(
        template: CustomTemplate,
        platform: String,
        entityType: String,
        creatorId: String,
    ) {
        dbQuery {
            insert {
                it.write(
                    template,
                    platform,
                    entityType,
                    creatorId,
                )
            }
        }
        cache[template.commandName] = template
    }

    suspend fun read(commandName: String, entityId: String): CustomTemplate? =
        cache[commandName] ?: dbQuery {
            queryPrimaryKey(commandName, entityId).map {
                it.read()
            }.firstOrNull()
        }?.also {
            cache[commandName] = it
        }

    suspend fun readAll(entityId: String): List<CustomTemplate> = dbQuery {
        selectAll().where { entityIdPredicate(entityId) }.map {
            it.read().also { template ->
                cache[template.commandName] = template
            }
        }
    }

    suspend fun exists(commandName: String, entityId: String): Boolean = dbQuery {
        queryPrimaryKey(commandName, entityId).any()
    }

    suspend fun delete(commandName: String, entityId: String) {
        dbQuery {
            queryPrimaryKey(commandName, entityId).forEach {
                Path(it[TemplateTable.mediaPath]).deleteSilently()
            }
            deleteWhere { primaryKeyPredicate(commandName, entityId) }
        }
        cache.remove(commandName)
    }

    private fun FieldSet.queryPrimaryKey(commandName: String, entityId: String): Query =
        selectAll().where { primaryKeyPredicate(commandName, entityId) }

    private fun entityIdPredicate(entityId: String): Op<Boolean> =
        TemplateTable.entityId eq entityId

    private fun primaryKeyPredicate(commandName: String, entityId: String): Op<Boolean> =
        TemplateTable.commandName eq commandName and entityIdPredicate(entityId)

    private fun ResultRow.read(): CustomTemplate = CustomTemplate(
        this[TemplateTable.commandName],
        this[TemplateTable.entityId],

        this[TemplateTable.description],
        this[TemplateTable.entityType].let {
            ChannelEnvironment.fromEntityType(it) ?: error("Invalid entity type: $it")
        },
        Path(this[TemplateTable.mediaPath]),
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
        this[TemplateTable.fillColor]?.let { Color(it) },
    )

    private fun UpdateBuilder<*>.write(
        template: CustomTemplate,
        platform: String,
        entityType: String,
        creatorId: String,
    ) {
        this[TemplateTable.commandName] = template.commandName
        this[TemplateTable.entityId] = template.entityId

        this[TemplateTable.platform] = platform
        this[TemplateTable.entityType] = entityType
        this[TemplateTable.creatorId] = creatorId
        this[TemplateTable.dateCreated] = OffsetDateTime.now()

        this[TemplateTable.description] = template.description
        this[TemplateTable.mediaPath] = template.mediaPath.toString()
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

        this[TemplateTable.rotationRadians] = template.contentRotationRadians
        this[TemplateTable.isTemplateBackground] = template.isBackground
        this[TemplateTable.fillColor] = template.fill?.rgb
    }
}

object TemplateTable : Table(name = "template") {
    val commandName = varchar("command_name", TemplateRepository.COMMAND_NAME_MAX_LENGTH)
    // Either a guild ID or a user ID (for DMs)
    val entityId = varchar("entity_id", 50)
    override val primaryKey = PrimaryKey(commandName, entityId, name = "template_pk")

    val description = varchar("description", TemplateRepository.COMMAND_DESCRIPTION_MAX_LENGTH)
    val platform = varchar("platform", 20)
    val entityType = varchar("entity_type", 50)
    val creatorId = varchar("creator_id", 50)
    val dateCreated = timestampWithTimeZone("date_created")

    val mediaPath = varchar("media_path", 100)
    val resultName = varchar("result_name", 100)

    val imageX = integer("image_x")
    val imageY = integer("image_y")
    val imageWidth = integer("image_width")
    val imageHeight = integer("image_height")
    val imagePosition = enumeration<ContentPosition>("image_position")

    val textX = integer("text_x")
    val textY = integer("text_y")
    val textWidth = integer("text_width")
    val textHeight = integer("text_height")
    val textPosition = enumeration<ContentPosition>("text_position")
    val textAlignment = enumeration<TextAlignment>("text_alignment")
    val textFont = varchar("text_font", 100)
    val textColorRgb = integer("text_color_rgb")
    val textMaxSize = integer("text_max_size")

    val rotationRadians = double("rotation_radians")
    val isTemplateBackground = bool("is_template_background")
    val fillColor = integer("fill_color").nullable()
}
