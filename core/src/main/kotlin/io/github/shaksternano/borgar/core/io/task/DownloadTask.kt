package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.filename
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.core.util.JSON
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.core.util.prettyPrintJson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private val VIDEO_QUALITIES: List<Int> = listOf(
    720,
    360,
)

class DownloadTask(
    private val url: String,
    private val audioOnly: Boolean,
    private val fileNumber: Int?,
    private val maxFileSize: Long,
) : BaseFileTask(
    requireInput = false,
) {

    override suspend fun run(input: List<DataSource>): List<DataSource> = try {
        val fileIndex: Int? = fileNumber?.let { it - 1 }
        download(url, 0, audioOnly, fileIndex, maxFileSize)
    } catch (e: InvalidFileNumberException) {
        var message = "File number is too large, there "
        message += if (e.maxFiles == 1) "is"
        else "are"
        message += " only ${e.maxFiles} file"
        if (e.maxFiles != 1) message += "s"
        message += "!"
        throw ErrorResponseException(message)
    } catch (e: FileTooLargeException) {
        throw ErrorResponseException("File is too large!")
    } catch (e: Throwable) {
        throw ErrorResponseException("Error downloading file!", e)
    }

    private suspend fun download(
        url: String,
        videoQualityIndex: Int,
        audioOnly: Boolean,
        fileIndex: Int?,
        maxFileSize: Long,
    ): List<DataSource> {
        val videoQuality = VIDEO_QUALITIES[videoQualityIndex]
        val downloadUrls = getDownloadUrls(url, videoQuality, audioOnly, fileIndex)
        return downloadUrls.map { downloadUrl ->
            val filename = useHttpClient { client ->
                val headResponse = client.head(downloadUrl)
                val contentLength = headResponse.contentLength() ?: 0
                if (contentLength > maxFileSize) {
                    if (videoQualityIndex < VIDEO_QUALITIES.size - 1) {
                        return download(
                            url,
                            videoQualityIndex + 1,
                            audioOnly,
                            fileIndex,
                            maxFileSize
                        )
                    } else {
                        throw FileTooLargeException()
                    }
                }
                getFilename(headResponse)
            }
            DataSource.fromUrl(downloadUrl, filename)
        }
    }

    private suspend fun getDownloadUrls(
        url: String,
        videoQuality: Int,
        audioOnly: Boolean,
        fileIndex: Int?
    ): List<String> {
        val cobaltApiDomain = getEnvVar("COBALT_API_DOMAIN") ?: "https://co.wuk.sh"
        val requestUrl = "$cobaltApiDomain/api/json"
        val requestBody = CobaltRequestBody(
            url,
            videoQuality,
            audioOnly,
            true
        )
        val responseBodyString = useHttpClient { client ->
            val response = client.post(requestUrl) {
                // The Accept header is set to application/json by default
                header(HttpHeaders.ContentType, "application/json")
                setBody(requestBody)
            }
            response.bodyAsText()
        }
        val responseBody = runCatching {
            JSON.decodeFromString<CobaltResponseBody>(responseBodyString)
        }.getOrElse {
            val prettyPrint = runCatching {
                prettyPrintJson(responseBodyString)
            }.getOrDefault(responseBodyString)
            throw IllegalStateException("Invalid Cobalt response body:\n$prettyPrint", it)
        }
        return if (responseBody.url != null)
            if (fileIndex != null && fileIndex != 0) throw InvalidFileNumberException(1)
            else responseBody.url.asSingletonList()
        else if (responseBody.picker != null)
            if (fileIndex != null)
                if (fileIndex >= responseBody.picker.size) throw InvalidFileNumberException(responseBody.picker.size)
                else responseBody.picker[fileIndex].url.asSingletonList()
            else responseBody.picker.map { it.url }
        else throw IllegalStateException("Missing url and picker fields. Response body:\n$responseBodyString")
    }

    @Serializable
    private data class CobaltRequestBody(
        val url: String,
        val vQuality: String,
        val isAudioOnly: Boolean,
        val isNoTTWatermark: Boolean,
    ) {
        constructor(
            url: String,
            vQuality: Int,
            isAudioOnly: Boolean,
            isNoTTWatermark: Boolean,
        ) : this(
            url,
            vQuality.toString(),
            isAudioOnly,
            isNoTTWatermark,
        )
    }

    @Serializable
    private data class CobaltResponseBody(
        val url: String? = null,
        val picker: List<CobaltPicker>? = null,
    )

    @Serializable
    private data class CobaltPicker(
        val url: String,
    )

    private fun getFilename(headResponse: HttpResponse): String {
        val filename = headResponse.filename()
        if (filename != null) return filename
        val contentType = headResponse.contentType()
        if (contentType != null) return filename(contentType.contentType, contentType.contentSubtype)
        return if (audioOnly) {
            "audio.mp3"
        } else {
            "video.mp4"
        }
    }

    private class FileTooLargeException : Exception()

    private class InvalidFileNumberException(
        val maxFiles: Int
    ) : Exception()
}
