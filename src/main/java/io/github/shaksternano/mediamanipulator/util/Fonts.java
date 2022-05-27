package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.io.FileUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Handles custom fonts.
 */
public class Fonts {

    private static Map<String, Font> FONTS;

    private static final Set<String> FONT_FILES = ImmutableSet.of(
            "futura_condensed_extra_bold.otf",
            "bitstream_vera_sans.ttf"
    );

    /**
     * Registers the custom fonts.
     */
    public static void registerFonts() {
        ImmutableMap.Builder<String, Font> builder = new ImmutableMap.Builder<>();

        for (String fontFile : FONT_FILES) {
            try (InputStream inputStream = FileUtil.getResource("font/" + fontFile)) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                builder.put(Files.getNameWithoutExtension(fontFile), font);
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
            }
        }

        FONTS = builder.build();
    }

    public static Font getCustomFont(String fontName) {
        Font font = FONTS.get(fontName);

        if (font == null) {
            throw new IllegalArgumentException("Font not found: " + fontName);
        } else {
            return FONTS.get(fontName);
        }
    }
}
