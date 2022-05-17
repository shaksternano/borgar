package io.github.shaksternano.mediamanipulator.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * A pattern to extract web URLs from a string.
     */
    public static final Pattern WEB_URL_PATTERN = Pattern.compile("\\b((?:https?|ftp|file)://[-a-zA-Z\\d+&@#/%?=~_|!:, .;]*[-a-zA-Z\\d+&@#/%=~_|])", Pattern.CASE_INSENSITIVE);

    /**
     * Extracts all web URLs from a string.
     *
     * @param text The text to extract the URLs from.
     * @return A list of all URLs in the text.
     */
    public static List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();

        Matcher matcher = WEB_URL_PATTERN.matcher(text);

        while (matcher.find()) {
            urls.add(text.substring(matcher.start(0), matcher.end(0)));
        }

        return urls;
    }

    public static String getStacktrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
