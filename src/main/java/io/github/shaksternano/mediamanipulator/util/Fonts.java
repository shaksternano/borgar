package io.github.shaksternano.mediamanipulator.util;

import io.github.shaksternano.mediamanipulator.Main;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Fonts {

    private static Font FUTURA_CONDENSED_EXTRA_BOLD;

    public static void registerFonts() {
        try (InputStream fontStream = FileUtil.getResource("font/futura_condensed_extra_bold.otf")) {
            FUTURA_CONDENSED_EXTRA_BOLD = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FUTURA_CONDENSED_EXTRA_BOLD);
        } catch (FontFormatException | IOException e) {
            Main.LOGGER.error("Error loading font file!", e);
        }
    }

    public static Font getCaptionFont() {
        return FUTURA_CONDENSED_EXTRA_BOLD == null ? new Font("Futura", Font.BOLD, 20) : FUTURA_CONDENSED_EXTRA_BOLD;
    }
}
