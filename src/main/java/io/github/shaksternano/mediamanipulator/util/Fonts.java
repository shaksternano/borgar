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

        try (InputStream fontStream = Main.class.getResourceAsStream("font/" + fontFileName)) {
            if (fontStream != null) {
                File fontFile = FileUtil.getUniqueTempFile(fontFileName);
                fontFile.deleteOnExit();

                FileUtils.copyInputStreamToFile(fontStream, fontFile);

                FUTURA_CONDENSED_EXTRA_BOLD = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(20F);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FUTURA_CONDENSED_EXTRA_BOLD);
            } else {
                Main.LOGGER.error("Could not find font file!");
            }
        } catch (IOException | FontFormatException e) {
            Main.LOGGER.error("Error loading font file!", e);
        }
    }

    public static Font getCaptionFont() {
        return FUTURA_CONDENSED_EXTRA_BOLD == null ? new Font("Futura", Font.BOLD, 20) : FUTURA_CONDENSED_EXTRA_BOLD;
    }
}
