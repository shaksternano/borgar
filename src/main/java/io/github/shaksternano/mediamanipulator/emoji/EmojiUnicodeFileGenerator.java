package io.github.shaksternano.mediamanipulator.emoji;

import io.github.shaksternano.mediamanipulator.util.github.GithubUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Generates a file containing emoji unicodes.
 */
public class EmojiUnicodeFileGenerator {

    /**
     * The program's {@link Logger}.
     */
    private static final Logger logger = LoggerFactory.getLogger("Emoji Unicodes File Generator");

    /**
     * The name of the owner of the Twemoji Github repository.
     */
    private static final String REPOSITORY_OWNER = "twitter";

    /**
     * The name of the Twemoji Github repository.
     */
    private static final String REPOSITORY_NAME = "twemoji";

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
        Optional<String> shaOptional = GithubUtil.getLatestReleaseTagCommitSha(REPOSITORY_OWNER, REPOSITORY_NAME);
        shaOptional.ifPresentOrElse(
                sha -> {
                    List<String> fileNames = GithubUtil.listFiles(REPOSITORY_OWNER, REPOSITORY_NAME, sha, "assets", "72x72");
                    if (fileNames.isEmpty()) {
                        logger.error("Failed to load emoji unicodes!");
                    } else {
                        Stream<String> emojiUnicodeStream = fileNames.stream()
                                .map(com.google.common.io.Files::getNameWithoutExtension)
                                .map(String::toLowerCase);
                        Iterable<String> emojiUnicodeIterable = emojiUnicodeStream::iterator;

                        File directory = new File("src/main/resources/emoji");
                        directory.mkdirs();
                        try {
                            Files.write(directory.toPath().resolve("emoji_unicodes.txt"), emojiUnicodeIterable);
                            logger.info("Created emoji unicodes file!");
                        } catch (IOException e) {
                            logger.error("Failed to create emoji unicodes file!", e);
                        }
                    }
                },
                () -> logger.error("Failed to get latest release tag commit SHA!")
        );
    }
}
