package io.github.shaksternano.mediamanipulator.util;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Contains static methods for dealing with files.
 */
public class  FileUtil {

    /**
     * The program's temporary directory.
     */
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "mediamanipulator");

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
     * @return The program's temporary directory.
     * This is guaranteed to be a directory instead of a file.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }

    /**
     * Deletes all the contents of the program's temporary directory.
     */
    public static void cleanTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true);
        try {
            FileUtils.cleanDirectory(tempDir);
        } catch (IllegalArgumentException ignored) {
        } catch (IOException e) {
            Main.LOGGER.warn("Error while cleaning temp directory!", e);
        }
    }

    /**
     * Gets a {@link File} with a unique name.
     * @param directory The directory the file will be located in.
     * @param fileName The name of the file.
     * @return A {@link File} with a unique name. If there is no other file with same name as the one provided,
     * the file will be created will have that name. If there is another file with the same name,
     * an incrementing number will be appended to the end of the file name. For example, if the provided file name
     * is {@code text_file.txt}, and there is another file with the same name, the file will be created as
     * {@code text_file1.txt}. If there is also a file called {@code text_file1.txt}, the file will be created as
     * {@code text_file2.txt}, and so on.
     */
    public static File getUniqueFile(@Nullable File directory, String fileName) {
        return getUniqueFile(new File(directory, fileName), false);
    }

    /**
     * Gets a {@link File} with a unique name.
     * @param file The file to get a unique name for.
     * @param isDirectory Whether the file is a directory.
     * @return A {@link File} with a unique name. If there is no other file with same name as the file provided,
     * the file will be created will have that name. If there is another file with the same name,
     * an incrementing number will be appended to the end of the file name. For example, if the provided file name
     * is {@code text_file.txt}, and there is another file with the same name, the file will be created as
     * {@code text_file1.txt}. If there is also a file called {@code text_file1.txt}, the file will be created as
     * {@code text_file2.txt}, and so on.
     */
    public static File getUniqueFile(File file, boolean isDirectory) {
        int num = 1;

        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String fileExtension = Files.getFileExtension(file.getName());
        String fileDirectory = file.getParent();

        while ((!isDirectory && file.exists()) || (isDirectory && file.isFile())) {
            String fileName = fileNameWithoutExtension + (num++);

            if (!fileExtension.isEmpty()) {
                fileName = fileName + "." + fileExtension;
            }

            file = new File(fileDirectory, fileName);
        }

        return file;
    }

    /**
     * Gets a unique file that will be created in the program's temporary directory.
     * @param fileName The name of the file.
     * @return A {@link File} with a unique name. If there is no other file with same name as the one provided,
     * the file will be created will have that name. If there is another file with the same name,
     * an incrementing number will be appended to the end of the file name. For example, if the provided file name
     * is {@code text_file.txt}, and there is another file with the same name, the file will be created as
     * {@code text_file1.txt}. If there is also a file called {@code text_file1.txt}, the file will be created as
     * {@code text_file2.txt}, and so on.
     */
    public static File getUniqueTempFile(String fileName) {
        File tempFile = getUniqueFile(getTempDirectory(), fileName);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Gets the name of a file from a path.
     * @param path The path to get the name of the file from.
     * @return The name of the file.
     */
    public static String getFileName(String path) {
        int index = path.lastIndexOf('/');
        if (index >= 0) {
            return path.substring(index + 1);
        } else {
            return path;
        }
    }

    /**
     * Gets a resource bundled with the program.
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
     * @param file The file whose name the string will be appended to.
     * @param toAppend The string to append.
     * @return The file with the string appended to the end of its name.
     */
    public static File appendName(File file, String toAppend) {
        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String extension = Files.getFileExtension(file.getName());
        return new File(fileNameWithoutExtension + toAppend + "." + extension);
    }

    /**
     * Downloads a file from a web URL.
     * @param webUrl The URL to download the file from.
     * @param file The file to download to.
     * @throws IOException If there was an error occurred while downloading the file.
     */
    public static void downloadFile(String webUrl, File file) throws IOException {
        URL url = new URL(webUrl);
        try (
                FileOutputStream outputStream = new FileOutputStream(file);
                ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream())
        ) {
            outputStream.getChannel().transferFrom(readableByteChannel, 0, MAXIMUM_FILE_SIZE_TO_DOWNLOAD);
        }
    }
}
