package io.github.shaksternano.borgar.core.io

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.io.IOException

/**
 * Executes an [HttpClient]'s request with the [urlString] and the parameters configured in [block].
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.request(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse {
    var method: HttpMethod = HttpMethod.Get
    return runCatching {
        request {
            url(urlString)
            block()
            method = this.method
        }
    }.getOrElse {
        // HttpClient exceptions don't contain complete stack traces
        throw IOException("Failed to execute ${method.value} request to $urlString", it)
    }
}

/**
 * Executes an [HttpClient]'s GET request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.get(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    get {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute GET request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s POST request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.post(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    post {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute POST request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s PUT request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.put(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    put {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute PUT request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s DELETE request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.delete(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    delete {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute DELETE request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s OPTIONS request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.options(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    options {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute OPTIONS request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s PATCH request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.patch(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    patch {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute PATCH request to $urlString", it)
}

/**
 * Executes an [HttpClient]'s HEAD request with the specified [url] and
 * an optional [block] receiving an [HttpRequestBuilder] for configuring the request.
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
suspend inline fun HttpClient.head(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = runCatching {
    head {
        url(urlString)
        block()
    }
}.getOrElse {
    // HttpClient exceptions don't contain complete stack traces
    throw IOException("Failed to execute HEAD request to $urlString", it)
}
