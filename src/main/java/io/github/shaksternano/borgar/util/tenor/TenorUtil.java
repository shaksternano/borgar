package io.github.shaksternano.borgar.util.tenor;

import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * For dealing with the Tenor API.
 */
public class TenorUtil {

    /**
     * Retrieves the direct file URL from a Tenor link.
     *
     * @param url       The Tenor URL.
     * @param mediaType The media type to get the URL of.
     * @param apiKey    The Tenor API key to use.
     * @return A future containing the direct file URL.
     */
    public static CompletableFuture<String> retrieveTenorMediaUrl(String url, String mediaType, String apiKey) {
        var invalidUrlMessage = "Invalid Tenor URL";
        try {
            var uri = new URI(url);
            if (uri.getHost().contains("tenor.com") && uri.getPath().startsWith("/view/")) {
                var mediaId = url.substring(url.lastIndexOf("-") + 1);
                var requestUrl = "https://g.tenor.com/v1/gifs?key=" + apiKey + "&ids=" + mediaId;
                var request = HttpRequest.newBuilder()
                    .uri(new URI(requestUrl))
                    .build();
                return HttpClient.newHttpClient()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> JsonParser.parseString(response.body())
                        .getAsJsonObject()
                        .getAsJsonArray("results")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("media")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject(mediaType)
                        .getAsJsonPrimitive("url")
                        .getAsString()
                    );
            } else {
                throw new IllegalArgumentException(invalidUrlMessage);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(invalidUrlMessage, e);
        }
    }

    public static CompletableFuture<String> retrieveTenorMediaUrl(String url, TenorMediaType mediaType, String apiKey) {
        return retrieveTenorMediaUrl(url, mediaType.getKey(), apiKey);
    }
}
