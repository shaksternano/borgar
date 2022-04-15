package io.github.shaksternano.mediamanipulator.util;

import io.github.shaksternano.mediamanipulator.Main;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class GraphicsUtil {

    public static Font FUTURA_CONDENSED_EXTRA_BOLD;

    public static void registerFonts() {
        try {
            URL url = GraphicsUtil.class.getClassLoader().getResource("font/Futura_Condensed_Extra_Bold.otf");

            if (url != null) {
                FUTURA_CONDENSED_EXTRA_BOLD = Font.createFont(Font.TRUETYPE_FONT, new File(url.toURI())).deriveFont(10F);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FUTURA_CONDENSED_EXTRA_BOLD);
            } else {
                Main.LOGGER.error("Could not find font file!");
            }
        } catch (IOException | FontFormatException | URISyntaxException e) {
            Main.LOGGER.error("Error loading font file!", e);
        }
    }
}
