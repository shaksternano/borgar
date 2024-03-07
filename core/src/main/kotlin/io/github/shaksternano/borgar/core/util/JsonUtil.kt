package io.github.shaksternano.borgar.core.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

val JSON: Json = Json {
    @OptIn(ExperimentalSerializationApi::class)
    explicitNulls = false
    ignoreUnknownKeys = true
    prettyPrint = true
}

fun prettyPrintJson(json: String): String {
    val jsonElement = Json.parseToJsonElement(json)
    return JSON.encodeToString(JsonElement.serializer(), jsonElement)
}

fun prettyPrintJsonCatching(json: String): String = runCatching {
    prettyPrintJson(json)
}.getOrDefault(json)
