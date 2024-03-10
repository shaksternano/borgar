package io.github.shaksternano.borgar.chat.command

import io.github.shaksternano.borgar.chat.event.CommandEvent
import io.github.shaksternano.borgar.chat.util.getEntityId
import io.github.shaksternano.borgar.chat.util.getUrlsExceptSelf
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.graphics.ContentPosition
import io.github.shaksternano.borgar.core.graphics.TextAlignment
import io.github.shaksternano.borgar.core.graphics.fontExists
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.logger
import io.github.shaksternano.borgar.core.media.createAudioReader
import io.github.shaksternano.borgar.core.media.createImageReader
import io.github.shaksternano.borgar.core.media.template.CustomTemplate
import io.github.shaksternano.borgar.core.util.ChannelEnvironment
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.splitCamelCase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.awt.Color
import java.awt.Font
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

private const val MAX_FILE_SIZE: Int = 25 * 1024 * 1024

object CreateTemplateCommand : NonChainableCommand() {

    override val name: String = "createtemplate"
    override val description: String = "Creates a custom image template for this server or DM."
    override val argumentInfo: Set<CommandArgumentInfo<*>> = setOf(
        CommandArgumentInfo(
            key = "file",
            description = "The template json.",
            type = CommandArgumentType.Attachment,
            required = false,
        ),
        CommandArgumentInfo(
            key = "url",
            description = "The template json url.",
            type = CommandArgumentType.String,
            required = false,
        ),
    )
    override val defaultArgumentKey: String = "url"
    override val requiredPermissions: Set<Permission> = setOf(Permission.MANAGE_GUILD_EXPRESSIONS)
    override val deferReply: Boolean = true
    override val ephemeralReply: Boolean = true

    override suspend fun run(
        arguments: CommandArguments,
        event: CommandEvent,
    ): List<CommandResponse> {
        val templateFileUrl = getTemplateFileUrl(arguments, event)
            ?: return CommandResponse("No template file found!").asSingletonList()
        val templateJson = runCatching {
            useHttpClient {
                val response = it.get(templateFileUrl)
                val jsonString = response.bodyAsText()
                Json.parseToJsonElement(jsonString).jsonObject
            }
        }.getOrElse {
            return CommandResponse("Invalid JSON!").asSingletonList()
        }
        val entityId = event.getEntityId()
        val environment = event.getEnvironment()
        val platform = event.manager.platform.id
        return try {
            val commandName = getString(templateJson, "command_name").lowercase()
            if (commandName.isBlank()) {
                return CommandResponse("Command name cannot be blank!").asSingletonList()
            }
            if (commandName.length > TemplateRepository.COMMAND_NAME_MAX_LENGTH) {
                return CommandResponse("Command name is too long!").asSingletonList()
            }
            if (COMMANDS_AND_ALIASES.containsKey(commandName)) {
                return CommandResponse("A command with the name **$commandName** already exists!").asSingletonList()
            }
            if (TemplateRepository.exists(commandName, entityId)) {
                return CommandResponse("A template with the command name **$commandName** already exists!").asSingletonList()
            }
            val template = createTemplate(
                templateJson,
                commandName,
                platform,
                entityId,
                environment,
            )
            TemplateRepository.create(
                template,
                platform,
                environment.entityType,
                event.authorId,
            )
            val guild = event.getGuild()
            guild?.addCommand(TemplateCommand(template))
            HelpCommand.removeCachedMessage(entityId)
            CommandResponse("Template created!")
        } catch (e: InvalidTemplateException) {
            e.cause?.let {
                logger.error("Invalid template file", it)
            }
            CommandResponse("Invalid template file. ${e.message}")
        }.asSingletonList()
    }

    private suspend fun getTemplateFileUrl(arguments: CommandArguments, event: CommandEvent): String? {
        val templateFileAttachment = arguments.getDefaultAttachment()
        if (templateFileAttachment != null) {
            return templateFileAttachment.url
        }
        val messageIntersection = event.asMessageIntersection(arguments)
        val templateFileUrl = arguments.getDefaultUrl()
        if (templateFileUrl != null) {
            return templateFileUrl
        }
        return messageIntersection.getUrlsExceptSelf(false).firstOrNull()?.url
    }

