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
        String fontFileName = "Futura_Condensed_Extra_Bold.otf";
        File fontFile = FileUtil.getUniqueTempFile(fontFileName);

        boolean success = FileUtil.getResourceAsFile("font/" + fontFileName, fontFile);
        String fontLoadError = "Error loading font file!";
        if (success) {
            try {
                FUTURA_CONDENSED_EXTRA_BOLD = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FUTURA_CONDENSED_EXTRA_BOLD);
            } catch (FontFormatException | IOException e) {
                Main.LOGGER.error(fontLoadError, e);
            }
        } else {
            Main.LOGGER.error(fontLoadError);
        }
    }

    public static Font getCaptionFont() {
        return FUTURA_CONDENSED_EXTRA_BOLD == null ? new Font("Futura", Font.BOLD, 20) : FUTURA_CONDENSED_EXTRA_BOLD;
    }
}
