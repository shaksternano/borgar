package io.github.shaksternano.borgar.command

import com.google.common.collect.ListMultimap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.shaksternano.borgar.command.util.CommandRegistry
import io.github.shaksternano.borgar.command.util.CommandResponse
import io.github.shaksternano.borgar.data.repository.TemplateRepository
import io.github.shaksternano.borgar.io.FileUtil
import io.github.shaksternano.borgar.media.ImageUtil
import io.github.shaksternano.borgar.media.graphics.Position
import io.github.shaksternano.borgar.media.graphics.TextAlignment
import io.github.shaksternano.borgar.media.template.CustomTemplateInfo
import io.github.shaksternano.borgar.util.Fonts
import io.github.shaksternano.borgar.util.MessageUtil
import io.github.shaksternano.borgar.util.StringUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.awt.Color
import java.awt.Font
import java.io.File
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

object CreateTemplateCommand : BaseCommand<Unit>(
    "template",
    "Creates a custom image template for this guild.",
) {

    @OptIn(DelicateCoroutinesApi::class)
    override fun execute(
        arguments: MutableList<String>,
        extraArguments: ListMultimap<String, String>,
        event: MessageReceivedEvent
    ): CompletableFuture<CommandResponse<Unit>> = GlobalScope.future {
        val member = event.member
        if (member != null) {
            if (!member.permissions.contains(Permission.MANAGE_GUILD_EXPRESSIONS)) {
                return@future CommandResponse("You do not have permission to create templates!")
            }
        }

        val templateFileOptional = MessageUtil.downloadFile(event.message).await()
        if (templateFileOptional.isEmpty) {
            return@future CommandResponse("No template file found!")
        }
        val templateFile = templateFileOptional.orElseThrow().file
        return@future try {
            val templateJson = parseJson(templateFile)
            val guildId = if (event.isFromGuild) event.guild.idLong else event.channel.idLong
            val commandName = getString(templateJson, "command_name").lowercase()
            if (CommandRegistry.isCommand(commandName)) {
                return@future CommandResponse("A command with the name `$commandName` already exists!")
            }
            if (TemplateRepository.exists(commandName, guildId)) {
                return@future CommandResponse("A template with the command name `$commandName` already exists!")
            }
            val template = createTemplate(templateJson, commandName)
            val mediaUrl = template.mediaUrl
            TemplateRepository.create(template, commandName, mediaUrl, guildId)
            CommandResponse("Template created!")
        } catch (e: InvalidTemplateException) {
            CommandResponse("Invalid template file. Reason: ${e.message}")
        }
    }

    private fun parseJson(file: File): JsonObject = runCatching {
        file.bufferedReader().use {
            JsonParser.parseReader(it).asJsonObject
        }
    }.getOrElse {
        throw InvalidTemplateException("Invalid JSON!", it)
    }

    private suspend fun createTemplate(templateJson: JsonObject, commandName: String): CustomTemplateInfo {
        val mediaUrl = getString(templateJson, "media_url")
        if (!isUrlValid(mediaUrl)) {
            throw InvalidTemplateException("Invalid media URL!")
        }
        val format = format(mediaUrl).lowercase()
        val resultName = getString(templateJson, "result_name") {
            commandName
        }

        val imageProperties = getObject(templateJson, "image")
        val imageX = getInt(imageProperties, "x")
        val imageY = getInt(imageProperties, "y")
        val imageWidth = getInt(imageProperties, "width")
        val imageHeight = getInt(imageProperties, "height")
        val imagePosition = getEnum<Position>(imageProperties, "position") {
            Position.CENTRE
        }

        val textProperties = getObject(templateJson, "text") {
            JsonObject()
        }
        val textX = getInt(textProperties, "x") {
            imageX
        }
        val textY = getInt(textProperties, "y") {
            imageY
        }
        val textWidth = getInt(textProperties, "width") {
            imageWidth
        }
        val textHeight = getInt(textProperties, "height") {
            imageHeight
        }
        val textPosition = getEnum<Position>(textProperties, "position") {
            imagePosition
        }
        val textAlignment = getEnum<TextAlignment>(textProperties, "alignment") {
            TextAlignment.CENTRE
        }
        val textFont = getString(textProperties, "font") {
            "Futura-CondensedExtraBold"
        }
        if (!Fonts.fontExists(textFont)) {
            throw InvalidTemplateException("Font $textFont does not exist!")
        }
        val textMaxSize = getInt(textProperties, "max_size") {
            200
        }
        val textColor = getColor(textProperties, "color") {
            Color.BLACK
        }

        val isBackground = getBoolean(templateJson, "is_background") {
            false
        }
        val fillColor = runCatching { getColor(templateJson, "fill_color") }.getOrDefault(null)

        return CustomTemplateInfo(
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

            isBackground,
            fillColor,
        )
    }

    private suspend fun isUrlValid(url: String): Boolean {
        val client = HttpClient.newHttpClient()
        val request = runCatching {
            HttpRequest.newBuilder(URI(url))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build()
        }.getOrElse {
            return false
        }
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.discarding()).await()
        return response.statusCode() in 200..299
    }

    private inline fun <reified R> getAs(
        json: JsonObject,
        key: String,
        transform: (JsonElement) -> R,
        noinline default: (() -> R)? = null
    ): R {
        val value = json.get(key)
        return if (value == null) {
            if (default == null) {
                throw MissingKeyException("No `$key` found!")
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

    private fun getObject(json: JsonObject, key: String, default: (() -> JsonObject)? = null): JsonObject {
        return getAs(json, key, JsonElement::getAsJsonObject, default)
    }

    private fun getString(json: JsonObject, key: String, default: (() -> String)? = null): String {
        return getAs(json, key, JsonElement::getAsString, default)
    }

    private fun getInt(json: JsonObject, key: String, default: (() -> Int)? = null): Int {
        return getAs(json, key, JsonElement::getAsInt, default)
    }

    private fun getBoolean(json: JsonObject, key: String, default: (() -> Boolean)? = null): Boolean {
        return getAs(json, key, JsonElement::getAsBoolean, default)
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
            Color(it.asInt)
        }, default)
    }

    private suspend fun format(url: String): String {
        return runCatching {
            withContext(Dispatchers.IO) {
                URL(url).openStream()
            }.use {
                ImageUtil.getImageFormat(it)
            }
        }.getOrElse {
            FileUtil.getFileExtension(url)
        }
    }

    private open class InvalidTemplateException(message: String, cause: Throwable? = null) :
        IllegalArgumentException(message, cause)

    private class MissingKeyException(key: String) : InvalidTemplateException("No `$key` found!")
}
