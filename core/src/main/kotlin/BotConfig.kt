package com.shakster.borgar.core

import com.shakster.borgar.core.io.IO_DISPATCHER
import com.shakster.borgar.core.util.DEFAULT_TENOR_API_KEY
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
data class BotConfig(
    val discord: DiscordConfig = DiscordConfig(),
    val revolt: RevoltConfig = RevoltConfig(),
    val commandPrefix: String = "%",
    val database: DatabaseConfig = DatabaseConfig(),
    val tenorApiKey: String = DEFAULT_TENOR_API_KEY,
    val cobaltApiUrl: String = "",
    val derpibooru: DerpibooruConfig = DerpibooruConfig(),
    val ffmpeg: FFmpegConfig = FFmpegConfig(),
) {

    @Serializable
    data class DiscordConfig(
        val token: String = "",
        val logChannelId: String = "",
    )

    @Serializable
    data class RevoltConfig(
        val token: String = "",
    )

    @Serializable
    data class DatabaseConfig(
        val url: String = "jdbc:sqlite:borgar.sqlite",
        val user: String = "root",
        val password: String = "password",
    )

    @Serializable
    data class DerpibooruConfig(
        val tagsUrl: String = "",
        val filteredTagsUrl: String = "",
    )

    @Serializable
    data class FFmpegConfig(
        val mp4Encoder: String = "",
        val webmEncoder: String = "",
    )

    companion object {

        private var instance: BotConfig? = null

        suspend fun load(path: Path) = withContext(IO_DISPATCHER) {
            @OptIn(ExperimentalSerializationApi::class)
            val json = Json {
                encodeDefaults = true
                isLenient = true
                prettyPrint = true
                allowTrailingComma = true
                allowComments = true
            }

            if (!path.exists()) {
                val defaultConfig = BotConfig()
                val json = json.encodeToString(defaultConfig)
                path.writeText(json)
                instance = defaultConfig
                logger.info("Config file not found at ${path.absolute()}, created a new one.")
                return@withContext
            }

            if (!path.isRegularFile()) {
                throw IllegalArgumentException("Config path must be a file: ${path.absolute()}")
            }

            val configContent = path.readText()
            try {
                instance = json.decodeFromString<BotConfig>(configContent)
            } catch (t: Throwable) {
                throw IllegalStateException("Failed to parse config file: ${path.absolute()}", t)
            }
        }

        fun get(): BotConfig {
            val config = instance
            if (config == null) {
                throw IllegalStateException("Config not loaded. Call load() first.")
            }
            return config
        }

        // For testing purposes
        fun set(config: BotConfig) {
            instance = config
        }
    }
}
