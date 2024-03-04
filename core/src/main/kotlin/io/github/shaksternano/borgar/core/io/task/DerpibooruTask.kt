package io.github.shaksternano.borgar.core.io.task

import io.github.shaksternano.borgar.core.exception.ErrorResponseException
import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.httpGet
import io.github.shaksternano.borgar.core.util.parseTags
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.min

private const val DERPIBOORU_API_DOMAIN: String = "https://derpibooru.org"
private const val DERPIBOORU_RESULTS_PER_PAGE: Int = 50

class DerpibooruTask(
    tags: String,
    private val fileCount: Int,
    private val maxFileSize: Long,
) : BaseFileTask() {

    override val requireInput: Boolean = false
    private val tags: Set<String> = parseTags(tags)

    override suspend fun run(input: List<DataSource>): List<DataSource> {
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
            .mapNotNull { it.getImage(maxFileSize) }
            .ifEmpty { throw ErrorResponseException("Images are too large!") }
    }

    private fun getRequestUrl(page: Int): String {
        val tagsQuery = tags.joinToString("%2C+") {
            it.replace(" ", "+")
        }
        return "$DERPIBOORU_API_DOMAIN/api/v1/json/search/images?q=$tagsQuery&filter_id=56027&sf=upvotes&page=$page&per_page=$DERPIBOORU_RESULTS_PER_PAGE"
    }

    private suspend fun DerpibooruImageBody.getImage(maxFileSize: Long): DataSource? {
        val fullDataSource = DataSource.fromUrl(representations.full)
        if (fullDataSource.size() <= maxFileSize) {
            return fullDataSource
        }
        val mediumDataSource = DataSource.fromUrl(representations.medium)
        if (mediumDataSource.size() <= maxFileSize) {
            return mediumDataSource
        }
        val smallDataSource = DataSource.fromUrl(representations.small)
        if (smallDataSource.size() <= maxFileSize) {
            return smallDataSource
        }
        return null
    }
}

@Serializable
private data class DerpibooruImagesResponse(
    val total: Int,
    val images: List<DerpibooruImageBody>
)

@Serializable
private data class DerpibooruImageBody(
    val representations: DerpibooruImageRepresentations,
)

@Serializable
private data class DerpibooruImageRepresentations(
    val full: String,
    val medium: String,
    val small: String,
)
