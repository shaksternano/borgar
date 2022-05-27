package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.exception.InvalidArgumentException;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.CompositeDrawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.util.DrawableUtil;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.io.reader.util.ImageReaderRegistry;
import io.github.shaksternano.mediamanipulator.image.io.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.image.io.writer.util.ImageWriterRegistry;
import io.github.shaksternano.mediamanipulator.image.io.writer.util.ImageWriters;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.image.util.ImageMediaBuilder;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.CollectionUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * A manipulator that works with image based media.
 */
@SuppressWarnings("UnusedAssignment")
public class ImageManipulator implements MediaManipulator {

    private static final Set<String> ANIMATED_IMAGE_FORMATS = ImmutableSet.of(
            "gif"
    );

    private static File animatedOnlyOperation(File media, String fileFormat, Function<ImageMedia, ImageMedia> operation, String operationName, String staticImageErrorMessage) throws IOException {
        if (ANIMATED_IMAGE_FORMATS.contains(fileFormat.toLowerCase())) {
            ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);

            if (imageMedia.isAnimated()) {
                ImageMedia result = operation.apply(imageMedia);
                File output = FileUtil.getUniqueTempFile(operationName + "." + fileFormat);
                ImageWriters.write(result, output, fileFormat);
                return output;
            } else {
                throw new UnsupportedFileFormatException(staticImageErrorMessage);
            }
        } else {
            throw new UnsupportedFileFormatException(staticImageErrorMessage);
        }
    }

    @Override
    public File caption(File media, String fileFormat, String[] words, Map<String, Drawable> nonTextParts) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFrame(0).getImage();

        int width = firstImage.getWidth();
        int height = firstImage.getHeight();
        int type = firstImage.getType();

        Font font = Fonts.getCustomFont("futura_condensed_extra_bold").deriveFont(width / 10F);
        int padding = (int) (width * 0.04);
        Graphics2D originalGraphics = firstImage.createGraphics();

        ImageUtil.configureTextDrawSettings(originalGraphics);

        originalGraphics.setFont(font);

        CompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(words)
                .build(TextAlignment.CENTER, width - (padding * 2));

        int fillHeight = paragraph.getHeight(originalGraphics) + (padding * 2);
        int newHeight = height + fillHeight;
        originalGraphics.dispose();

        firstImage.flush();
        firstImage = null;
        imageMedia = null;

        return applyToEachFrame(media, fileFormat, image -> {
            BufferedImage resizedImage = new BufferedImage(width, newHeight, type);
            Graphics2D graphics = resizedImage.createGraphics();
            graphics.setFont(font);

            graphics.drawImage(image, 0, fillHeight, null);

            ImageUtil.configureTextDrawSettings(graphics);

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

            graphics.setColor(Color.BLACK);

            paragraph.draw(graphics, padding, padding);

            graphics.dispose();
            return resizedImage;
        }, "captioned");
    }

    @Override
    public File sonicSays(String[] words, Map<String, Drawable> nonTextParts) throws IOException {
        int speechBubbleX = 345;
        int speechBubbleY = 35;

        int speechBubbleWidth = 630;
        int speechBubbleHeight = 490;

        int speechBubbleCentreY = speechBubbleY + (speechBubbleHeight / 2);

        int padding = 50;
        int doubledPadding = padding * 2;

        BufferedImage sonic = ImageUtil.getImageResource("image/background/sonic.jpg");
        Graphics2D graphics = sonic.createGraphics();

        Font font = Fonts.getCustomFont("bitstream_vera_sans").deriveFont(speechBubbleWidth / 10F);
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        ImageUtil.configureTextDrawSettings(graphics);

        ParagraphCompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(words)
                .build(TextAlignment.CENTER, speechBubbleWidth - doubledPadding);

        int maxParagraphHeight = speechBubbleHeight - doubledPadding;

        int paragraphHeight = DrawableUtil.fitHeight(maxParagraphHeight, paragraph, graphics);

        int paragraphY = speechBubbleCentreY - (paragraphHeight / 2);

        paragraph.draw(graphics, speechBubbleX + padding, paragraphY);

        File output = FileUtil.getUniqueTempFile("sonic_says.jpg");
        ImageWriters.write(sonic, output, "jpg");

        return output;
    }

    @Override
    public File stretch(File media, String fileFormat, float widthMultiplier, float heightMultiplier, boolean raw) throws IOException {
        return applyToEachFrame(media, fileFormat, image -> ImageUtil.stretch(image, (int) (image.getWidth() * widthMultiplier), (int) (image.getHeight() * heightMultiplier), raw), "stretched");
    }

    @Override
    public File resize(File media, String fileFormat, float resizeMultiplier, boolean raw, boolean rename) throws IOException {
        return applyToEachFrame(media, fileFormat, image -> ImageUtil.resize(image, resizeMultiplier, raw), rename ? "resized" : null);
    }

    @Override
    public File pixelate(File media, String fileFormat, int pixelationMultiplier) throws IOException {
        return applyToEachFrame(
                media,
                fileFormat,
                image -> ImageUtil.stretch(
                        ImageUtil.stretch(
                                image,
                                image.getWidth() / pixelationMultiplier,
                                image.getHeight() / pixelationMultiplier,
                                true
                        ),
                        image.getWidth(),
                        image.getHeight(),
                        true
                ),
                "pixelated"
        );
    }

    @Override
    public File speechBubble(File media, String fileFormat, boolean cutOut) throws IOException {
        String speechBubblePath = cutOut ? "image/overlay/speech_bubble_2_partial.png" : "image/overlay/speech_bubble_1_partial.png";

        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFrame(0).getImage();

        int width = firstImage.getWidth();
        int height = firstImage.getHeight();

        BufferedImage speechBubble = ImageUtil.getImageResource(speechBubblePath);

        int minDimension = 3;
        if (width < minDimension) {
            throw new InvalidMediaException("Image width of " + width + " pixels is too small!");
        } else {
            if (speechBubble.getHeight() < speechBubble.getWidth()) {
                float scaleRatio = (float) width / speechBubble.getWidth();
                int newHeight = (int) (speechBubble.getHeight() * scaleRatio);

                if (newHeight < minDimension) {
                    throw new InvalidMediaException("Image height of " + height + " pixels is too small!");
                }
            }
        }

        firstImage.flush();
        firstImage = null;
        imageMedia = null;

        BufferedImage resizedSpeechBubble = ImageUtil.fitWidth(speechBubble, width);

        speechBubble.flush();
        speechBubble = null;

        String operationName = "speech_bubbled";
        if (cutOut) {
            final BufferedImage finalResizedSpeechBubble = resizedSpeechBubble;
            return applyToEachFrame(media, fileFormat, image -> ImageUtil.cutoutImage(image, finalResizedSpeechBubble, 0, 0, 0xFFFFFF), operationName);
        } else {
            BufferedImage filledSpeechBubble = ImageUtil.fill(resizedSpeechBubble, Color.WHITE);

            resizedSpeechBubble.flush();
            resizedSpeechBubble = null;

            return applyToEachFrame(media, fileFormat, image -> ImageUtil.overlayImage(image, filledSpeechBubble, 0, -filledSpeechBubble.getHeight(), true, null), operationName);
        }
    }

    @Override
    public File speed(File media, String fileFormat, float speedMultiplier) throws IOException {
        return animatedOnlyOperation(media, fileFormat, imageMedia -> {
            if (speedMultiplier != 1 && speedMultiplier > 0) {
                ImageMediaBuilder builder = new ImageMediaBuilder();

                for (Frame frame : imageMedia) {
                    builder.add(new AwtFrame(frame.getImage(), (int) (frame.getDuration() / speedMultiplier)));
                }

                ImageMedia modifiedDurations = builder.build();
                List<BufferedImage> bufferedFrames = modifiedDurations.toBufferedImages();
                List<BufferedImage> keptFrames = CollectionUtil.keepEveryNthElement(bufferedFrames, Frame.GIF_MINIMUM_FRAME_DURATION, Image::flush);

                ImageMedia newImageMedia = ImageMediaBuilder.fromBufferedImages(keptFrames);
                ImageMediaBuilder resultBuilder = new ImageMediaBuilder();

                for (Frame frame : newImageMedia) {
                    resultBuilder.add(new AwtFrame(frame.getImage(), frame.getDuration() * Frame.GIF_MINIMUM_FRAME_DURATION));
                }

                ImageMedia result = resultBuilder.build();

                if (result.isEmpty()) {
                    result = new ImageMediaBuilder().add(imageMedia.getFrame(0)).build();
                }

                return result;
            } else {
                throw new InvalidArgumentException("Speed multiplier " + speedMultiplier + " is not allowed!");
            }
        }, "changed_speed", "Cannot change the speed of a static image.");
    }

    @Override
    public File reduceFps(File media, String fileFormat, int fpsReductionRatio, boolean rename) throws IOException {
        return animatedOnlyOperation(
                media,
                fileFormat,
                imageMedia -> MediaCompression.removeFrames(imageMedia, fpsReductionRatio),
                rename ? "reduced_fps" : null,
                "Cannot reduce the FPS of a static image."
        );
    }

    @Override
    public File rotate(File media, String fileFormat, float degrees, @Nullable Color backgroundColor) throws IOException {
        return applyToEachFrame(media, fileFormat, image -> ImageUtil.rotate(image, degrees, null, null, backgroundColor), "rotated");
    }

    @Override
    public File spin(File media, String fileFormat, float speed, @Nullable Color backgroundColor) throws IOException {
        ImageMedia image = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> bufferedImages = image.toBufferedImages();
        List<BufferedImage> keptImages = CollectionUtil.keepEveryNthElement(bufferedImages, Frame.GIF_MINIMUM_FRAME_DURATION, Image::flush);

        image = null;
        bufferedImages = null;

        BufferedImage firstFrame = keptImages.get(0);

        int maxDimension = Math.max(firstFrame.getWidth(), firstFrame.getHeight());
        float absoluteSpeed = Math.abs(speed);

        int framesPerRotation = 150;
        if (absoluteSpeed >= 1) {
            framesPerRotation = Math.max((int) (framesPerRotation / absoluteSpeed), 1);
        }

        int size = framesPerRotation * ((keptImages.size() + (framesPerRotation - 1)) / framesPerRotation);
        Map<Integer, Frame> indexedFrames = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            int duration = Frame.GIF_MINIMUM_FRAME_DURATION;
            if (absoluteSpeed < 1) {
                duration /= absoluteSpeed;
            }

            indexedFrames.put(i, new AwtFrame(keptImages.get(i % keptImages.size()), duration));
        }

        keptImages = null;

        final int finalFramesPerRotation = framesPerRotation;
        List<Frame> rotatedFrames = indexedFrames.entrySet().parallelStream().map(frameEntry -> {
            float index = frameEntry.getKey();
            Frame originalFrame = frameEntry.getValue();
            BufferedImage originalImage = originalFrame.getImage();
            float angle = 360 * (index / finalFramesPerRotation);

            if (speed < 0) {
                angle = -angle;
            }

            BufferedImage rotatedImage = ImageUtil.rotate(originalImage, angle, maxDimension, maxDimension, backgroundColor);
            Frame frame = new AwtFrame(rotatedImage, originalFrame.getDuration());
            originalFrame.flush();
            return frame;
        }).collect(ImmutableList.toImmutableList());
        ImageMedia rotatedImage = ImageMediaBuilder.fromCollection(rotatedFrames);

        File outputFile = FileUtil.getUniqueTempFile("spun.gif");

        ImageWriters.write(rotatedImage, outputFile, "gif");

        return outputFile;
    }

    @Override
    public File makeGif(File media, String fileFormat, boolean justRenameFile) throws IOException {
        if (Files.getFileExtension(media.getName()).equalsIgnoreCase("gif")) {
            throw new UnsupportedFileFormatException("This file is already a GIF file!");
        } else {
            File gifFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "gif"));

            if (justRenameFile) {
                Files.move(media, gifFile);
            } else {
                ImageMedia nonGifImage = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
                ImageWriters.write(nonGifImage, gifFile, "gif");
            }

            return gifFile;
        }
    }

    @Override
    public File makePngAndTransparent(File media, String fileFormat) throws IOException {
        File pngFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "png"));
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, BufferedImage.TYPE_INT_ARGB);
        ImageWriters.write(imageMedia, pngFile, "png");
        return pngFile;
    }

    @Override
    public File makeIco(File media, String fileFormat) throws IOException {
        File icoFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "ico"));
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        ImageWriters.write(imageMedia, icoFile, "ico");
        return icoFile;
    }

    @Override
    public File compress(File media, String fileFormat) throws IOException {
        if (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
            float ratio = (float) FileUtil.DISCORD_MAXIMUM_FILE_SIZE / media.length();
            media = resize(media, fileFormat, ratio, false, false);

            boolean reduceResolution = true;
            while (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                if (reduceResolution || !ANIMATED_IMAGE_FORMATS.contains(fileFormat)) {
                    media = resize(media, fileFormat, 0.75F, false, false);
                } else {
                    media = reduceFps(media, fileFormat, 2, false);
                }

                reduceResolution = !reduceResolution;
            }

        }

        return media;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> readerFormats = ImageReaderRegistry.getSupportedFormats();
        Set<String> writerFormats = ImageWriterRegistry.getSupportedFormats();
        return CollectionUtil.intersection(readerFormats, writerFormats);
    }

    /**
     * Applies the given operation to the given image based file.
     *
     * @param media         The image based file to apply the operation to.
     * @param imageFormat   The image format of the file.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    private File applyToEachFrame(File media, String imageFormat, Function<BufferedImage, BufferedImage> operation, @Nullable String operationName) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, imageFormat, null);

        ImageMediaBuilder builder = new ImageMediaBuilder();

        for (Frame frame : imageMedia) {
            BufferedImage unmodifiedImage = frame.getImage();
            BufferedImage modifiedImage = operation.apply(unmodifiedImage);
            builder.add(new AwtFrame(modifiedImage, frame.getDuration()));
            frame.flush();
        }

        ImageMedia outputImage = builder.build();

        String outputName;
        if (operationName == null) {
            outputName = media.getName();
        } else {
            outputName = FileUtil.changeFileName(media.getName(), operationName);
        }

        File output = FileUtil.getUniqueTempFile(outputName);
        ImageWriters.write(outputImage, output, imageFormat);

        return output;
    }
}
