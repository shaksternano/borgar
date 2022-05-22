package io.github.shaksternano.mediamanipulator.emoji;

import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.io.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class EmojiUtil {

    private static Set<String> emojiUnicodeSet = ImmutableSet.of();

    public static void initEmojiUnicodeSet() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtil.getResource("emoji/emoji_unicodes.txt")))) {
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

    public static boolean isEmojiUnicode(String unicode) {
        return emojiUnicodeSet.contains(unicode.toLowerCase());
    }

    public static String getEmojiUrl(String unicode) {
        return "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/" + unicode + ".png";
    }
}
