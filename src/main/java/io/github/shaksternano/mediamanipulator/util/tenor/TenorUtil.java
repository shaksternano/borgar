package io.github.shaksternano.mediamanipulator.util.tenor;

import com.google.gson.JsonElement;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.JsonUtil;
import io.github.shaksternano.mediamanipulator.util.NetworkUtil;

import java.net.URI;
import java.net.URISyntaxException;
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
        try {
            URI uri = new URI(url);
            if (uri.getHost().contains("tenor.com") && uri.getPath().startsWith("/view/")) {
                String mediaId = url.substring(url.lastIndexOf("-") + 1);
                String requestUrl = "https://g.tenor.com/v1/gifs?key=" + apiKey + "&ids=" + mediaId;

                JsonElement request = NetworkUtil.httpGet(requestUrl);

                Optional<JsonElement> resultsArrayElementOptional = JsonUtil.getNestedElement(request, "results");
                Optional<JsonElement> resultElementOptional = JsonUtil.getArrayElement(resultsArrayElementOptional.orElse(JsonUtil.EMPTY), 0);
                Optional<JsonElement> mediaArrayElementOptional = JsonUtil.getNestedElement(resultElementOptional.orElse(JsonUtil.EMPTY), "media");
                Optional<JsonElement> mediaElementOptional = JsonUtil.getArrayElement(mediaArrayElementOptional.orElse(JsonUtil.EMPTY), 0);
                Optional<JsonElement> mediaUrlElementOptional = JsonUtil.getNestedElement(mediaElementOptional.orElse(JsonUtil.EMPTY), mediaType.getKey(), "url");

                Optional<String> mediaUrlOptional = mediaUrlElementOptional.map(JsonUtil::getString);
                if (mediaUrlOptional.isPresent()) {
                    return mediaUrlOptional;
                } else {
                    Main.getLogger().error("Error while getting Tenor media URL from Tenor URL " + url + "!");
                    Main.getLogger().error("Erroneous Tenor JSON contents:\n" + request);
                }
            }
        } catch (URISyntaxException e) {
            Main.getLogger().error("Error parsing URL " + url + "!", e);
        }

        return Optional.empty();
    }
}
