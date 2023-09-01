package io.github.shaksternano.borgar.discord.util;

import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * A pattern to extract web URLs from a string.
     */
    private static final Pattern WEB_URL_PATTERN = Pattern.compile("\\b((?:https?|ftp|file)://[-a-zA-Z\\d+&@#/%?=~_|!:, .;]*[-a-zA-Z\\d+&@#/%=~_|])", Pattern.CASE_INSENSITIVE);

    /**
     * Extracts all web URLs from a string.
     *
     * @param text The text to extract the URLs from.
     * @return A list of all URLs in the text.
     */
    public static List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        for (var textPart : text.split("\\s+")) {
            var matcher = WEB_URL_PATTERN.matcher(textPart);
            while (matcher.find()) {
                urls.add(matcher.group());
            }
        }
        return urls;
    }

    public static String getStacktrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static boolean nullOrBlank(@Nullable String string) {
        return string == null || string.isBlank();
    }

    public static String splitCamelCase(String string) {
        return string.replaceAll(
            String.format(
                "%s|%s|%s",
                "(?<=[A-Z])(?=[A-Z][a-z])",
                "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"
            ),
            " "
        );
    }
}