    private suspend fun createTemplate(
        templateJson: JsonObject,
        commandName: String,
        platform: String,
        entityId: String,
        environment: ChannelEnvironment,
    ): CustomTemplate {
        val description = getString(templateJson, "description") {
            "A custom template."
        }
        if (description.length > TemplateRepository.COMMAND_DESCRIPTION_MAX_LENGTH) {
            throw InvalidTemplateException("Description is too long!")
        }
        val mediaUrl = getString(templateJson, "media_url")
        validateUrl(mediaUrl)

        val resultName = getString(templateJson, "result_name") {
            commandName
        }

        val imageStartX = getPositiveInt(templateJson, "image.start.x")
        val imageStartY = getPositiveInt(templateJson, "image.start.y")
        val imageEndX = getPositiveInt(templateJson, "image.end.x")
        val imageEndY = getPositiveInt(templateJson, "image.end.y")
        val imagePadding = getPositiveOrZeroInt(templateJson, "image.padding") {
            0
        }
        checkValidPadding(
            imageStartX,
            "image.start.x",
            imageEndX,
            "image.end.x",
            imagePadding,
            "image.padding"
        )
        checkValidPadding(
            imageStartY,
            "image.start.y",
            imageEndY,
            "image.end.y",
            imagePadding,
            "image.padding"
        )

        val imageX = imageStartX + imagePadding
        val imageY = imageStartY + imagePadding
        val imageWidth = imageEndX - imageStartX - imagePadding * 2
        val imageHeight = imageEndY - imageStartY - imagePadding * 2
        val imagePosition = getEnum<ContentPosition>(templateJson, "image.position") {
            ContentPosition.CENTRE
        }

        val textStartX = getPositiveInt(templateJson, "text.start.x") {
            imageStartX
        }
        val textStartY = getPositiveInt(templateJson, "text.start.y") {
            imageStartY
        }
        val textEndX = getPositiveInt(templateJson, "text.end.x") {
            imageEndX
        }
        val textEndY = getPositiveInt(templateJson, "text.end.y") {
            imageEndY
        }
        val textPadding = getPositiveOrZeroInt(templateJson, "text.padding") {
            imagePadding
        }
        checkValidPadding(
            textStartX,
            "text.start.x",
            textEndX,
            "text.end.x",
            textPadding,
            "text.padding"
        )
        checkValidPadding(
            textStartY,
            "text.start.y",
            textEndY,
            "text.end.y",
            textPadding,
            "text.padding"
        )

        val textX = textStartX + textPadding
        val textY = textStartY + textPadding
        val textWidth = textEndX - textStartX - textPadding * 2
        val textHeight = textEndY - textStartY - textPadding * 2
        val textPosition = getEnum<ContentPosition>(templateJson, "text.position") {
            imagePosition
        }
        val textAlignment = getEnum<TextAlignment>(templateJson, "text.alignment") {
            TextAlignment.CENTRE
        }
        val textFont = getString(templateJson, "text.font") {
            "Futura-CondensedExtraBold"
        }
        if (!fontExists(textFont)) {
            throw InvalidTemplateException("Font $textFont does not exist!")
        }
        val textMaxSize = getPositiveInt(templateJson, "text.max_size") {
            200
        }
        val textColor = getColor(templateJson, "text.color") {
            Color.BLACK
        }

        val rotationDegrees = getDouble(templateJson, "rotation") {
            0.0
        }
        val rotationRadians = Math.toRadians(rotationDegrees)
        val isBackground = getBoolean(templateJson, "is_background") {
            true
        }
        val fillColor = runCatching { getColor(templateJson, "fill_color") }.getOrDefault(null)

        val mediaPath = downloadMedia(
            mediaUrl,
            commandName,
            entityId,
            platform,
            environment.entityType,
        )

        return CustomTemplate(
            commandName,
            entityId,

            description,
            environment,
            mediaPath,
            resultName,

            imageX,
            imageY,
            imageWidth,
            imageHeight,
            imagePosition,

            textX,
            textY,
            textWidth,
            textHeight,
            textPosition,
            textAlignment,

            Font(textFont, Font.PLAIN, textMaxSize),
            textColor,
            rotationRadians,
            isBackground,
            fillColor,
        )
    }

    private fun validateUrl(url: String) {
        val uri = runCatching {
            URI.create(url)
        }.getOrElse {
            throw InvalidTemplateException("URL $url is invalid!")
        }
        val domain = uri.host ?: throw InvalidTemplateException("URL $url is invalid!")
        if (domain !in ALLOWED_DOMAINS) {
            throw InvalidTemplateException("Domain $domain is not allowed!")
        }
    }

