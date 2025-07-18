package io.github.shaksternano.borgar.core

import io.github.shaksternano.borgar.core.io.IO_DISPATCHER
import io.github.shaksternano.borgar.core.util.DEFAULT_TENOR_API_KEY
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.system.exitProcess

@Serializable
data class BotConfig(
    val botTokens: BotTokens = BotTokens(),
    val database: DatabaseConfig = DatabaseConfig(),
    val discordLogChannelId: String = "",
    val tenorApiKey: String = DEFAULT_TENOR_API_KEY,
    val encoder: EncoderConfig = EncoderConfig(),
    val cobaltApiUrl: String = "",
    val derpibooru: DerpibooruConfig = DerpibooruConfig(),
) {

    @Serializable
    data class BotTokens(
        val discord: String = "",
        val revolt: String = "",
    )

    @Serializable
    data class DatabaseConfig(
        val url: String = "jdbc:postgresql://localhost:5232/postgres",
        val driver: String = "org.postgresql.Driver",
        val user: String = "root",
        val password: String = "password",
    )

    @Serializable
    data class EncoderConfig(
        val ffmpegMp4Encoder: String = "",
        val ffmpegWebmEncoder: String = "",
    )

    @Serializable
    data class DerpibooruConfig(
        val tagsUrl: String = "",
        val filteredTagsUrl: String = "",
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
                exitProcess(0)
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
    }
}
