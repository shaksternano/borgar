package io.github.shaksternano.borgar.discord.util;

import io.github.shaksternano.borgar.discord.Main;
import io.github.shaksternano.borgar.discord.io.FileUtil;

import java.awt.*;
import java.io.IOException;

/**
 * Handles custom fonts.
 */
public class Fonts {

    /**
     * Registers the custom fonts.
     */
    public static void registerFonts() {
        FileUtil.forEachResource("font", (resourcePath, inputStream) -> {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            } catch (FontFormatException | IOException e) {
                Main.getLogger().error("Could not load font " + resourcePath, e);
            }
        });
    }

    public static boolean fontExists(String fontName) {
        for (var font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            if (font.getName().equals(fontName)) {
                return true;
            }
        }
        return false;
    }
}
