package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.util.JSON
import io.github.shaksternano.borgar.core.util.asSingletonList
import io.github.shaksternano.borgar.core.util.getEnvVar
import io.github.shaksternano.borgar.core.util.prettyPrintJsonCatching
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private val VIDEO_QUALITIES: List<Int> = listOf(
    1080,
    720,
    480,
    360,
)

class DownloadTask(
    private val audioOnly: Boolean,
    private val fileNumber: Int?,
    private val maxFileSize: Long,
) : BaseFileTask(
    requireInput = false,
) {

    override suspend fun run(input: List<DataSource>): List<DataSource> = try {
        val url = input.firstOrNull()?.url ?: throw ErrorResponseException("No URL specified!")
        val fileIndex = fileNumber?.let { it - 1 }
        download(url, 0, audioOnly, fileIndex, maxFileSize)
    } catch (e: InvalidFileNumberException) {
        var message = "File number is too large, there "
        message +=
            if (e.maxFiles == 1) "is"
            else "are"
        message += " only ${e.maxFiles} file"
        if (e.maxFiles != 1)
            message += "s"
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
                    return if (videoQualityIndex < VIDEO_QUALITIES.size - 1) {
                        download(
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
                getFilename(headResponse, downloadUrl)
            }
            DataSource.fromUrl(downloadUrl, filename)
        }
    }

    private suspend fun getDownloadUrls(
        url: String,
        videoQuality: Int,
        audioOnly: Boolean,
        fileIndex: Int?,
    ): List<String> {
        val cobaltApiDomain = getEnvVar("COBALT_API_DOMAIN") ?: "https://co.wuk.sh"
        val requestUrl = "$cobaltApiDomain/api/json"
        val requestBody = CobaltRequestBody(
            url,
            videoQuality,
            audioOnly,
            isNoTTWatermark = true,
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
            val prettyPrint = prettyPrintJsonCatching(responseBodyString)
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
        else {
            val prettyPrint = prettyPrintJsonCatching(responseBodyString)
            throw IllegalStateException("Missing url and picker fields. Response body:\n$prettyPrint")
        }
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

    private fun getFilename(headResponse: HttpResponse, url: String): String {
        val filename = headResponse.filename()
        if (filename != null) return filename
        val urlExtension = fileExtension(url)
        if (urlExtension.isNotBlank()) return filename(url)
        val filenameWithoutExtension = filenameWithoutExtension(url)
        val contentType = headResponse.contentType()
        val extension = if (contentType != null && contentType.contentSubtype.isNotBlank()) {
            contentType.contentSubtype.let {
                if (it.equals("jpeg", ignoreCase = true)) "jpg"
                else it
            }
        } else if (audioOnly) {
            "mp3"
        } else {
            "mp4"
        }
        return filename(filenameWithoutExtension, extension)
    }

    private class FileTooLargeException : Exception()

    private class InvalidFileNumberException(
        val maxFiles: Int,
    ) : Exception()
}
