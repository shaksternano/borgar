package io.github.shaksternano.mediamanipulator.emoji;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.io.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EmojiUtil {

    public static final String EMOJI_FILES_DIRECTORY = "src/main/resources/" + FileUtil.getResourcePathInRootPackage("emoji");
    private static Set<String> emojiUnicodeSet = ImmutableSet.of();
    private static Map<String, String> emojiShortcodesToUrls = ImmutableMap.of();

    public static void initEmojiUnicodeSet() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtil.getResourceInRootPackage("emoji/emoji_unicodes.txt")))) {
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.add(line.toLowerCase());
            }
            emojiUnicodeSet = builder.build();
        } catch (IOException e) {
            Main.getLogger().error("Error while loading emoji unicodes!", e);
        }
    }

    public static void initEmojiShortCodesToUrlsMap() {
        try (Reader reader = new InputStreamReader(FileUtil.getResourceInRootPackage("emoji/emojis.json"))) {
            JsonElement emojiNamesElement = JsonParser.parseReader(reader);
            if (emojiNamesElement instanceof JsonObject emojiNamesObject) {
                emojiShortcodesToUrls = emojiNamesObject.entrySet()
                    .stream()
                    .filter(entry -> {
                        boolean isString = entry.getValue() instanceof JsonPrimitive primitive && primitive.isString();
                        if (!isString) {
                            Main.getLogger().error("Error parsing shortcodes file! The value {} of the entry with key {} is not a string!", entry.getValue(), entry.getKey());
                        }
                        return isString;
                    })
                    .map(entry -> {
                        String emoji = entry.getValue().getAsString();
                        String emojiUnicode = emoji.codePoints()
                            .mapToObj(Integer::toHexString)
                            .collect(Collectors.joining("-"));
                        return Map.entry(entry.getKey(), getEmojiUrl(emojiUnicode));
                    })
                    .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
            } else {
                Main.getLogger().error("Invalid emoji shortcodes JSON:\n{}", emojiNamesElement);
            }
        } catch (IOException e) {
            Main.getLogger().error("Error while loading emoji shortcodes!", e);
        } catch (JsonParseException e) {
            Main.getLogger().error("Error while parsing emoji shortcodes!", e);
        }
    }

    public static boolean isEmojiUnicode(String unicode) {
        return emojiUnicodeSet.contains(unicode.toLowerCase());
    }

    public static Optional<String> getEmojiUrlFromShortcode(String shortcode) {
        return Optional.ofNullable(emojiShortcodesToUrls.get(shortcode));
    }

    public static String getEmojiUrl(String unicode) {
        return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + unicode + ".png";
    }
}
