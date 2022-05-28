package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import io.github.shaksternano.mediamanipulator.command.SonicSaysCommand;
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
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A manipulator that works with image based media.
 */
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

    @SuppressWarnings("UnusedAssignment")
    @Override
    public File caption(File media, String fileFormat, String[] words, Map<String, Drawable> nonTextParts) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFrame(0).getImage();

        int width = firstImage.getWidth();

        Font font = Fonts.getCustomFont("futura_condensed_extra_bold").deriveFont(width / 10F);
        int padding = (int) (width * 0.04);
        Graphics2D originalGraphics = firstImage.createGraphics();

        ImageUtil.configureTextDrawSettings(originalGraphics);

        originalGraphics.setFont(font);

        CompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(words)
                .build(TextAlignment.CENTER, width - (padding * 2));

        int fillHeight = paragraph.getHeight(originalGraphics) + (padding * 2);
        originalGraphics.dispose();

        firstImage.flush();
        firstImage = null;

        ImageMediaBuilder builder = new ImageMediaBuilder();

        int paragraphFrameCount = paragraph.getFrameCount();
        if (paragraphFrameCount == 1) {
            for (Frame frame : imageMedia) {
                BufferedImage image = frame.getImage();
                BufferedImage captionedImage = drawCaption(image, fillHeight, padding, paragraph, font);
                builder.add(new AwtFrame(captionedImage, frame.getDuration()));
            }
        } else {
            List<BufferedImage> images = imageMedia.toBufferedImages();
            List<BufferedImage> normalisedImages = CollectionUtil.keepEveryNthElement(images, Frame.GIF_MINIMUM_FRAME_DURATION, Image::flush);
            List<BufferedImage> extendedImages = CollectionUtil.extendLoop(normalisedImages, paragraphFrameCount);

            images = null;
            normalisedImages = null;

            BufferedImage previousImage = null;

            Iterator<BufferedImage> imageIterator = extendedImages.iterator();
            while (imageIterator.hasNext()) {
                BufferedImage image = imageIterator.next();

                if (image.equals(previousImage) && paragraph.sameAsPreviousFrame()) {
                    builder.increaseLastFrameDuration(Frame.GIF_MINIMUM_FRAME_DURATION);
                } else {
                    BufferedImage captionedImage = drawCaption(image, fillHeight, padding, paragraph, font);
                    builder.add(new AwtFrame(captionedImage, Frame.GIF_MINIMUM_FRAME_DURATION));
                    previousImage = image;
                }

                imageIterator.remove();
            }
        }

        String outputFormat;
        String outputExtension;
        if (paragraphFrameCount > 1 && !imageMedia.isAnimated()) {
            outputFormat = "gif";
            outputExtension = "." + outputFormat;
        } else {
            outputFormat = fileFormat;
            outputExtension = Files.getFileExtension(media.getName());

            if (!outputExtension.isEmpty()) {
                outputExtension = "." + outputExtension;
            }
        }

        ImageMedia result = builder.build();
        File outputFile = FileUtil.getUniqueTempFile("captioned" + outputExtension);
        ImageWriters.write(result, outputFile, outputFormat);
        return outputFile;
    }

    @Override
    public File sonicSaysText(String[] words, Map<String, Drawable> nonTextParts) throws IOException {
        File outputFile;

        if (words.length == 0) {
            try (InputStream inputStream = FileUtil.getResource(SonicSaysCommand.IMAGE_PATH)) {
                outputFile = FileUtil.getUniqueTempFile(SonicSaysCommand.IMAGE_NAME);
                FileUtils.copyInputStreamToFile(inputStream, outputFile);
            }
        } else {
            int speechBubbleCentreY = SonicSaysCommand.SPEECH_BUBBLE_Y + (SonicSaysCommand.SPEECH_BUBBLE_HEIGHT / 2);

            BufferedImage sonicSays = ImageUtil.getImageResource(SonicSaysCommand.IMAGE_PATH);
            Graphics2D sonicSaysGraphics = sonicSays.createGraphics();

            Font font = Fonts.getCustomFont("bitstream_vera_sans").deriveFont(SonicSaysCommand.SPEECH_BUBBLE_WIDTH / 10F);
            sonicSaysGraphics.setFont(font);
            sonicSaysGraphics.setColor(Color.WHITE);
            ImageUtil.configureTextDrawSettings(sonicSaysGraphics);

            ParagraphCompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                    .addWords(words)
                    .build(TextAlignment.CENTER, SonicSaysCommand.SPEECH_BUBBLE_WIDTH - SonicSaysCommand.DOUBLE_SPEECH_PADDING);

            int maxParagraphHeight = SonicSaysCommand.SPEECH_BUBBLE_HEIGHT - SonicSaysCommand.DOUBLE_SPEECH_PADDING;

            int paragraphHeight = DrawableUtil.fitHeight(maxParagraphHeight, paragraph, sonicSaysGraphics);

            sonicSaysGraphics.dispose();

            int paragraphY = speechBubbleCentreY - (paragraphHeight / 2);

            String outputFileName = "sonic_says.";
            String outputFormat;

            int paragraphFrameCount = paragraph.getFrameCount();
            if (paragraphFrameCount == 1) {
                outputFormat = Files.getFileExtension(SonicSaysCommand.IMAGE_PATH);
            } else {
                outputFormat = "gif";
            }
            outputFileName += outputFormat;

            ImageMediaBuilder builder = new ImageMediaBuilder();

            for (int i = 0; i < paragraphFrameCount; i++) {
                if (paragraph.sameAsPreviousFrame() && !builder.isEmpty()) {
                    builder.increaseLastFrameDuration(Frame.GIF_MINIMUM_FRAME_DURATION);
                } else {
                    BufferedImage sonicSaysText = new BufferedImage(sonicSays.getWidth(), sonicSays.getHeight(), sonicSays.getType());
                    Graphics2D sonicSaysTextGraphics = sonicSaysText.createGraphics();

                    sonicSaysTextGraphics.drawImage(sonicSays, 0, 0, null);

                    sonicSaysTextGraphics.setFont(font);
                    sonicSaysTextGraphics.setColor(Color.WHITE);
                    ImageUtil.configureTextDrawSettings(sonicSaysTextGraphics);

                    paragraph.draw(sonicSaysTextGraphics, SonicSaysCommand.SPEECH_BUBBLE_X + SonicSaysCommand.SPEECH_BUBBLE_PADDING, paragraphY);

                    sonicSaysTextGraphics.dispose();
                    builder.add(new AwtFrame(sonicSaysText, Frame.GIF_MINIMUM_FRAME_DURATION));
                }
            }

            ImageMedia result = builder.build();
            outputFile = FileUtil.getUniqueTempFile(outputFileName);
            ImageWriters.write(result, outputFile, outputFormat);
        }

        return outputFile;
    }

    @Override
    public File sonicSaysImage(File media, String fileFormat) throws IOException {
        BufferedImage sonicSays = ImageUtil.getImageResource(SonicSaysCommand.IMAGE_PATH);
        int imageWidth = SonicSaysCommand.SPEECH_BUBBLE_WIDTH - SonicSaysCommand.DOUBLE_SPEECH_PADDING;
        int imageHeight = SonicSaysCommand.SPEECH_BUBBLE_HEIGHT - SonicSaysCommand.DOUBLE_SPEECH_PADDING;
        return applyToEachFrame(media, fileFormat, image -> {
            BufferedImage sonicSaysImage = new BufferedImage(sonicSays.getWidth(), sonicSays.getHeight(), sonicSays.getType());
            Graphics2D sonicSaysTextGraphics = sonicSaysImage.createGraphics();

            sonicSaysTextGraphics.drawImage(sonicSays, 0, 0, null);

            BufferedImage resizedImage = ImageUtil.fit(image, imageWidth, imageHeight);

            int imageX = SonicSaysCommand.SPEECH_BUBBLE_X + SonicSaysCommand.SPEECH_BUBBLE_PADDING + (imageWidth - resizedImage.getWidth()) / 2;
            int imageY = SonicSaysCommand.SPEECH_BUBBLE_Y + SonicSaysCommand.SPEECH_BUBBLE_PADDING + (imageHeight - resizedImage.getHeight()) / 2;

            sonicSaysTextGraphics.drawImage(resizedImage, imageX, imageY, null);

            return sonicSaysImage;
        }, "sonic_says");
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

    @SuppressWarnings("UnusedAssignment")
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

    @SuppressWarnings("UnusedAssignment")
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
    public File compress(File media, String fileFormat, @Nullable Guild guild) throws IOException {
        if (media.length() > DiscordUtil.getMaxUploadSize(guild)) {
            float ratio = (float) DiscordUtil.getMaxUploadSize(guild) / media.length();
            media = resize(media, fileFormat, ratio, false, false);

            boolean reduceResolution = true;
            while (media.length() > DiscordUtil.getMaxUploadSize(guild)) {
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

    private static BufferedImage drawCaption(BufferedImage image, int fillHeight, int padding, Drawable paragraph, Font font) {
        BufferedImage captionedImage = new BufferedImage(image.getWidth(), image.getHeight() + fillHeight, image.getType());
        Graphics2D graphics = captionedImage.createGraphics();
        ImageUtil.configureTextDrawSettings(graphics);

        graphics.drawImage(image, 0, fillHeight, null);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, captionedImage.getWidth(), fillHeight);

        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
        paragraph.draw(graphics, padding, padding);

        graphics.dispose();

        return captionedImage;
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

        List<Frame> frames = imageMedia.parallelStream().map(frame -> {
            BufferedImage unmodifiedImage = frame.getImage();
            BufferedImage modifiedImage = operation.apply(unmodifiedImage);
            int duration = frame.getDuration();
            frame.flush();
            return (Frame) new AwtFrame(modifiedImage, duration);
        }).toList();

        ImageMedia outputImage = ImageMediaBuilder.fromCollection(frames);

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
