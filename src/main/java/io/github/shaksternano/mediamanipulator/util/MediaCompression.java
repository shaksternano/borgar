package io.github.shaksternano.mediamanipulator.util;

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
import java.util.ArrayList;
import java.util.List;

public class MediaCompression {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static BufferedImage compressImage(BufferedImage image) throws IOException {
        File compressedImageFile = FileUtil.getUniqueTempFile("compressed_image.jpg");
        OutputStream outputStream = new FileOutputStream(compressedImageFile);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();

        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(imageOutputStream);

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.05F);
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
        if (image.getColorModel().hasAlpha()) {
            BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = copy.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, copy.getWidth(), copy.getHeight());
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            image.flush();

            return copy;
        } else {
            return image;
        }
    }

    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, long fileSize, long targetSize) {
        if (fileSize > targetSize) {
            float frameRatio = ((float) fileSize / targetSize);
            frameRatio *= 6;
            return removeFrames(frames, (int) frameRatio);
        } else {
            return frames;
        }
    }

    public static List<DelayedImage> removeFrames(List<DelayedImage> frames, int frameRatio) {
        if (frames.size() <= 1) {
            return frames;
        } else {
            List<DelayedImage> keptFrames = new ArrayList<>();

            int keptIndex = -1;
            for (int i = 0; i < frames.size(); i++) {
                if (i % frameRatio == 0) {
                    keptFrames.add(frames.get(i));
                    keptIndex++;
                } else {
                    DelayedImage keptFrame = keptFrames.get(keptIndex);
                    int keptFrameDelay = keptFrame.getDelay();
                    int removedFrameDelay = frames.get(i).getDelay();
                    keptFrame.setDelay(keptFrameDelay + removedFrameDelay);
                }
            }

            return keptFrames;
        }
    }
}
