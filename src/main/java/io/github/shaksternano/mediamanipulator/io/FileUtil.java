package io.github.shaksternano.mediamanipulator.io;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorMediaType;
import io.github.shaksternano.mediamanipulator.util.tenor.TenorUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

/**
 * Contains static methods for dealing with files.
 */
public class FileUtil {

    /**
     * The program's temporary directory.
     */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "mediamanipulator";

    /**
     * The maximum file size that can be sent in a Discord message, 8MB.
     */
    public static final long DISCORD_MAXIMUM_FILE_SIZE = 8388608;

    /**
     * The maximum file size that is allowed to be downloaded, 100MB.
     */
    private static final long MAXIMUM_FILE_SIZE_TO_DOWNLOAD = 104857600;

    /**
     * Gets the program's temporary directory.
     *
     * @return The program's temporary directory.
     * This is guaranteed to be a directory instead of a file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true, false);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }

    /**
     * Deletes all the contents of the program's temporary directory.
     */
    public static void cleanTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true, false);
        try {
            FileUtils.cleanDirectory(tempDir);
        } catch (IllegalArgumentException ignored) {
        } catch (IOException e) {
            Main.getLogger().warn("Error while cleaning temp directory!", e);
        }
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
        String filePath = directory == null ? fileName : directory + File.separator + fileName;
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
        } else {
            while (file.exists()) {
                File tempDirectory = getUniqueFile(fileDirectory + File.separator + "temp", true, true);
                tempDirectory.mkdirs();
                tempDirectory.deleteOnExit();
                file = new DeletableParentFile(tempDirectory, name);
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
        File tempFile = getUniqueFile(getTempDirectory().toString(), fileName);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Gets a resource bundled with the program.
     *
     * @param resourcePath The path to the resource.
     * @return The resource as an {@link InputStream}.
     * @throws IOException If the resource could not be found.
     */
    public static InputStream getResource(String resourcePath) throws IOException {
        InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        } else {
            return inputStream;
        }
    }

    /**
     * Appends a string to the end of a file name, before the file extension.
     *
     * @param file     The file whose name the string will be appended to.
     * @param toAppend The string to append.
     * @return The file with the string appended to the end of its name.
     */
    public static File appendName(File file, String toAppend) {
        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String extension = Files.getFileExtension(file.getName());
        return new File(fileNameWithoutExtension + toAppend + "." + extension);
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
            if (tenorMediaUrlOptional.isPresent()) {
                url = tenorMediaUrlOptional.orElseThrow();
            }

            String fileNameWithoutExtension = Files.getNameWithoutExtension(url);
            String extension = Files.getFileExtension(url);

            if (extension.isEmpty()) {
                extension = "png";
            } else {
                int index = extension.indexOf("?");
                if (index != -1) {
                    extension = extension.substring(0, index);
                }
            }

            String fileName = fileNameWithoutExtension + "." + extension;
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

    public static String getFileType(File file) {
        Optional<String> fileTypeOptional = Optional.empty();

        try {
            fileTypeOptional = ImageUtil.getImageType(file);
        } catch (IOException e) {
            Main.getLogger().error("Error getting file type from file " + file + "!", e);
        }

        return fileTypeOptional.orElse(Files.getFileExtension(file.getName()));
    }
}
