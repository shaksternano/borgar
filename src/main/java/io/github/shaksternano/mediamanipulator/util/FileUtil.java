package io.github.shaksternano.mediamanipulator.util;

import com.google.common.io.Files;

import java.io.File;

public class FileUtil {

    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "mediamanipulator");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempDirectory() {
        File tempDir = getUniqueFile(TEMP_DIR, true);
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static File getUniqueFile(File file, boolean directory) {
        int num = 1;

        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String fileExtension = Files.getFileExtension(file.getName());
        String fileDirectory = file.getParent();

        while ((!directory && file.exists()) || (directory && file.isFile())) {
            String fileName = fileNameWithoutExtension + (num++);

            if (!fileExtension.isEmpty()) {
                fileName = fileName + "." + fileExtension;
            }

            file = new File(fileDirectory, fileName);
        }

        return file;
    }

    public static File getUniqueTempFile(String fileName) {
        File tempFile = getUniqueFile(new File(getTempDirectory(), fileName), false);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static File appendName(File file, String toAppend) {
        String fileNameWithoutExtension = Files.getNameWithoutExtension(file.getName());
        String extension = Files.getFileExtension(file.getName());
        return new File(fileNameWithoutExtension + toAppend + "." + extension);
    }
}
