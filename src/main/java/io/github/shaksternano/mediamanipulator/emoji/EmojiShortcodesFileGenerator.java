package io.github.shaksternano.mediamanipulator.emoji;

import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

public class EmojiShortcodesFileGenerator {

    /**
     * The program's {@link Logger}.
     */
    private static final Logger LOGGER = MiscUtil.createLogger("Emoji Shortcodes File Generator");
    private static final String EMOJI_SHORTCODES_FILE_NAME = "emojis.json";

    public static void main(String[] args) {
        generateEmojiShortCodesFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateEmojiShortCodesFile() {
        LOGGER.info("Starting!");
        long startTime = System.currentTimeMillis();

        File directory = new File(EmojiUtil.EMOJI_FILES_DIRECTORY);
        directory.mkdirs();
        if (directory.isDirectory()) {
            File emojiShortcodesFile = new File(directory, EMOJI_SHORTCODES_FILE_NAME);
            try {
                FileUtil.downloadFile("https://raw.githubusercontent.com/ArkinSolomon/discord-emoji-converter/master/emojis.json", emojiShortcodesFile);

                long totalTime = System.currentTimeMillis() - startTime;
                LOGGER.info("Created emoji shortcodes file \"" + emojiShortcodesFile + "\" in " + totalTime + "ms!");
            } catch (IOException e) {
                LOGGER.error("Error downloading emoji shortcodes file!", e);
            }
        } else if (directory.isFile()) {
            LOGGER.error("Failed to create emoji shortcodes file! The directory path \"" + directory + "\" already exists as a file!");
        } else {
            LOGGER.error("Failed to create emoji shortcodes file! Could not create parent directory \"" + directory + "\"!");
        }
    }
}
