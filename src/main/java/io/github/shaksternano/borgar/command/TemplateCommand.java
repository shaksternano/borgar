package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.graphics.OverlayData;
import io.github.shaksternano.borgar.media.graphics.TextDrawData;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.io.MediaReaders;
import io.github.shaksternano.borgar.media.io.imageprocessor.DualImageProcessor;
import io.github.shaksternano.borgar.media.io.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.borgar.media.template.Template;
import io.github.shaksternano.borgar.util.MessageUtil;
import io.github.shaksternano.borgar.util.MiscUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TemplateCommand extends OptionalFileInputFileCommand {

    private final Template template;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public TemplateCommand(String name, String description, Template template) {
        super(name, description);
        this.template = template;
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var contentImageReader = MediaReaders.createImageReader(file, fileFormat);
        var contentAudioReader = MediaReaders.createAudioReader(file, fileFormat);
        var templateImageReader = template.getImageReader();
        var processor = new ImageContentProcessor(template);
        String outputFormat;
        if (contentImageReader.isAnimated() || (!contentImageReader.isAnimated() && !templateImageReader.isAnimated())) {
            outputFormat = contentImageReader.format();
        } else {
            outputFormat = templateImageReader.format();
        }
        return new NamedFile(
            MediaUtil.processMedia(
                contentImageReader,
                contentAudioReader,
                templateImageReader,
                outputFormat,
                template.getResultName(),
                processor,
                maxFileSize
            ),
            template.getResultName(),
            outputFormat
        );
    }

    @Override
    protected NamedFile createFile(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var imageReader = template.getImageReader();
        var audioReader = template.getAudioReader();
        var nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        var processor = new TextContentProcessor(arguments, nonTextParts, template);
        var outputFormat = imageReader.format();
        var resultName = template.getResultName();
        return new NamedFile(
            MediaUtil.processMedia(
                imageReader,
                audioReader,
                outputFormat,
                resultName,
                processor,
                maxFileSize
            ),
            template.getResultName(),
            outputFormat
        );
    }

    private record ImageContentProcessor(
        Template template
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
            var width = template.getImageContentWidth();
            var height = template.getImageContentHeight();

            var resizedContentImage = ImageUtil.fit(contentImage, width, height);

            int resizedWidth = resizedContentImage.getWidth();
            int resizedHeight = resizedContentImage.getHeight();

            int contentImageX = template.getImageContentX() + ((template.getImageContentWidth() - resizedWidth) / 2);
            int contentImageY = switch (template.getImageContentPosition()) {
                case TOP -> template.getImageContentY();
                case BOTTOM -> template.getImageContentY() + (template.getImageContentHeight() - resizedHeight);
                default ->
                    template.getImageContentY() + ((template.getImageContentHeight() - resizedHeight) / 2);
            };

            var fill = template.getFill().orElseGet(() -> resizedContentImage.getColorModel().hasAlpha()
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
                template.isBackground(),
                template.getContentClip().orElse(null),
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
        Template template
    ) implements SingleImageProcessor<TextDrawData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, @Nullable TextDrawData constantData) throws IOException {
            if (constantData == null) {
                return frame.content();
            }
            return ImageUtil.drawText(
                frame.content(),
                constantData,
                frame.timestamp(),
                template
            );
        }

        @Nullable
        @Override
        public TextDrawData constantData(BufferedImage image) {
            return ImageUtil.getTextDrawData(image, words, nonTextParts, template).orElse(null);
        }

        @Override
        public void close() throws IOException {
            MiscUtil.closeAll(nonTextParts.values());
        }
    }
}
