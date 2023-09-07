package io.github.shaksternano.borgar.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkUtil {

    /**
     * Construct and run a GET request.
     *
     * @param url The URL to request.
     * @return The response as a {@link JsonElement}.
     */
    public static JsonElement httpGet(String url) {
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
