package io.github.shaksternano.mediamanipulator.util;

import com.google.common.io.Files;

import java.io.File;

public class FileUtil {

    private static final File TEMP_DIR = getUniqueFile(new File(System.getProperty("java.io.tmpdir"), "mediamanipulator"), true);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTempDirectory() {
        TEMP_DIR.mkdirs();
        TEMP_DIR.deleteOnExit();
        return TEMP_DIR;
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
}
