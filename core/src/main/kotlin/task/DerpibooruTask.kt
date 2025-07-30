package com.shakster.borgar.core.task

import com.shakster.borgar.core.exception.ErrorResponseException
import com.shakster.borgar.core.io.DataSource
import com.shakster.borgar.core.io.httpGet
import com.shakster.borgar.core.util.asSingletonList
import com.shakster.borgar.core.util.parseTags
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.min

private const val DERPIBOORU_API_URL: String = "https://derpibooru.org"
private const val DERPIBOORU_RESULTS_PER_PAGE: Int = 50
private const val DERPIBOORU_18_PLUS_DARK_FILTER_ID: Int = 37429

class DerpibooruTask(
    tags: String,
    private val id: String?,
    private val searchAll: Boolean,
    private val fileCount: Int,
    private val maxFileSize: Long,
) : BaseFileTask() {

    override val requireInput: Boolean = false
    private val tags: Set<String> = parseTags(tags).let {
        if (searchAll) it
        else it.plus("safe")
    }

    override suspend fun run(input: List<DataSource>): List<DataSource> {
        if (id != null) {
            val requestUrl = getRequestUrlId()
            val response = runCatching {
                httpGet<DerpibooruSingleImageResponse>(requestUrl)
            }.getOrElse {
                throw ErrorResponseException("Image not found!")
            }
            val imageUrl = response.image.representations.full
            return DataSource.fromUrl(imageUrl, sendUrl = true).asSingletonList()
        }

        val requestUrl = getRequestUrl(1)
        val response = httpGet<DerpibooruImagesResponse>(requestUrl)
        if (response.total == 0) {
            throw ErrorResponseException("No images found!")
        }
        val totalPages = min(ceil(response.total.toDouble() / DERPIBOORU_RESULTS_PER_PAGE).toInt(), 10)
        val weightedPageNumbers = (1..totalPages).flatMap { page ->
            List(totalPages - page + 1) { page }
        }
        val randomPage = weightedPageNumbers.random()
        val randomPageResponse =
            if (randomPage == 1) response
            else httpGet(getRequestUrl(randomPage))
        return randomPageResponse.images
            .ifEmpty { throw ErrorResponseException("No images found!") }
            .shuffled()
            .take(fileCount)
            .map {
                if (fileCount == 1) DataSource.fromUrl(it.representations.full, sendUrl = true)
                else {
                    val image = it.getImage(maxFileSize)
                    val format =
                        if (it.format.equals("jpeg", ignoreCase = true)) "jpg"
                        else it.format.lowercase()
                    image.rename("derpibooru-${it.id}.$format")
                }
            }
    }

    private fun getRequestUrl(page: Int): String {
        val tagsQuery = tags.joinToString("%2C+") {
            it.replace(" ", "+")
        }
        return "$DERPIBOORU_API_URL/api/v1/json/search/images" +
            "?q=$tagsQuery" +
            "&filter_id=$DERPIBOORU_18_PLUS_DARK_FILTER_ID" +
            "&sf=upvotes" +
            "&page=$page" +
            "&per_page=$DERPIBOORU_RESULTS_PER_PAGE"
    }

    private fun getRequestUrlId(): String =
        "$DERPIBOORU_API_URL/api/v1/json/images/$id"

    private suspend fun DerpibooruImageBody.getImage(maxFileSize: Long): DataSource {
        val allRepresentations = representations.all
        allRepresentations.forEachIndexed { index, representation ->
            val dataSource = DataSource.fromUrl(representation)
            if (index == allRepresentations.size - 1 || dataSource.isWithinReportedSize(maxFileSize)) {
                return dataSource
            }
        }
        throw IllegalStateException("This should never happen")
    }
}

@Serializable
private data class DerpibooruImagesResponse(
    val total: Int,
    val images: List<DerpibooruImageBody>
)

@Serializable
private data class DerpibooruSingleImageResponse(
    val image: DerpibooruImageBody,
)

@Serializable
private data class DerpibooruImageBody(
    val id: Int,
    val format: String,
    val representations: DerpibooruImageRepresentations,
)

@Serializable
private data class DerpibooruImageRepresentations(
    val full: String,
    val medium: String,
    val small: String,
    val thumb: String,
    @SerialName("thumb_small")
    val thumbSmall: String,
    @SerialName("thumb_tiny")
    val thumbTiny: String,
) {

    val all: List<String>
        get() = listOf(full, medium, small, thumb, thumbSmall, thumbTiny)
}
