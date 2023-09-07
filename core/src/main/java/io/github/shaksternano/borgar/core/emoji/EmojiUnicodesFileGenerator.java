package io.github.shaksternano.borgar.core.emoji;

import io.github.shaksternano.borgar.core.util.GithubUtil;
import io.github.shaksternano.borgar.core.util.MiscUtil;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generates a file containing emoji unicodes.
 */
public class EmojiUnicodesFileGenerator {

    /**
     * The program's {@link Logger}.
     */
    private static final Logger LOGGER = MiscUtil.createLogger("Emoji Unicodes File Generator");

    /**
     * The name of the owner of the Twemoji GitHub repository.
     */
    private static final String REPOSITORY_OWNER = "twitter";

    /**
     * The name of the Twemoji GitHub repository.
     */
    private static final String REPOSITORY_NAME = "twemoji";

    private static final String EMOJI_UNICODES_FILE_NAME = "emoji_unicodes.txt";

    /**
     * The program's main class.
     *
     * @param args The program arguments.
     */
    public static void main(String[] args) {
        generateEmojiUnicodesFile();
    }

    /**
     * Generates a file containing the unicodes of all the characters that have a
     * corresponding <a href="https://github.com/twitter/twemoji">Twemoji</a> image.
     * The relative path of the generated file will be {@code src/main/resources/emoji/emoji_unicodes.txt}.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void generateEmojiUnicodesFile() {
        LOGGER.info("Starting!");
        long startTime = System.currentTimeMillis();

        File directory = new File(EmojiUtil.EMOJI_FILES_DIRECTORY);
        directory.mkdirs();
        if (directory.isDirectory()) {
            Optional<String> shaOptional = GithubUtil.getLatestReleaseTagCommitSha(REPOSITORY_OWNER, REPOSITORY_NAME);
            shaOptional.ifPresentOrElse(
                sha -> {
                    List<String> fileNames = GithubUtil.listFiles(REPOSITORY_OWNER, REPOSITORY_NAME, sha, "assets", "72x72");
                    if (fileNames.isEmpty()) {
                        LOGGER.error("Failed to load emoji unicodes, could not retrieve any file names!");
                    } else {
                        Stream<String> emojiUnicodeStream = fileNames.stream()
                            .map(com.google.common.io.Files::getNameWithoutExtension)
                            .map(String::toLowerCase);
                        Iterable<String> emojiUnicodeIterable = emojiUnicodeStream::iterator;
                        File emojiUnicodesFile = new File(directory, EMOJI_UNICODES_FILE_NAME);
                        try {
                            Files.write(emojiUnicodesFile.toPath(), emojiUnicodeIterable);

                            long totalTime = System.currentTimeMillis() - startTime;
                            LOGGER.info("Created emoji unicodes file \"" + emojiUnicodesFile + "\" in " + totalTime + "ms!");
                        } catch (IOException e) {
                            LOGGER.error("Failed to create emoji unicodes file under \"" + emojiUnicodesFile + "\"!", e);
                        }
                    }
                },
                () -> LOGGER.error("Failed to load emoji unicodes, could not get the latest release tag commit SHA!")
            );
        } else if (directory.isFile()) {
            LOGGER.error("Failed to create emoji unicodes file! The directory path \"" + directory + "\" already exists as a file!");
        } else {
            LOGGER.error("Failed to create emoji unicodes file! Could not create parent directory \"" + directory + "\"!");
        }
    }
}
