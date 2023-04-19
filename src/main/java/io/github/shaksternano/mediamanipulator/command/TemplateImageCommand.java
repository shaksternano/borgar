package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.*;
import io.github.shaksternano.mediamanipulator.media.graphics.OverlayData;
import io.github.shaksternano.mediamanipulator.media.graphics.TextDrawData;
import io.github.shaksternano.mediamanipulator.media.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.media.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.media.template.TemplateImageInfo;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TemplateImageCommand extends OptionalFileInputFileCommand {

    private final TemplateImageInfo templateInfo;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public TemplateImageCommand(String name, String description, TemplateImageInfo templateInfo) {
        super(name, description);
        this.templateInfo = templateInfo;
    }

    @Override
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        var contentImageReader = MediaReaders.createImageReader(file, fileFormat);
        var contentAudioReader = MediaReaders.createAudioReader(file, fileFormat);
        var templateImageReader = templateInfo.getImageReader();
        var processor = new ImageContentProcessor(templateInfo);
        String outputFormat;
        if (contentImageReader.animated() || (!contentImageReader.animated() && !templateImageReader.animated())) {
            outputFormat = contentImageReader.format();
        } else {
            outputFormat = templateImageReader.format();
        }
        return MediaUtil.processMedia(
            contentImageReader,
            contentAudioReader,
            templateImageReader,
            outputFormat,
            templateInfo.getResultName(),
            processor
        );
    }

    @Override
    protected File createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        var imageReader = templateInfo.getImageReader();
        var audioReader = templateInfo.getAudioReader();
        var nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        var processor = new TextContentProcessor(arguments, nonTextParts, templateInfo);
        var outputFormat = imageReader.format();
        var outputName = templateInfo.getResultName() + "." + outputFormat;
        var output = FileUtil.getUniqueTempFile(outputName);
        return MediaUtil.processMedia(
            imageReader,
            audioReader,
            output,
            outputFormat,
            processor
        );
    }

    private record ImageContentProcessor(
        TemplateImageInfo templateInfo
    ) implements DualImageProcessor<ImageContentData> {

        @Override
        public BufferedImage transformImage(ImageFrame contentFrame, ImageFrame templateFrame, ImageContentData constantData) {
            var resizedContentImage = ImageUtil.fit(
                contentFrame.content(),
                constantData.contentImageTargetWidth(),
                constantData.contentImageTargetHeight()
            );
            return ImageUtil.overlayImage(
                templateFrame.content(),
                resizedContentImage,
                constantData.overlayData(),
                constantData.contentIsBackground(),
                constantData.contentClip(),
                constantData.fill()
            );
        }

        @Override
        public ImageContentData constantData(BufferedImage contentImage, BufferedImage templateImage) throws IOException {
            var width = templateInfo.getImageContentWidth();
            var height = templateInfo.getImageContentHeight();

            var resizedContentImage = ImageUtil.fit(contentImage, width, height);

            int resizedWidth = resizedContentImage.getWidth();
            int resizedHeight = resizedContentImage.getHeight();

            int contentImageX = templateInfo.getImageContentX() + ((templateInfo.getImageContentWidth() - resizedWidth) / 2);
            int contentImageY = switch (templateInfo.getImageContentPosition()) {
                case TOP -> templateInfo.getImageContentY();
                case BOTTOM -> templateInfo.getImageContentY() + (templateInfo.getImageContentHeight() - resizedHeight);
                default ->
                    templateInfo.getImageContentY() + ((templateInfo.getImageContentHeight() - resizedHeight) / 2);
            };

            var fill = templateInfo.getFill().orElseGet(() -> resizedContentImage.getColorModel().hasAlpha()
                ? null
                : Color.WHITE
            );

            var overlayData = ImageUtil.getOverlayData(
                templateImage,
                contentImage,
                contentImageX,
                contentImageY,
                false,
                ImageUtil.getType(contentImage)
            );

            return new ImageContentData(
                overlayData,
                contentImageX,
                contentImageY,
                width,
                height,
                templateInfo.isBackground(),
                templateInfo.getContentClip().orElse(null),
                fill
            );
        }
    }

    private record ImageContentData(
        OverlayData overlayData,
        int contentImageX,
        int contentImageY,
        int contentImageTargetWidth,
        int contentImageTargetHeight,
        boolean contentIsBackground,
        @Nullable Shape contentClip,
        @Nullable Color fill
    ) {
    }

    private record TextContentProcessor(
        List<String> words,
        Map<String, Drawable> nonTextParts,
        TemplateImageInfo templateInfo

    ) implements ImageProcessor<TextDrawData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, @Nullable TextDrawData constantData) throws IOException {
            if (constantData == null) {
                return frame.content();
            }
            return ImageUtil.drawText(
                frame.content(),
                constantData,
                frame.timestamp(),
                templateInfo
            );
        }

        @Nullable
        @Override
        public TextDrawData constantData(BufferedImage image) {
            return ImageUtil.getTextDrawData(image, words, nonTextParts, templateInfo).orElse(null);
        }

        @Override
        public void close() throws IOException {
            MiscUtil.closeAll(nonTextParts.values());
        }
    }
}
