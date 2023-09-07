package io.github.shaksternano.borgar.core.command

import com.google.common.collect.ListMultimap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.shaksternano.borgar.core.command.util.CommandRegistry
import io.github.shaksternano.borgar.core.command.util.CommandResponse
import io.github.shaksternano.borgar.core.data.repository.TemplateRepository
import io.github.shaksternano.borgar.core.io.FileUtil
import io.github.shaksternano.borgar.core.media.ImageUtil
import io.github.shaksternano.borgar.core.media.graphics.Position
import io.github.shaksternano.borgar.core.media.graphics.TextAlignment
import io.github.shaksternano.borgar.core.media.template.CustomTemplate
import io.github.shaksternano.borgar.core.util.Fonts
import io.github.shaksternano.borgar.core.util.MessageUtil
import io.github.shaksternano.borgar.core.util.StringUtil
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.InputStream

object CreateTemplateCommand : KotlinCommand<Unit>(
    "template",
    "Creates a custom image template for this guild.",
) {

    override suspend fun executeSuspend(
        arguments: List<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CommandResponse<Unit> {
        val templateFileOptional = MessageUtil.downloadFile(event.message).await()
        if (templateFileOptional.isEmpty) {
            return CommandResponse("No template file found!")
        }
        val templateFile = templateFileOptional.orElseThrow().file
        return try {
            val templateJson = parseJson(templateFile)
            val entityId = if (event.isFromGuild) {
                event.guild.idLong
            } else {
                event.author.idLong
            }
            val commandName = getString(templateJson, "command_name").lowercase()
            if (CommandRegistry.isCommand(commandName)) {
                return CommandResponse("A command with the name `$commandName` already exists!")
            }
            if (TemplateRepository.exists(commandName, entityId)) {
                return CommandResponse("A template with the command name `$commandName` already exists!")
            }
            val template = createTemplate(templateJson, commandName, entityId)
            TemplateRepository.create(template)
            HelpCommand.removeCachedMessage(entityId)
            CommandResponse("Template created!")
        } catch (e: InvalidTemplateException) {
            CommandResponse("Invalid template file. ${e.message}")
        }
    }

    override fun requiredPermissions(): Set<Permission> = setOf(Permission.MANAGE_GUILD_EXPRESSIONS)

    private fun parseJson(file: File): JsonObject = runCatching {
        file.bufferedReader().use {
            JsonParser.parseReader(it).asJsonObject
        }
    }.getOrElse {
        throw InvalidTemplateException("Invalid JSON!", it)
    }

    private suspend fun createTemplate(
        templateJson: JsonObject,
        commandName: String,
        entityId: Long
    ): CustomTemplate {
        val description = getString(templateJson, "description") {
            "A custom template."
        }
        val mediaUrl = getString(templateJson, "media_url")
        if (!isUrlValid(mediaUrl)) {
            throw InvalidTemplateException("Invalid media URL!")
        }
        val format = format(mediaUrl).lowercase()
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
        checkValidPadding(imageStartX, "image.start.x", imageEndX, "image.end.x", imagePadding, "image.padding")
        checkValidPadding(imageStartY, "image.start.y", imageEndY, "image.end.y", imagePadding, "image.padding")

        val imageX = imageStartX + imagePadding
        val imageY = imageStartY + imagePadding
        val imageWidth = imageEndX - imageStartX - imagePadding * 2
        val imageHeight = imageEndY - imageStartY - imagePadding * 2
        val imagePosition = getEnum<Position>(templateJson, "image.position") {
            Position.CENTRE
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
        checkValidPadding(textStartX, "text.start.x", textEndX, "text.end.x", textPadding, "text.padding")
        checkValidPadding(textStartY, "text.start.y", textEndY, "text.end.y", textPadding, "text.padding")

        val textX = textStartX + textPadding
        val textY = textStartY + textPadding
        val textWidth = textEndX - textStartX - textPadding * 2
        val textHeight = textEndY - textStartY - textPadding * 2
        val textPosition = getEnum<Position>(templateJson, "text.position") {
            imagePosition
        }
        val textAlignment = getEnum<TextAlignment>(templateJson, "text.alignment") {
            TextAlignment.CENTRE
        }
        val textFont = getString(templateJson, "text.font") {
            "Futura-CondensedExtraBold"
        }
        if (!Fonts.fontExists(textFont)) {
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

        return CustomTemplate(
            commandName,
            entityId,

            description,
            mediaUrl,
            format,
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

    private suspend fun isUrlValid(url: String): Boolean {
        return try {
            HttpClient(CIO).use { client ->
                client.head(url).status.isSuccess()
            }
        } catch (e: Exception) {
            false
        }
    }

    private inline fun <reified R> getAs(
        json: JsonObject,
        key: String,
        transform: (JsonElement) -> R,
        noinline default: (() -> R)? = null
    ): R {
        val keys = key.split(".")
        val value = keys.fold(json as JsonElement) { subJson, keyPart ->
            if (subJson is JsonObject) {
                val jsonElement = subJson.get(keyPart)
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
                val typeName = StringUtil.splitCamelCase(R::class.java.simpleName).lowercase()
                throw InvalidTemplateException("`$key` is not a valid $typeName!")
            }
        }
    }

    private fun getString(json: JsonObject, key: String, default: (() -> String)? = null): String {
        return getAs(json, key, JsonElement::getAsString, default)
    }

    private fun getInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        return getAs(json, key, JsonElement::getAsInt, default)
    }

    private fun getPositiveInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        val value = getInt(json, key, default)
        if (value <= 0) {
            throw InvalidTemplateException("`$key` must be positive!")
        }
        return value
    }

    private fun getPositiveOrZeroInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        val value = getInt(json, key, default)
        if (value < 0) {
            throw InvalidTemplateException("`$key` must be positive or zero!")
        }
        return value
    }

    @Suppress("SameParameterValue")
    private fun getBoolean(json: JsonObject, key: String, default: (() -> Boolean)? = null): Boolean {
        return getAs(json, key, JsonElement::getAsBoolean, default)
    }

    @Suppress("SameParameterValue")
    private fun getDouble(json: JsonObject, key: String, default: (() -> Double)? = null): Double {
        return getAs(json, key, JsonElement::getAsDouble, default)
    }

    private fun checkValidPadding(
        start: Int,
        startKey: String,
        end: Int,
        endKey: String,
        padding: Int,
        paddingKey: String
    ) {
        if (start + padding * 2 >= end) {
            throw InvalidTemplateException("`$startKey` + `$paddingKey` * 2 must be less than `$endKey`!")
        }
    }

    private inline fun <reified T : Enum<T>> getEnum(
        json: JsonObject,
        key: String,
        noinline default: (() -> T)? = null
    ): T {
        return getAs(json, key, {
            enumValueOf<T>(it.asString.uppercase())
        }, default)
    }

    private fun getColor(json: JsonObject, key: String, default: (() -> Color)? = null): Color {
        return getAs(json, key, {
            val rgb = try {
                it.asInt
            } catch (e: Exception) {
                Integer.decode(it.asString)
            }
            Color(rgb)
        }, default)
    }

    private suspend fun format(url: String): String {
        return try {
            HttpClient(CIO).use { client ->
                client.get(url).body<InputStream>().use {
                    ImageUtil.getImageFormat(it)
                }
            }
        } catch (e: Exception) {
            FileUtil.getFileExtension(url)
        }
    }

    private open class InvalidTemplateException(message: String, cause: Throwable? = null) :
        IllegalArgumentException(message, cause)

    private class MissingKeyException(key: String) : InvalidTemplateException("No `$key` found!")
}
