package io.github.shaksternano.mediamanipulator.util.tenor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.JsonUtil;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * For dealing with the Tenor API.
 */
public class TenorUtil {

    /**
     * Gets the direct file URL from a Tenor link.
     *
     * @param url       The Tenor URL.
     * @param mediaType The media type to get the URL of.
     * @param apiKey    The Tenor API key to use.
     * @return The direct file URL.
     */
    public static Optional<String> getTenorMediaUrl(String url, TenorMediaType mediaType, String apiKey) {
        String urlWithoutPrefix = url.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","");

        if (urlWithoutPrefix.startsWith("tenor.com/")) {
            String mediaId = url.substring(url.lastIndexOf("-") + 1);
            String requestUrl = "https://g.tenor.com/v1/gifs?key=" + apiKey + "&ids=" + mediaId;

            JsonElement request = get(requestUrl);

            Optional<JsonElement> resultsArrayElementOptional = JsonUtil.getNestedElement(request, "results");
            Optional<JsonElement> resultElementOptional = JsonUtil.getArrayElement(resultsArrayElementOptional.orElse(null), 0);
            Optional<JsonElement> mediaArrayElementOptional = JsonUtil.getNestedElement(resultElementOptional.orElse(null), "media");
            Optional<JsonElement> mediaElementOptional = JsonUtil.getArrayElement(mediaArrayElementOptional.orElse(null), 0);
            Optional<JsonElement> mediaUrlElementOptional = JsonUtil.getNestedElement(mediaElementOptional.orElse(null), mediaType.getKey(), "url");

            if (mediaUrlElementOptional.isPresent()) {
                JsonElement mediaUrlElement = mediaUrlElementOptional.orElseThrow();

                if (mediaUrlElement.isJsonPrimitive()) {
                    JsonPrimitive mediaUrlPrimitive = mediaUrlElement.getAsJsonPrimitive();

                    if (mediaUrlPrimitive.isString()) {
                        return Optional.of(mediaUrlPrimitive.getAsString());
                    }
                }
            }

            Main.LOGGER.error("Error while getting Tenor media URL from Tenor URL " + url + "!");
            Main.LOGGER.error("Erroneous Tenor JSON contents:\n" + request);
        }

        return Optional.empty();
    }

    /**
     * Construct and run a GET request.
     *
     * @param url The URL to request.
     * @return The response as a {@link JsonElement}.
     */
    private static JsonElement get(String url) {
        HttpURLConnection connection = null;
        try {
            // Get request
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("HTTP Code: '%1$s' from '%2$s'", statusCode, url);
                throw new ConnectException(error);
            }

            // Parse response
            return parser(connection);
        } catch (Exception ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return JsonUtil.EMPTY;
    }

    /**
     * Parse the response into JSONObject.
     *
     * @param connection The connection to parse.
     * @return The response as a {@link JsonElement}.
     */
    private static JsonElement parser(HttpURLConnection connection) {
        char[] buffer = new char[1024 * 4];
        int characterCount;

        try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();

            while (-1 != (characterCount = reader.read(buffer))) {
                writer.write(buffer, 0, characterCount);
            }

            return JsonParser.parseString(writer.toString());
        } catch (IOException ignored) {
        }

        return JsonUtil.EMPTY;
    }
}
