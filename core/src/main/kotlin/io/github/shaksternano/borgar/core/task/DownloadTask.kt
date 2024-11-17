package io.github.shaksternano.borgar.core.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.exception.FileTooLargeException
import io.github.shaksternano.borgar.core.io.*
import io.github.shaksternano.borgar.core.io.head
import io.github.shaksternano.borgar.core.io.post
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
    240,
    144,
)

class DownloadTask(
    private val url: String,
    private val audioOnly: Boolean = false,
    private val fileNumber: Int? = null,
    private val maxFileSize: Long,
) : BaseFileTask() {

    override val requireInput: Boolean = false

    override suspend fun run(input: List<DataSource>): List<DataSource> =
        try {
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

    private tailrec suspend fun download(
        url: String,
        videoQualityIndex: Int,
        audioOnly: Boolean,
        fileIndex: Int?,
        maxFileSize: Long,
    ): List<DataSource> {
        val videoQuality = VIDEO_QUALITIES[videoQualityIndex]
        val downloadUrls = runCatching {
            getDownloadUrls(url, videoQuality, audioOnly, fileIndex)
        }.getOrElse {
            if (videoQualityIndex < VIDEO_QUALITIES.size - 1) {
                return download(
                    url,
                    videoQualityIndex + 1,
                    audioOnly,
                    fileIndex,
                    maxFileSize,
                )
            } else {
                throw it
            }
        }
        val files = mutableListOf<DataSource>()
        downloadUrls.forEach { dataSource ->
            val toSend: DataSource
            val tooLarge: Boolean
            val size = dataSource.size()
            if (size == null) {
                val fileDataSource = try {
                    dataSource.getOrWriteFile(maxFileSize)
                } catch (e: FileTooLargeException) {
                    null
                }
                if (fileDataSource == null) {
                    toSend = dataSource
                    tooLarge = true
                } else {
                    toSend = fileDataSource
                    tooLarge = false
                }
            } else {
                toSend = dataSource
                tooLarge = size > maxFileSize
            }
            if (tooLarge) {
                files.forEach {
                    it.path?.deleteSilently()
                }
                if (videoQualityIndex < VIDEO_QUALITIES.size - 1) {
                    return download(
                        url,
                        videoQualityIndex + 1,
                        audioOnly,
                        fileIndex,
                        maxFileSize,
                    )
                } else {
                    throw FileTooLargeException()
                }
            } else {
                files.add(toSend)
            }
        }
        return files
    }

    private suspend fun getDownloadUrls(
        url: String,
        videoQuality: Int,
        audioOnly: Boolean,
        fileIndex: Int?,
    ): List<DataSource> {
        val cobaltApiUrl = getEnvVar("COBALT_API_URL")
        if (cobaltApiUrl.isNullOrBlank()) {
            throw IllegalStateException("Cobalt API url is not set!")
        }
        val requestBody = CobaltRequestBody(
            url,
            videoQuality.toString(),
            filenameStyle = "basic",
            downloadMode = if (audioOnly) "audio" else "auto",
            twitterGif = true,
        )
        val responseBodyString = useHttpClient { client ->
            val response = client.post(cobaltApiUrl) {
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
        if (responseBody.status == "error") {
            throw CobaltException(responseBody.error?.code ?: responseBodyString)
        }
        return if (responseBody.url.isNotBlank()) {
            if (fileIndex != null && fileIndex != 0) {
                throw InvalidFileNumberException(1)
            } else {
                val filename = responseBody.filename
                    .replaceInvalidFilenameCharacters()
                DataSource.fromUrl(responseBody.url, filename).asSingletonList()
            }
        } else if (responseBody.picker != null) {
            if (fileIndex != null) {
                if (fileIndex >= responseBody.picker.size) {
                    throw InvalidFileNumberException(responseBody.picker.size)
                } else {
                    val fileUrl = responseBody.picker[fileIndex].url
                    val filename = useHttpClient { client ->
                        val headResponse = client.head(fileUrl)
                        getFilename(headResponse, fileUrl)
                    }.replaceInvalidFilenameCharacters()
                    DataSource.fromUrl(fileUrl, filename).asSingletonList()
                }
            } else useHttpClient { client ->
                responseBody.picker.map {
                    val fileUrl = it.url
                    val headResponse = client.head(fileUrl)
                    val filename = getFilename(headResponse, fileUrl)
                        .replaceInvalidFilenameCharacters()
                    DataSource.fromUrl(fileUrl, filename)
                }
            }
        } else {
            val prettyPrint = prettyPrintJsonCatching(responseBodyString)
            throw IllegalStateException("Missing url and picker fields. Response body:\n$prettyPrint")
        }
    }

    @Serializable
    private data class CobaltRequestBody(
        val url: String,
        val videoQuality: String,
        val filenameStyle: String,
        val downloadMode: String,
        val twitterGif: Boolean,
    )

    @Serializable
    private data class CobaltResponseBody(
        val status: String,
        val url: String = "",
        val filename: String = "",
        val picker: List<CobaltPicker>? = null,
        val error: CobaltError? = null,
    )

    @Serializable
    private data class CobaltPicker(
        val url: String,
    )

    @Serializable
    private data class CobaltError(
        val code: String,
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

    private class CobaltException(
        override val message: String,
    ) : Exception(message)

    private class InvalidFileNumberException(
        val maxFiles: Int,
    ) : Exception()
}
