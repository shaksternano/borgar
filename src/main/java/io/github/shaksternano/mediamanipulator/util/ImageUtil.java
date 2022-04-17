package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.Streams;
import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageUtil {

    public static BufferedImage captionImage(BufferedImage image, String caption, Font font) {
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

        graphics.setFont(font.deriveFont((float) fillHeight / 2));

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

        graphics.setColor(Color.BLACK);

        FontMetrics metrics = graphics.getFontMetrics();
        int textX = (resizedImage.getWidth() - metrics.stringWidth(caption)) / 2;
        int textY = ((fillHeight - metrics.getHeight()) / 2) + metrics.getAscent();

        graphics.drawString(caption, textX, textY);
        graphics.dispose();

        return resizedImage;
    }

    public static BufferedImage stretch(BufferedImage image, float widthMultiplier, float heightMultiplier) {
        int newWidth = (int) (image.getWidth() * widthMultiplier);
        int newHeight = (int) (image.getHeight() * heightMultiplier);
        BufferedImage stretchedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D graphics = stretchedImage.createGraphics();
        graphics.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        return stretchedImage;
    }

    public static BufferedImage compressImage(BufferedImage image, int qualityReduction) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        image = Scalr.resize(image, image.getWidth() / qualityReduction, image.getHeight() / qualityReduction);
        return Scalr.resize(image, Scalr.Method.SPEED, imageWidth, imageHeight);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static BufferedImage compressImage(BufferedImage image) throws IOException {
        File compressedImageFile = FileUtil.getUniqueTempFile("compressed_image.jpg");
        OutputStream outputStream = new FileOutputStream(compressedImageFile);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(imageOutputStream);

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.8F);
        }

        writer.write(null, new IIOImage(removeAlpha(image), null, null), param);

        outputStream.close();
        imageOutputStream.close();
        writer.dispose();

        BufferedImage compressedImage = ImageIO.read(compressedImageFile);
        compressedImageFile.delete();
        return compressedImage;
    }

    private static BufferedImage removeAlpha(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return image;
        }

        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = copy.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, copy.getWidth(), copy.getHeight());
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        image.flush();

        return copy;
    }

    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, long fileSize, long targetSize) {
        if (fileSize > targetSize) {
            int frameRatio = (int) (fileSize / targetSize);
            frameRatio *= 6;
            return removeFrames(frames, frameRatio);
        } else {
            return frames;
        }
    }

    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, int frameRatio) {
        return Streams
                .mapWithIndex(frames.stream(), AbstractMap.SimpleImmutableEntry::new)
                .filter(entry -> entry.getValue() % frameRatio == 0)
                .map(Map.Entry::getKey)
                .peek(delayedImage -> delayedImage.setDelay(delayedImage.getDelay() * frameRatio))
                .collect(Collectors.toList());
    }
}