    private suspend fun downloadMedia(
        mediaUrl: String,
        commandName: String,
        entityId: String,
        platform: String,
        entityType: String,
    ): Path =
        useHttpClient { client ->
            val response = runCatching {
                client.get(mediaUrl)
            }.getOrElse { t ->
                throw InvalidTemplateException("Invalid media URL!", t)
            }
            if (!response.status.isSuccess()) {
                throw InvalidTemplateException("Invalid media URL!")
            }
            val contentLength = response.contentLength() ?: 0
            if (contentLength > MAX_FILE_SIZE) {
                throw InvalidTemplateException("Media is too large!")
            }
            val contentType = response.contentType()
            val fileFormat = if (contentType != null && contentType.contentSubtype.isNotBlank()) {
                contentType.contentSubtype
            } else {
                fileExtension(mediaUrl)
            }.lowercase()
                .let {
                    if (it == "jpeg") "jpg"
                    else it
                }
                .ifBlank {
                    throw InvalidTemplateException("Could not determine media format!")
                }
            runCatching {
                val path = Path("templates/media")
                withContext(Dispatchers.IO) {
                    path.createDirectories()
                }
                path.resolve(
                    platform +
                        "_$entityType" +
                        "_${entityId}" +
                        "_$commandName.$fileFormat"
                )
            }.getOrElse { t ->
                throw InvalidTemplateException("Command name $commandName is not allowed!", t)
            }.also { mediaPath ->
                runCatching {
                    response.download(mediaPath)
                }.getOrElse { t ->
                    mediaPath.deleteSilently()
                    throw InvalidTemplateException("Failed to read media!", t)
                }
                runCatching {
                    // Check if media can be read
                    val dataSource = DataSource.fromFile(mediaPath)
                    createImageReader(dataSource).close()
                    createAudioReader(dataSource).close()
                }.getOrElse { t ->
                    mediaPath.deleteSilently()
                    throw InvalidTemplateException("Media is not an image or audio file!", t)
                }
            }
        }

    private inline fun <reified R> getAs(
        json: JsonObject,
        key: String,
        noinline default: (() -> R)? = null,
        transform: (JsonElement) -> R,
    ): R {
        val keys = key.split(".")
        val value = keys.fold(json as JsonElement) { subJson, keyPart ->
            if (subJson is JsonObject) {
                val jsonElement = subJson[keyPart]
                if (jsonElement != null) {
                    return@fold jsonElement
                }
            }
            json
        }
        return if (value === json) {
            if (default == null) {
                throw MissingKeyException(key)
            } else {
                default()
            }
        } else {
            runCatching {
                transform(value)
            }.getOrElse {
                val typeName = R::class.simpleName?.splitCamelCase()?.lowercase()
                throw if (typeName == null)
                    InvalidTemplateException("**$key** is invalid!")
                else
                    InvalidTemplateException("**$key** is not a valid $typeName!")
            }
        }
    }

    private fun getString(json: JsonObject, key: String, default: (() -> String)? = null): String {
        return getAs(json, key, default) {
            it.jsonPrimitive.content
        }
    }

    private fun getInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        return getAs(json, key, default) {
            it.jsonPrimitive.int
        }
    }

    private fun getPositiveInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        val value = getInt(json, key, default)
        if (value <= 0) {
            throw InvalidTemplateException("**$key** must be positive!")
        }
        return value
    }

    private fun getPositiveOrZeroInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        val value = getInt(json, key, default)
        if (value < 0) {
            throw InvalidTemplateException("**$key** must be positive or zero!")
        }
        return value
    }

    @Suppress("SameParameterValue")
    private fun getBoolean(json: JsonObject, key: String, default: (() -> Boolean)? = null): Boolean =
        getAs(json, key, default) {
            it.jsonPrimitive.boolean
        }

    @Suppress("SameParameterValue")
    private fun getDouble(json: JsonObject, key: String, default: (() -> Double)? = null): Double =
        getAs(json, key, default) {
            it.jsonPrimitive.double
        }

    private fun checkValidPadding(
        start: Int,
        startKey: String,
        end: Int,
        endKey: String,
        padding: Int,
        paddingKey: String,
    ) {
        if (start + padding * 2 >= end) {
            throw InvalidTemplateException("**$startKey** + **$paddingKey** * 2 must be less than **$endKey**!")
        }
    }

    private inline fun <reified T : Enum<T>> getEnum(
        json: JsonObject,
        key: String,
        noinline default: (() -> T)? = null,
    ): T =
        getAs(json, key, default) {
            enumValueOf<T>(it.jsonPrimitive.content.uppercase())
        }

    private fun getColor(json: JsonObject, key: String, default: (() -> Color)? = null): Color =
        getAs(json, key, default) {
            val rgb = try {
                it.jsonPrimitive.int
            } catch (e: Exception) {
                Integer.decode(it.jsonPrimitive.content)
            }
            Color(rgb)
        }

    private open class InvalidTemplateException(
        override val message: String,
        override val cause: Throwable? = null,
    ) : IllegalArgumentException(message, cause)

    private class MissingKeyException(key: String) : InvalidTemplateException("No **$key** found!")
}
