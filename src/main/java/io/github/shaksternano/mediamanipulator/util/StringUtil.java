package io.github.shaksternano.mediamanipulator.util;

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

    public static List<String> splitString(String string, int maxLength) {
        List<String> messages = new ArrayList<>();
        String[] lines = string.split(Pattern.quote("\n"));
        StringBuilder builder = new StringBuilder();
        int currentLength = 0;

        for (String line : lines) {
            if (currentLength + line.length() > maxLength) {
                messages.add(builder.toString());
                builder.setLength(0);

                if (line.length() > maxLength) {
                    for (int i = 0; i < line.length(); i++) {
                        if (i == 0 || i % maxLength != 0) {
                            builder.append(line.charAt(i));
                        } else {
                            messages.add(builder.toString());
                            builder.setLength(0);
                        }
                    }
                } else {
                    builder.append(line).append("\n");
                }
            } else {
                builder.append(line).append("\n");
            }

            currentLength = builder.length();
        }

        messages.add(builder.toString());

        return messages;
    }
}
