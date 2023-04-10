package io.github.shaksternano.mediamanipulator.io;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.ResourceContainerImageInfo;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorMediaType;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Contains static methods for dealing with files.
 */
public class FileUtil {

    private static File TEMP_DIR;
    private static final String ROOT_RESOURCE_DIRECTORY = Main.getRootPackage().replace(".", "/") + "/";

    /**
     * The maximum file size that is allowed to be downloaded, 100MB.
     */
    private static final long MAXIMUM_FILE_SIZE_TO_DOWNLOAD = 104857600;

    private static File createTempDir() throws IOException {
        File tempDir = Files.createTempDirectory("mediamanipulator").toFile();
        tempDir.deleteOnExit();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(tempDir)));
        return tempDir;
    }

    /**
     * Gets the program's temporary directory.
     *
     * @return The program's temporary directory.
     * This is guaranteed to be a directory instead of a file.
     */
    public static File getTempDir() {
        if (TEMP_DIR == null || !TEMP_DIR.isDirectory()) {
            try {
                TEMP_DIR = createTempDir();
            } catch (IOException e) {
                Main.getLogger().error("Failed to create temporary directory!", e);
                Main.shutdown(1);
            }
        }

        return TEMP_DIR;
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

    /**
     * Gets a unique file that will be created in the program's temporary directory.
     *
     * @param fileName The name of the file.
     * @return A {@link File} with a unique name.
     */
    public static File getUniqueTempFile(String fileName) {
        File tempFile = getUniqueFile(getTempDir().toString(), fileName);
        tempFile.deleteOnExit();
        return tempFile;
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
     * Downloads a file from a web URL.
     *
     * @param url  The URL to download the file from.
     * @param file The file to download to.
     * @throws IOException If there was an error occurred while downloading the file.
     */
    public static void downloadFile(String url, File file) throws IOException {
        try (
            FileOutputStream outputStream = new FileOutputStream(file);
            ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream())
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

    public static String changeFileName(String fileNameAndExtension, String newFileName) {
        String extension = com.google.common.io.Files.getFileExtension(fileNameAndExtension);
        return newFileName + "." + extension;
    }

    public static String changeExtension(String filePath, @Nullable String newExtension) {
        String fileNameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(filePath);
        if (newExtension == null || newExtension.isBlank()) {
            return fileNameWithoutExtension;
        } else {
            return fileNameWithoutExtension + "." + newExtension;
        }
    }

    public static void validateResourcePathInRootPackage(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or blank!");
        } else {
            try (InputStream inputStream = ResourceContainerImageInfo.class.getClassLoader().getResourceAsStream(getResourcePathInRootPackage(resourcePath))) {
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
