package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import io.github.shaksternano.mediamanipulator.media.template.ResourceTemplateImageInfo;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorMediaType;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorUtil;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
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
    private static final long MAXIMUM_FILE_SIZE_TO_DOWNLOAD = 104857600;

    public static File createTempFile(String nameWithoutExtension, String extension) throws IOException {
        var extensionWithDot = extension.isBlank() ? "" : "." + extension;
        var file = Files.createTempFile(nameWithoutExtension, extensionWithDot).toFile();
        file.deleteOnExit();
        return file;
    }

    /**
     * Gets a file that doesn't already exist by creating temporary folders
     * that don't exist and placing the file in there.
     *
     * @param directory The directory the file will be located in.
     * @param fileName  The name of the file.
     * @return A file that doesn't already exist.
     */
    public static File getUniqueFile(@Nullable String directory, String fileName) {
        String filePath = directory == null ? fileName : directory + File.separatorChar + fileName;
        return getUniqueFile(filePath, false, false);
    }

    /**
     * Gets a file that doesn't already exist by creating temporary folders
     * that don't exist and placing the file in there.
     *
     * @param filePath        The starting file path to get a unique file path from.
     * @param isDirectory     Whether the file is a directory.
     * @param uniqueDirectory Whether the directory should be unique if trying to get a directory.
     * @return A file that doesn't already exist.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getUniqueFile(String filePath, boolean isDirectory, boolean uniqueDirectory) {
        int num = 1;

        File file = new File(filePath);
        String name = file.getName();
        String fileDirectory = file.getParent();

        if (isDirectory) {
            while (uniqueDirectory ? file.exists() : file.isFile()) {
                String fileName = name + num;
                file = new File(fileDirectory, fileName);
                num++;
            }

            file.mkdirs();
        } else {
            while (file.exists()) {
                File tempDirectory = getUniqueFile(fileDirectory + File.separatorChar + "temp", true, true);
                tempDirectory.mkdirs();
                tempDirectory.deleteOnExit();
                file = new File(tempDirectory, name);
            }
        }

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

    /**
     * Downloads a file from a URL.
     *
     * @param url       The text to download the image from.
     * @param directory The directory to download the image to.
     * @return An {@link Optional} describing the image file.
     */
    public static Optional<File> downloadFile(String url, String directory) {
        try {
            Optional<String> tenorMediaUrlOptional = TenorUtil.getTenorMediaUrl(url, TenorMediaType.GIF_NORMAL, Main.getTenorApiKey());
            url = tenorMediaUrlOptional.orElse(url);
            String fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(url);
            String extension = com.google.common.io.Files.getFileExtension(url);

            int index = extension.indexOf("?");
            if (index != -1) {
                extension = extension.substring(0, index);
            }

            String fileName = fileNameWithoutExtension;
            if (!extension.isBlank()) {
                fileName += "." + extension;
            }

            File imageFile = getUniqueFile(directory, fileName);
            downloadFile(url, imageFile);
            return Optional.of(imageFile);
        } catch (IOException ignored) {
        }

        return Optional.empty();
    }

    /**
     * Downloads a file from a URL.
     *
     * @param url The text to download the image from.
     * @return An {@link Optional} describing the image file.
     */
    public static Optional<NamedFile> downloadFile(String url) {
        try {
            var tenorMediaUrlOptional = TenorUtil.getTenorMediaUrl(url, TenorMediaType.GIF_NORMAL, Main.getTenorApiKey());
            url = tenorMediaUrlOptional.orElse(url);
            var fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(url);
            var extension = com.google.common.io.Files.getFileExtension(url);
            int index = extension.indexOf("?");
            if (index != -1) {
                extension = extension.substring(0, index);
            }
            var extensionWithDot = extension.isBlank() ? "" : "." + extension;
            var fileName = fileNameWithoutExtension + extensionWithDot;
            var file = createTempFile(fileNameWithoutExtension, extension);
            downloadFile(url, file);
            return Optional.of(new NamedFile(file, fileName));
        } catch (IOException ignored) {
            return Optional.empty();
        }
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
        if (MiscUtil.nullOrBlank(newExtension)) {
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
}
