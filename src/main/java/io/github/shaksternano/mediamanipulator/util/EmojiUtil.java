package io.github.shaksternano.mediamanipulator.util;

import emoji4j.Emoji;
import emoji4j.EmojiManager;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class EmojiUtil {

    private static final Pattern shortCodePattern = Pattern.compile(":(\\w+):");

    /**
     * Get emoji by unicode, short code, decimal html entity or hexadecimal html
     * entity
     *
     * @param code unicode, short code, decimal html entity or hexadecimal html
     * @return Emoji
     */
    public static Emoji getEmoji(String code) {

        Matcher m = shortCodePattern.matcher(code);


        // Test for shortcode with colons
        if (m.find()) {
            code = m.group(1);
        }

        String emojiCode = code;

        return EmojiManager.data().stream()
                .filter(e -> e.getEmoji().equals(emojiCode) ||
                        e.getHexHtml().equalsIgnoreCase(emojiCode) ||
                        e.getDecimalHtml().equalsIgnoreCase(emojiCode) ||
                        e.getDecimalSurrogateHtml().equalsIgnoreCase(emojiCode) ||
                        e.getHexHtmlShort().equalsIgnoreCase(emojiCode) ||
                        e.getDecimalHtmlShort().equalsIgnoreCase(emojiCode) ||
                        collectionToStream(e.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(emojiCode)) ||
                        collectionToStream(e.getEmoticons()).anyMatch(emoticon -> emoticon.equalsIgnoreCase(emojiCode)))
                .findFirst()
                .orElse(null);
    }

    private static Stream<String> collectionToStream(Collection<String> collection) {
        return Optional.ofNullable(collection).stream().flatMap(Collection::stream);
    }

    /**
     * Checks if an Emoji exists for the unicode, short code, decimal or
     * hexadecimal html entity
     *
     * @param code unicode, short code, decimal html entity or hexadecimal html
     * @return is emoji
     */
    public static boolean isEmoji(String code) {
        return getEmoji(code) != null;
    }
}
