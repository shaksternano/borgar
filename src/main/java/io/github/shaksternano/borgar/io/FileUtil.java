package io.github.shaksternano.borgar.io;

import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.template.ResourceTemplateImageInfo;
import io.github.shaksternano.borgar.util.StringUtil;
import io.github.shaksternano.borgar.util.tenor.TenorMediaType;
import io.github.shaksternano.borgar.util.tenor.TenorUtil;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Contains static methods for dealing with files.
 */
public class FileUtil {

    private static final String ROOT_RESOURCE_DIRECTORY = Main.getRootPackage().replace(".", "/") + "/";

    /**
     * The maximum file size that is allowed to be downloaded, 100MB.
     */
    private static final long MAXIMUM_FILE_SIZE_TO_DOWNLOAD = 100 << 20;

    public static File createTempFile(String nameWithoutExtension, String extension) throws IOException {
        var extensionWithDot = extension.isBlank() ? "" : "." + extension;
        var file = Files.createTempFile(nameWithoutExtension, extensionWithDot).toFile();
        file.deleteOnExit();
        return file;
    }

    public static String getResourcePathInRootPackage(String resourcePath) {
        return ROOT_RESOURCE_DIRECTORY + resourcePath;
    }

    /**
     * Gets a resource bundled with the program.
     *
     * @param resourcePath The path to the resource.
     * @return The resource as an {@link InputStream}.
     * @throws FileNotFoundException If the resource could not be found.
     */
    public static InputStream getResourceInRootPackage(String resourcePath) throws FileNotFoundException {
        return getResource(getResourcePathInRootPackage(resourcePath));
    }

    private static InputStream getResource(String resourcePath) throws FileNotFoundException {
        InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath + "!");
        } else {
            return inputStream;
        }
    }

    public static CompletableFuture<NamedFile> downloadFile(String url) {
        CompletableFuture<String> urlFuture;
        try {
            urlFuture = TenorUtil.retrieveTenorMediaUrl(url, TenorMediaType.GIF_NORMAL, Main.getTenorApiKey());
        } catch (IllegalArgumentException e) {
            urlFuture = CompletableFuture.completedFuture(url);
        }
        return urlFuture.thenCompose(url1 -> {
            var fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(url1);
            var extension = com.google.common.io.Files.getFileExtension(url1);
            int index = extension.indexOf("?");
            if (index != -1) {
                extension = extension.substring(0, index);
            }
            try {
                var file = createTempFile(fileNameWithoutExtension, extension);
                downloadFile(url1, file);
                return CompletableFuture.completedFuture(new NamedFile(file, fileNameWithoutExtension, extension));
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        });
    }

    /**
     * Downloads a file from a web URL.
     *
     * @param url  The URL to download the file from.
     * @param file The file to download to.
     * @throws IOException If there was an error occurred while downloading the file.
     */
    public static void downloadFile(String url, File file) throws IOException {
        try (
            var outputStream = new FileOutputStream(file);
            var readableByteChannel = Channels.newChannel(new URL(url).openStream())
        ) {
            outputStream.getChannel().transferFrom(readableByteChannel, 0, MAXIMUM_FILE_SIZE_TO_DOWNLOAD);
        }
    }

    public static String getFileFormat(File file) {
        try {
            return ImageUtil.getImageFormat(file);
        } catch (Exception ignored) {
            return com.google.common.io.Files.getFileExtension(file.getName());
        }
    }

    public static String changeExtension(String fileName, @Nullable String newExtension) {
        String fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
        if (StringUtil.nullOrBlank(newExtension)) {
            return fileNameWithoutExtension;
        } else {
            return fileNameWithoutExtension + "." + newExtension;
        }
    }

    public static void validateResourcePathInRootPackage(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or blank!");
        } else {
            try (InputStream inputStream = ResourceTemplateImageInfo.class.getClassLoader().getResourceAsStream(getResourcePathInRootPackage(resourcePath))) {
                if (inputStream == null) {
                    throw new FileNotFoundException("File path not found: " + resourcePath);
                }
            } catch (IOException e) {
                throw new IOException("Error loading file with path " + resourcePath, e);
            }
        }
    }

    private static Set<String> getResourcePaths(String packageName) {
        Reflections reflections = new Reflections(packageName, Scanners.Resources);
        return reflections.getResources("(.*?)");
    }

    public static void forEachResource(String directory, BiConsumer<String, InputStream> operation) {
        // Remove trailing forward slashes
        String trimmedDirectory = directory.trim().replaceAll("/$", "");
        String packageName = Main.getRootPackage() + "." + trimmedDirectory.replaceAll(Pattern.quote("/"), ".");
        Set<String> resourcePaths = getResourcePaths(packageName);
        for (String resourcePath : resourcePaths) {
            try (InputStream inputStream = getResource(resourcePath)) {
                operation.accept(resourcePath, inputStream);
            } catch (IOException e) {
                Main.getLogger().error("Error loading resource " + resourcePath, e);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void delete(File... files) {
        for (var file : files) {
            if (file != null) {
                file.delete();
            }
        }
    }

    public static NamedFile changeFileExtension(File file, String fileName, String newExtension) throws IOException {
        var fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
        var output = FileUtil.createTempFile(fileNameWithoutExtension, newExtension);
        return new NamedFile(
            Files.move(
                file.toPath(),
                output.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            ).toFile(),
            fileNameWithoutExtension,
            newExtension
        );
    }

    public static CompletableFuture<HttpHeaders> getHeaders(String url) {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build();
        return HttpClient.newHttpClient()
            .sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .thenApply(HttpResponse::headers);
    }
}
