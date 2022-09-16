package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.AutocropOps;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.pixels.PixelsExtractor;
import io.github.shaksternano.mediamanipulator.exception.InvalidArgumentException;
import io.github.shaksternano.mediamanipulator.exception.InvalidMediaException;
import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import io.github.shaksternano.mediamanipulator.graphics.GraphicsUtil;
import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.OutlinedTextDrawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.ContainerImageInfo;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.CustomContainerImageInfo;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaderRegistry;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.image.util.AwtFrame;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.image.util.ImageMediaBuilder;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.image.writer.util.ImageWriterRegistry;
import io.github.shaksternano.mediamanipulator.image.writer.util.ImageWriters;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.CollectionUtil;
import io.github.shaksternano.mediamanipulator.util.DiscordUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.function.Function;

/**
 * A manipulator that works with image based media.
 */
public class ImageManipulator implements MediaManipulator {

    private static final Set<String> ANIMATED_IMAGE_FORMATS = ImmutableSet.of(
            "gif"
    );

    private static final Drawable EMPTY = new EmptyDrawable();

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
    public File caption(File media, String fileFormat, List<String> words, Map<String, Drawable> nonTextParts, boolean caption2) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFirstImage();

        int width = firstImage.getWidth();
        int height = firstImage.getHeight();

        int biggestDimension = Math.max(width, height);

        String fontName = caption2 ? "Helvetica Neue" : "Futura-CondensedExtraBold";
        float fontRatio = caption2 ? 9 : 7;
        Font font = new Font(fontName, Font.PLAIN, (int) (biggestDimension / fontRatio));
        int padding = (int) (biggestDimension * 0.04F);
        Graphics2D originalGraphics = firstImage.createGraphics();

        originalGraphics.setFont(font);
        ImageUtil.configureTextDrawQuality(originalGraphics);

        int maxWidth = width - (padding * 2);

        TextAlignment textAlignment;
        if (caption2) {
            textAlignment = TextAlignment.LEFT;
        } else {
            textAlignment = TextAlignment.CENTER;
        }

        Drawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(null, words)
                .build(textAlignment, maxWidth);

        GraphicsUtil.fontFitWidth(maxWidth, paragraph, originalGraphics);
        font = originalGraphics.getFont();

        int fillHeight = paragraph.getHeight(originalGraphics) + (padding * 2);

        int captionBoxX = 0;
        int captionBoxY = caption2 ? firstImage.getHeight() : 0;

        originalGraphics.dispose();

        firstImage.flush();
        firstImage = null;

        paragraph = null;

        boolean originalIsAnimated = imageMedia.isAnimated();

