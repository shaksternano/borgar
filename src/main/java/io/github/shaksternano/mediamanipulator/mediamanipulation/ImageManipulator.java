package io.github.shaksternano.mediamanipulator.mediamanipulation;

import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.GraphicsUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public enum ImageManipulator implements MediaManipulator {

    INSTANCE;

    @Override
    public File caption(File media, String caption) throws IOException {
        BufferedImage image = ImageIO.read(media);

        int fillHeight = image.getHeight() / 4;

        BufferedImage resizedImage = new BufferedImage(image.getWidth(), image.getHeight() + fillHeight, image.getType());

        Graphics2D graphics = resizedImage.createGraphics();

        graphics.drawImage(image, 0, fillHeight, null);

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        graphics.setFont(GraphicsUtil.FUTURA_CONDENSED_EXTRA_BOLD.deriveFont((float) fillHeight / 2));

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

        graphics.setColor(Color.BLACK);

        FontMetrics metrics = graphics.getFontMetrics();
        int textX = (resizedImage.getWidth() - metrics.stringWidth(caption)) / 2;
        int textY = ((fillHeight - metrics.getHeight()) / 2) + metrics.getAscent();

        graphics.drawString(caption, textX, textY);
        graphics.dispose();

        String directory = media.getParent();
        String fileNameWithoutExtension = Files.getNameWithoutExtension(media.getName());
        String extension = Files.getFileExtension(media.getName());

        File captionedImage = FileUtil.getUniqueFile(new File(directory, fileNameWithoutExtension + "_captioned." + extension), false);

        ImageIO.write(resizedImage, extension, captionedImage);

        return captionedImage;
    }
}
