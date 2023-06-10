package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.command.util.CommandResponse;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.Environment;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DownloadCommand extends BaseCommand<InputStream> {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public DownloadCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<CommandResponse<InputStream>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return MessageUtil.getUrl(event.getMessage())
            .thenCompose(urlOptional ->
                urlOptional.map(url ->
                    getDownloadUrl(url).thenCompose(streamUrl ->
                        FileUtil.getHeaders(streamUrl)
                            .thenCompose(headers -> {
                                var contentLength = getContentLength(headers);
                                if (contentLength > DiscordUtil.getMaxUploadSize(event)) {
                                    return new CommandResponse<InputStream>("File is too large!").asFuture();
                                }
                                var fileName = getFileName(headers);
                                try {
                                    var inputStream = new URL(streamUrl).openStream();
                                    return new CommandResponse<InputStream>(inputStream, fileName)
                                        .withResponseData(inputStream)
                                        .asFuture();
                                } catch (IOException e) {
                                    Main.getLogger().error("Failed to open stream from URL {}", streamUrl, e);
                                    return new CommandResponse<InputStream>("Error downloading file!").asFuture();
                                }
                            })
                    )
                ).orElseGet(() -> new CommandResponse<InputStream>("No URL found").asFuture())
            );
    }

    @Override
    public void handleFirstResponse(Message response, MessageReceivedEvent event, @Nullable InputStream responseData) {
        if (responseData != null) {
            try {
                responseData.close();
            } catch (IOException e) {
                Main.getLogger().error("Failed to close input stream", e);
            }
        }
    }

    private static CompletableFuture<String> getDownloadUrl(String url) {
        var cobaltApiDomain = Environment.getEnvVar("COBALT_API_DOMAIN").orElse("https://co.wuk.sh");
        var body = new JsonObject();
        body.addProperty("url", url);
        try {
            var request = HttpRequest.newBuilder(new URI(cobaltApiDomain + "/api/json"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
            return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    var responseBody = response.body();
                    try {
                        return JsonParser.parseString(responseBody)
                            .getAsJsonObject()
                            .get("url")
                            .getAsString();
                    } catch (RuntimeException e) {
                        Main.getLogger().error("Failed to parse Cobalt stream URL response: {}", responseBody, e);
                        throw e;
                    }
                });
        } catch (URISyntaxException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static long getContentLength(HttpHeaders headers) {
        var defaultContentLength = -1L;
        return headers.firstValue("Content-Length").map(contentLength -> {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                Main.getLogger().error("Invalid Content-Length header {} found in Cobalt stream URL response", contentLength);
                return defaultContentLength;
            }
        }).orElse(defaultContentLength);
    }

    private static String getFileName(HttpHeaders headers) {
        var defaultFileName = "video.mp4";
        return headers.firstValue("Content-Disposition").map(contentDisposition -> {
            var headerParts = contentDisposition.split("filename=");
            if (headerParts.length < 2) {
                Main.getLogger().error("Invalid Content-Disposition header {} found in Cobalt stream URL response", contentDisposition);
                return defaultFileName;
            }
            var fileName = headerParts[1].replace("\"", "");
            if (fileName.isBlank()) {
                Main.getLogger().error("Invalid Content-Disposition header {} found in Cobalt stream URL response", contentDisposition);
                return defaultFileName;
            }
            return fileName;
        }).orElse(defaultFileName);
    }
}
