package io.github.shaksternano.mediamanipulator.util;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileUtil {

    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "mediamanipulator");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static void cleanTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true);
        try {
            FileUtils.cleanDirectory(tempDir);
        } catch (IllegalArgumentException ignored) {
        } catch (IOException e) {
            Main.LOGGER.warn("Error while cleaning temp directory!", e);
        }
    }

    public static File getUniqueFile(@Nullable File directory, String fileName) {
        return getUniqueFile(new File(directory, fileName), false);
    }

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

    public static File getUniqueTempFile(String fileName) {
        File tempFile = getUniqueFile(getTempDirectory(), fileName);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static boolean getResourceAsFile(String resourcePath, File file) {
        try (InputStream inputStream = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                FileUtils.copyInputStreamToFile(inputStream, file);
                return true;
            } else {
                Main.LOGGER.error("Could not find resource file!");
            }
        } catch (IOException e) {
            Main.LOGGER.error("Error loading resource file!", e);
        }

        return false;
    }

    public static File appendName(File file, String toAppend) {
        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String extension = Files.getFileExtension(file.getName());
        return new File(fileNameWithoutExtension + toAppend + "." + extension);
    }

    public static void downloadFile(String webUrl, File file) throws IOException {
        URL url = new URL(webUrl);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
}