        ImageMedia withCaptionBox = ImageMediaBuilder.fromCollection(imageMedia.parallelStream().map(frame -> {
            BufferedImage originalImage = frame.getImage();
            BufferedImage withCaptionBoxImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight() + fillHeight, ImageUtil.getType(originalImage));
            Graphics2D graphics = withCaptionBoxImage.createGraphics();
            graphics.setColor(Color.WHITE);

            int imageX = 0;
            int imageY = caption2 ? 0 : fillHeight;

            graphics.fillRect(captionBoxX, captionBoxY, withCaptionBoxImage.getWidth(), fillHeight);
            graphics.drawImage(originalImage, imageX, imageY, null);
            graphics.dispose();
            int duration = frame.getDuration();
            frame.flush();
            return new AwtFrame(withCaptionBoxImage, duration);
        }).collect(ImmutableList.toImmutableList()));

        imageMedia = null;

        ContainerImageInfo containerImageInfo = new CustomContainerImageInfo(
                withCaptionBox,
                "captioned",
                captionBoxX,
                captionBoxY,
                width,
                fillHeight,
                padding,
                Position.TOP,
                textAlignment,
                font,
                Color.BLACK,
                null,
                null,
                true,
                null
        );

        ImageMedia result = drawTextOnImage(words, nonTextParts, containerImageInfo);

        String outputFormat;
        String outputExtension;
        if (result.isAnimated() && !originalIsAnimated) {
            outputFormat = "gif";
            outputExtension = "." + outputFormat;
        } else {
            outputFormat = fileFormat;
            outputExtension = com.google.common.io.Files.getFileExtension(media.getName());

            if (!outputExtension.isBlank()) {
                outputExtension = "." + outputExtension;
            }
        }

        File outputFile = FileUtil.getUniqueTempFile(containerImageInfo.getResultName() + outputExtension);
        ImageWriters.write(result, outputFile, outputFormat);
        return outputFile;
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public File demotivate(File media, String fileFormat, List<String> words, List<String> subText, Map<String, Drawable> nonTextParts) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFirstImage();

        int contentWidth = firstImage.getWidth();
        int contentHeight = firstImage.getHeight();
        int contentLargestDimension = Math.max(contentWidth, contentHeight);
        int contentImageType = ImageUtil.getType(firstImage);

        int demotivateImagePadding = (int) (contentLargestDimension * 0.2F);

        Graphics2D graphics = firstImage.createGraphics();

        Font font = new Font("Times", Font.PLAIN, contentLargestDimension / 6);
        Font subFont = font.deriveFont(font.getSize() / 3F);
        graphics.setFont(font);
        ImageUtil.configureTextDrawQuality(graphics);

        TextAlignment textAlignment = TextAlignment.CENTER;
        Drawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(null, words)
                .build(textAlignment, contentWidth);

        int paragraphHeight = paragraph.getHeight(graphics);

        Drawable subParagraph = subText.isEmpty() ? EMPTY : new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(null, subText)
                .build(textAlignment, contentWidth);
        graphics.setFont(subFont);
        int subParagraphHeight = subParagraph.getHeight(graphics);
        int mainSubSpacing = subParagraphHeight / 4;

        graphics.dispose();
        firstImage = null;
        imageMedia = null;

        int demotivateWidth = contentWidth + (demotivateImagePadding * 2);
        int demotivateHeight = contentHeight + (demotivateImagePadding * 2) + paragraphHeight + mainSubSpacing + subParagraphHeight;
        BufferedImage demotivateBackground = new BufferedImage(demotivateWidth, demotivateHeight, contentImageType);
        Graphics2D demotivateBackgroundGraphics = demotivateBackground.createGraphics();
        demotivateBackgroundGraphics.setColor(Color.BLACK);
        demotivateBackgroundGraphics.fillRect(0, 0, demotivateWidth, demotivateHeight);

        int lineDiameter = Math.max(Math.round(contentLargestDimension * 0.005F), 1);
        int lineImageSpacing = lineDiameter * 3;

        demotivateBackgroundGraphics.setColor(Color.WHITE);

        // Top border
        demotivateBackgroundGraphics.fillRect(demotivateImagePadding - (lineDiameter + lineImageSpacing), demotivateImagePadding - (lineDiameter + lineImageSpacing), contentWidth + (lineDiameter * 2) + (lineImageSpacing * 2), lineDiameter);
        // Bottom border
        demotivateBackgroundGraphics.fillRect(demotivateImagePadding - (lineDiameter + lineImageSpacing), demotivateImagePadding + contentHeight + lineImageSpacing, contentWidth + (lineDiameter * 2) + (lineImageSpacing * 2), lineDiameter);
        // Left border
        demotivateBackgroundGraphics.fillRect(demotivateImagePadding - (lineDiameter + lineImageSpacing), demotivateImagePadding - (lineDiameter + lineImageSpacing), lineDiameter, contentHeight + (lineDiameter * 2) + (lineImageSpacing * 2));
        // Right border
        demotivateBackgroundGraphics.fillRect(demotivateImagePadding + contentWidth + lineImageSpacing, demotivateImagePadding - (lineDiameter + lineImageSpacing), lineDiameter, contentHeight + (lineDiameter * 2) + (lineImageSpacing * 2));

        ImageMedia demotivateWithTextMedia;
        if (words.isEmpty()) {
            demotivateWithTextMedia = new StaticImage(demotivateBackground);
        } else {
            ImageMediaBuilder builder = new ImageMediaBuilder();
            int paragraphFrames = paragraph.getFrameCount();
            for (int i = 0; i < paragraphFrames; i++) {
                BufferedImage demotivateWithText = new BufferedImage(demotivateWidth, demotivateHeight, contentImageType);
                Graphics2D demotivateWithTextGraphics = demotivateWithText.createGraphics();
                demotivateWithTextGraphics.drawImage(demotivateBackground, 0, 0, null);

                demotivateWithTextGraphics.setColor(Color.WHITE);
                demotivateWithTextGraphics.setFont(font);
                ImageUtil.configureTextDrawQuality(demotivateWithTextGraphics);
                paragraph.draw(
                        demotivateWithTextGraphics,
                        demotivateImagePadding,
                        demotivateImagePadding + contentHeight + (demotivateImagePadding / 2)
                );
                demotivateWithTextGraphics.setFont(subFont);
                subParagraph.draw(
                        demotivateWithTextGraphics,
                        demotivateImagePadding,
                        demotivateImagePadding + contentHeight + (demotivateImagePadding / 2) + paragraphHeight + mainSubSpacing
                );

                builder.add(new AwtFrame(demotivateWithText, Frame.GIF_MINIMUM_FRAME_DURATION));
            }
            demotivateWithTextMedia = builder.build();
        }

        ContainerImageInfo containerImageInfo = new CustomContainerImageInfo(
                demotivateWithTextMedia,
                "demotivated",
                demotivateImagePadding,
                demotivateImagePadding,
                contentWidth,
                contentHeight,
                0,
                Position.TOP,
                textAlignment,
                null,
                null,
                null,
                null,
                true,
                null
        );

        return containerImageWithImage(media, fileFormat, containerImageInfo);
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public File impact(File media, String fileFormat, List<String> topWords, List<String> bottomWords, Map<String, Drawable> nonTextParts) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
        BufferedImage firstImage = imageMedia.getFirstImage();

        int width = firstImage.getWidth();
        int height = firstImage.getHeight() / 5;

        int smallestDimension = Math.min(width, height);
        int padding = (int) (smallestDimension * 0.04F);

        int topY = 0;
        int bottomY = firstImage.getHeight() - height;

        firstImage.flush();
        firstImage = null;

        boolean originalIsAnimated = imageMedia.isAnimated();

        ContainerImageInfo topWordsContainerImageInfo = new CustomContainerImageInfo(
                imageMedia,
                "impacted",
                0,
                topY,
                width,
                height,
                padding,
                Position.CENTRE,
                TextAlignment.CENTER,
                new Font("Impact", Font.BOLD, smallestDimension),
                Color.WHITE,
                word -> new OutlinedTextDrawable(word, Color.WHITE, Color.BLACK, 0.15F),
                null,
                true,
                null
        );

        ImageMedia result = drawTextOnImage(topWords, nonTextParts, topWordsContainerImageInfo);

        ContainerImageInfo bottomWordsContainerImageInfo = new CustomContainerImageInfo(
                result,
                topWordsContainerImageInfo.getResultName(),
                0,
                bottomY,
                width,
                height,
                padding,
                topWordsContainerImageInfo.getTextContentPosition(),
                topWordsContainerImageInfo.getTextContentAlignment(),
                topWordsContainerImageInfo.getFont(),
                topWordsContainerImageInfo.getTextColor(),
                topWordsContainerImageInfo.getCustomTextDrawableFactory().orElse(null),
                null,
                topWordsContainerImageInfo.isBackground(),
                topWordsContainerImageInfo.getFill().orElse(null)
        );

        result = drawTextOnImage(bottomWords, nonTextParts, bottomWordsContainerImageInfo);

        String outputFormat;
        String outputExtension;
        if (result.isAnimated() && !originalIsAnimated) {
            outputFormat = "gif";
            outputExtension = "." + outputFormat;
        } else {
            outputFormat = fileFormat;
            outputExtension = com.google.common.io.Files.getFileExtension(media.getName());

            if (!outputExtension.isBlank()) {
                outputExtension = "." + outputExtension;
            }
        }

        File outputFile = FileUtil.getUniqueTempFile(topWordsContainerImageInfo.getResultName() + outputExtension);
        ImageWriters.write(result, outputFile, outputFormat);
        return outputFile;
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public File containerImageWithImage(File media, String fileFormat, ContainerImageInfo containerImageInfo) throws IOException {
        ImageMedia containerImage = containerImageInfo.getImage();
        ImageMedia contentImage = ImageReaders.read(media, fileFormat, null);

        int imageType = ImageUtil.getType(contentImage.getFirstImage());
        boolean contentIsAnimated = contentImage.isAnimated();

        ImageMedia resizedContentImage = ImageMediaBuilder.fromCollection(contentImage.parallelStream().map(frame -> {
            int width = containerImageInfo.getImageContentWidth();
            int height = containerImageInfo.getImageContentHeight();
            BufferedImage resizedImage = ImageUtil.fit(frame.getImage(), width, height);
            int duration = frame.getDuration();
            frame.flush();
            return new AwtFrame(resizedImage, duration);
        }).collect(ImmutableList.toImmutableList()));

        contentImage = null;

        int resizedWidth = resizedContentImage.getFirstImage().getWidth();
        int resizedHeight = resizedContentImage.getFirstImage().getHeight();

        int imageX = containerImageInfo.getImageContentX() + ((containerImageInfo.getImageContentWidth() - resizedWidth) / 2);
        int imageY = containerImageInfo.getImageContentY() + ((containerImageInfo.getImageContentHeight() - resizedHeight) / 2);
        switch (containerImageInfo.getImageContentPosition()) {
            case TOP -> imageY = containerImageInfo.getImageContentY();
            case BOTTOM ->
                    imageY = containerImageInfo.getImageContentY() + (containerImageInfo.getImageContentHeight() - resizedHeight);
        }

        Color fill = containerImageInfo.getFill().orElse(null);
        if (fill == null && !resizedContentImage.getFirstImage().getColorModel().hasAlpha()) {
            fill = Color.WHITE;
        }

        ImageMedia result = ImageUtil.overlayImage(containerImage, resizedContentImage, containerImageInfo.isBackground(), imageX, imageY, containerImageInfo.getContentClip().orElse(null), imageType, fill, false);

        String outputFormat;
        String outputExtension;
        if (result.isAnimated() && !contentIsAnimated) {
            outputFormat = "gif";
            outputExtension = "." + outputFormat;
        } else {
            outputFormat = fileFormat;
            outputExtension = com.google.common.io.Files.getFileExtension(media.getName());

            if (!outputExtension.isBlank()) {
                outputExtension = "." + outputExtension;
            }
        }

        File output = FileUtil.getUniqueTempFile(containerImageInfo.getResultName() + "." + outputExtension);
        ImageWriters.write(result, output, outputFormat);

        return output;
    }

    @Override
    public File containerImageWithText(List<String> words, Map<String, Drawable> nonTextParts, ContainerImageInfo containerImageInfo) throws IOException {
        ImageMedia result = drawTextOnImage(words, nonTextParts, containerImageInfo);

        String outputFileName = containerImageInfo.getResultName() + ".";
        String outputFormat;
        if (result.isAnimated()) {
            outputFormat = "gif";
        } else {
            outputFormat = "png";
        }
        outputFileName += outputFormat;

        File outputFile = FileUtil.getUniqueTempFile(outputFileName);
        ImageWriters.write(result, outputFile, outputFormat);

        return outputFile;
    }

    @SuppressWarnings("UnusedAssignment")
    private static ImageMedia drawTextOnImage(List<String> words, Map<String, Drawable> nonTextParts, ContainerImageInfo containerImageInfo) throws IOException {
        ImageMedia imageMedia = containerImageInfo.getImage();

        if (words.isEmpty()) {
            return imageMedia;
        } else {
            ParagraphCompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                    .addWords(containerImageInfo.getCustomTextDrawableFactory().orElse(null), words)
                    .build(containerImageInfo.getTextContentAlignment(), containerImageInfo.getTextContentWidth());

            Graphics2D graphics = imageMedia.getFirstImage().createGraphics();

            Font font = containerImageInfo.getFont();
            graphics.setFont(font);
            ImageUtil.configureTextDrawQuality(graphics);

            GraphicsUtil.fontFitWidth(containerImageInfo.getTextContentWidth(), paragraph, graphics);
            int paragraphHeight = GraphicsUtil.fontFitHeight(containerImageInfo.getTextContentHeight(), paragraph, graphics);
            float fontSize = graphics.getFont().getSize2D();

            graphics.dispose();

            int containerCentreY = containerImageInfo.getTextContentY() + (containerImageInfo.getTextContentHeight() / 2);

            int paragraphX = containerImageInfo.getTextContentX();
            int paragraphY = containerCentreY - (paragraphHeight / 2);
            switch (containerImageInfo.getTextContentPosition()) {
                case TOP -> paragraphY = containerImageInfo.getTextContentY();
                case BOTTOM ->
                        paragraphY = containerImageInfo.getTextContentY() + (containerImageInfo.getTextContentHeight() - paragraphHeight);
            }

            ImageMediaBuilder builder = new ImageMediaBuilder();

            int paragraphFrameCount = paragraph.getFrameCount();
            if (paragraphFrameCount == 1) {
                for (Frame frame : imageMedia) {
                    BufferedImage image = frame.getImage();
                    BufferedImage imageWithText = drawOnImage(image, containerImageInfo, paragraph, paragraphX, paragraphY, fontSize);
                    int duration = frame.getDuration();
                    builder.add(new AwtFrame(imageWithText, duration));
                    frame.flush();
                }
            } else {
                List<BufferedImage> normalisedImages = imageMedia.toNormalisedImages();
                List<BufferedImage> extendedImages = CollectionUtil.extendLoop(normalisedImages, paragraphFrameCount);

                normalisedImages = null;

                BufferedImage previousImage = null;

                Iterator<BufferedImage> imageIterator = extendedImages.iterator();
                while (imageIterator.hasNext()) {
                    BufferedImage image = imageIterator.next();

                    if (image.equals(previousImage) && paragraph.sameAsPreviousFrame()) {
                        builder.increaseLastFrameDuration(Frame.GIF_MINIMUM_FRAME_DURATION);
                    } else {
                        BufferedImage imageWithText = drawOnImage(image, containerImageInfo, paragraph, paragraphX, paragraphY, fontSize);
                        builder.add(new AwtFrame(imageWithText, Frame.GIF_MINIMUM_FRAME_DURATION));
                        previousImage = imageWithText;
                    }

                    imageIterator.remove();
                }
            }

            return builder.build();
        }
    }

    @Override
    public File uncaption(File media, boolean coloredCaption, String fileFormat) throws IOException {
        return cropImage(
                media,
                fileFormat,
                image -> findNonCaptionAreaTopAndBottom(image, coloredCaption),
                "uncaptioned"
        );
    }

    private Rectangle findNonCaptionAreaTopAndBottom(BufferedImage image, boolean coloredCaption) {
        Rectangle nonCaptionArea = new Rectangle(0, 0, image.getWidth(), image.getHeight());

        Rectangle nonTopCaptionArea = coloredCaption ?
                findNonCaptionAreaColored(image, true) :
                findNonCaptionArea(image, true);
        nonCaptionArea = nonCaptionArea.intersection(nonTopCaptionArea);

        Rectangle nonBottomCaptionArea = coloredCaption ?
                findNonCaptionAreaColored(image, false) :
                findNonCaptionArea(image, false);
        nonCaptionArea = nonCaptionArea.intersection(nonBottomCaptionArea);

        return nonCaptionArea;
    }

    private static Rectangle findNonCaptionArea(BufferedImage image, boolean topCaption) {
        boolean continueLooking = true;
        int captionEnd = -1;
        int y = topCaption ? 0 : image.getHeight() - 1;
        while (topCaption ? y < image.getHeight() : y >= 0) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                if (!ImageUtil.isGreyScale(color)) {
                    continueLooking = false;
                    break;
                }
            }

            if (continueLooking) {
                captionEnd = y;

                if (topCaption) {
                    y++;
                } else {
                    y--;
                }
            } else {
                break;
            }
        }

        return createNonCaptionArea(image, topCaption, captionEnd);
    }

    private static Rectangle findNonCaptionAreaColored(BufferedImage image, boolean topCaption) {
        boolean continueLooking = true;
        int captionEnd = -1;
        int y = topCaption ? 0 : image.getHeight() - 1;
        int colorTolerance = 150;
        while (topCaption ? y < image.getHeight() : y >= 0) {
            boolean rowIsCompletelyWhite = true;
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                double colorDistance = ImageUtil.colorDistance(color, Color.WHITE);
                if (colorDistance > colorTolerance) {
                    rowIsCompletelyWhite = false;
                    if ((topCaption ? y == 0 : y == image.getHeight() - 1)
                            || x == 0
                            || x == image.getWidth() - 1
                    ) {
                        continueLooking = false;
                        break;
                    }
                } else if (rowIsCompletelyWhite && x == image.getWidth() - 1) {
                    captionEnd = y;
                }
            }

            if (continueLooking) {
                if (topCaption) {
                    y++;
                } else {
                    y--;
                }
            } else {
                break;
            }
        }

        return createNonCaptionArea(image, topCaption, captionEnd);
    }

    private static Rectangle createNonCaptionArea(BufferedImage image, boolean topCaption, int captionEnd) {
        if (captionEnd != -1) {
            int width = image.getWidth();
            int height = topCaption ? image.getHeight() - captionEnd - 1 : captionEnd;

            if (width > 0 && height > 0) {
                if (topCaption) {
                    return new Rectangle(0, captionEnd + 1, width, height);
                } else {
                    return new Rectangle(0, 0, width, height);
                }
            }
        }

        return new Rectangle(0, 0, image.getWidth(), image.getHeight());
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
    public File crop(File media, String fileFormat, float topRatio, float rightRatio, float bottomRatio, float leftRatio) throws IOException {
        if (topRatio == 0 && rightRatio == 0 && bottomRatio == 0 && leftRatio == 0) {
            return media;
        } else if (topRatio < 0 || rightRatio < 0 || bottomRatio < 0 || leftRatio < 0) {
            throw new InvalidArgumentException("Crop ratios must be positive");
        } else if (topRatio > 1 || rightRatio > 1 || bottomRatio > 1 || leftRatio > 1) {
            throw new InvalidArgumentException("Crop ratios must be less than 1");
        } else {
            ImageMedia imageMedia = ImageReaders.read(media, fileFormat, null);
            BufferedImage firstImage = imageMedia.getFirstImage();

            int width = firstImage.getWidth();
            int height = firstImage.getHeight();

            int x = Math.min((int) (width * leftRatio), width - 1);
            int y = Math.min((int) (height * topRatio), height - 1);
            int newWidth = Math.max((int) (width * (1 - leftRatio - rightRatio)), 1);
            int newHeight = Math.max((int) (height * (1 - topRatio - bottomRatio)), 1);

            return applyToEachFrame(media, fileFormat, image -> image.getSubimage(x, y, newWidth, newHeight), "cropped");
        }
    }

    @Override
    public File autoCrop(File media, String fileFormat, Color cropColor, int colorTolerance) throws IOException {
        return cropImage(
                media,
                fileFormat,
                image -> findAutoCropArea(image, cropColor, colorTolerance),
                "cropped"
        );
    }

    @SuppressWarnings("UnusedAssignment")
    private static File cropImage(File media, String imageFormat, Function<BufferedImage, Rectangle> cropKeepAreaFinder, @Nullable String operationName) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, imageFormat, null);
        BufferedImage firstImage = imageMedia.getFirstImage();

        Rectangle toKeep = null;
        int width = firstImage.getWidth();
        int height = firstImage.getHeight();

        for (Frame frame : imageMedia) {
            BufferedImage image = frame.getImage();
            Rectangle mayKeepArea = cropKeepAreaFinder.apply(image);
            if ((mayKeepArea.getX() != 0
                    || mayKeepArea.getY() != 0
                    || mayKeepArea.getWidth() != width
                    || mayKeepArea.getHeight() != height)
                    && mayKeepArea.getWidth() > 0
                    && mayKeepArea.getHeight() > 0
            ) {
                if (toKeep == null) {
                    toKeep = mayKeepArea;
                } else {
                    toKeep = toKeep.union(mayKeepArea);
                }
            }
        }

        firstImage.flush();
        firstImage = null;
        imageMedia = null;

        if (toKeep == null || (
                toKeep.getX() == 0
                        && toKeep.getY() == 0
                        && toKeep.getWidth() == width
                        && toKeep.getHeight() == height
        )) {
            return media;
        } else {
            final Rectangle finalToKeep = toKeep;
            return applyToEachFrame(
                    media,
                    imageFormat,
                    image -> image.getSubimage(
                            (int) finalToKeep.getX(),
                            (int) finalToKeep.getY(),
                            (int) finalToKeep.getWidth(),
                            (int) finalToKeep.getHeight()
                    ),
                    operationName
            );
        }
    }

    private static Rectangle findAutoCropArea(BufferedImage image, Color cropColor, int colorTolerance) {
        int width = image.getWidth();
        int height = image.getHeight();
        PixelsExtractor extractor = rectangle -> ImmutableImage.wrapAwt(image).pixels(
                (int) rectangle.getX(),
                (int) rectangle.getY(),
                (int) rectangle.getWidth(),
                (int) rectangle.getHeight()
        );

        int x1 = AutocropOps.scanright(cropColor, height, width, 0, extractor, colorTolerance);
        int x2 = AutocropOps.scanleft(cropColor, height, width - 1, extractor, colorTolerance);
        int y1 = AutocropOps.scandown(cropColor, height, width, 0, extractor, colorTolerance);
        int y2 = AutocropOps.scanup(cropColor, width, height - 1, extractor, colorTolerance);
        if (x1 == 0 && y1 == 0 && x2 == width - 1 && y2 == height - 1) {
            return new Rectangle(0, 0, width, height);
        } else {
            return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }
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
        BufferedImage firstImage = imageMedia.getFirstImage();

        int width = firstImage.getWidth();
        int height = firstImage.getHeight();

        BufferedImage speechBubble = ImageUtil.getImageResourceInRootPackage(speechBubblePath).getFirstImage();

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

            return applyToEachFrame(media, fileFormat, image -> ImageUtil.overlayImage(image, filledSpeechBubble, false, 0, -filledSpeechBubble.getHeight(), null, null, null, true), operationName);
        }
    }

    @Override
    public File speed(File media, String fileFormat, float speedMultiplier) throws IOException {
        return animatedOnlyOperation(media, fileFormat, imageMedia -> {
            if (speedMultiplier != 1 && speedMultiplier > 0) {
                ImageMediaBuilder builder = new ImageMediaBuilder();

                for (Frame frame : imageMedia) {
                    builder.add(new AwtFrame(frame.getImage(), Math.round(frame.getDuration() / speedMultiplier)));
                }

                ImageMedia modifiedDurations = builder.build();
                List<BufferedImage> keptFrames = modifiedDurations.toNormalisedImages();

                ImageMediaBuilder resultBuilder = new ImageMediaBuilder();

                for (BufferedImage image : keptFrames) {
                    resultBuilder.add(new AwtFrame(image, Frame.GIF_MINIMUM_FRAME_DURATION));
                }

                int duration = resultBuilder.getDuration();
                int expectedDuration = Math.round(imageMedia.getDuration() / speedMultiplier);
                if (expectedDuration > duration) {
                    resultBuilder.increaseLastFrameDuration(expectedDuration - duration);
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
        List<BufferedImage> keptImages = image.toNormalisedImages();

        image = null;

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
        if (com.google.common.io.Files.getFileExtension(media.getName()).equalsIgnoreCase("gif")) {
            throw new UnsupportedFileFormatException("This file is already a GIF file!");
        } else {
            File gifFile = FileUtil.getUniqueTempFile(FileUtil.changeExtension(media.getName(), "gif"));

            if (justRenameFile) {
                Files.move(media.toPath(), gifFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
            boolean reduceResolution = true;
            while (media.length() > DiscordUtil.getMaxUploadSize(guild)) {
                if (reduceResolution || !ANIMATED_IMAGE_FORMATS.contains(fileFormat)) {
                    media = resize(media, fileFormat, 0.75F, false, false);
                } else {
                    media = reduceFps(media, fileFormat, 2, false);
                }

                BufferedImage image = ImageReaders.read(media, fileFormat, null).getFirstImage();
                if (image.getWidth() <= DiscordUtil.DISCORD_MAX_DISPLAY_WIDTH || image.getHeight() <= DiscordUtil.DISCORD_MAX_DISPLAY_HEIGHT) {
                    reduceResolution = !reduceResolution;
                }
                image.flush();
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

    private static BufferedImage drawOnImage(BufferedImage image, ContainerImageInfo containerImageInfo, Drawable drawable, int textX, int textY, float fontSize) throws IOException {
        BufferedImage imageWithText = new BufferedImage(image.getWidth(), image.getHeight(), ImageUtil.getType(image));
        Graphics2D graphics = imageWithText.createGraphics();

        Optional<Shape> contentClipOptional = containerImageInfo.getContentClip();
        containerImageInfo.getFill().ifPresent(color -> {
            graphics.setColor(color);
            contentClipOptional.ifPresentOrElse(
                    graphics::fill,
                    () -> graphics.fillRect(0, 0, imageWithText.getWidth(), imageWithText.getHeight())
            );
        });

        if (containerImageInfo.isBackground()) {
            graphics.drawImage(image, 0, 0, null);
        }

        Font font = containerImageInfo.getFont().deriveFont(fontSize);
        graphics.setFont(font);
        ImageUtil.configureTextDrawQuality(graphics);
        graphics.setColor(containerImageInfo.getTextColor());

        contentClipOptional.ifPresent(graphics::setClip);

        drawable.draw(graphics, textX, textY);

        if (contentClipOptional.isPresent()) {
            graphics.setClip(null);
        }

        if (!containerImageInfo.isBackground()) {
            graphics.drawImage(image, 0, 0, null);
        }

        graphics.dispose();

        return imageWithText;
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
    private static File applyToEachFrame(File media, String imageFormat, Function<BufferedImage, BufferedImage> operation, @Nullable String operationName) throws IOException {
        ImageMedia imageMedia = ImageReaders.read(media, imageFormat, null);

        ImageMedia outputImage = ImageMediaBuilder.fromCollection(imageMedia.parallelStream().map(frame -> {
            BufferedImage unmodifiedImage = frame.getImage();
            BufferedImage modifiedImage = operation.apply(unmodifiedImage);
            int duration = frame.getDuration();
            frame.flush();
            return new AwtFrame(modifiedImage, duration);
        }).collect(ImmutableList.toImmutableList()));

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

    private static class EmptyDrawable implements Drawable {

        @Override
        public void draw(Graphics2D graphics, int x, int y) {

        }

        @Override
        public int getWidth(Graphics2D graphicsContext) {
            return 0;
        }

        @Override
        public int getHeight(Graphics2D graphicsContext) {
            return 0;
        }

        @Override
        public Drawable resizeToWidth(int width) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Drawable resizeToHeight(int height) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getFrameCount() {
            return 0;
        }

        @Override
        public boolean sameAsPreviousFrame() {
            return false;
        }
    }
}
