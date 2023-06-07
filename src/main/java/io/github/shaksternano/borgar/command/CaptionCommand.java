package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.graphics.GraphicsUtil;
import io.github.shaksternano.borgar.media.graphics.TextAlignment;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.borgar.media.io.imageprocessor.SingleImageProcessor;
import io.github.shaksternano.borgar.util.MessageUtil;
import io.github.shaksternano.borgar.util.MiscUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A command that adds a captions media.
 */
public class CaptionCommand extends FileCommand {

    private final boolean CAPTION_2;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     * @param caption2    Whether to put text on the bottom of the image instead of the top.
     */
    public CaptionCommand(String name, String description, boolean caption2) {
        super(name, description);
        CAPTION_2 = caption2;
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        var processor = new CaptionProcessor(CAPTION_2, arguments, nonTextParts);
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                "captioned",
                processor,
                maxFileSize
            ),
            "captioned",
            fileFormat
        );
    }

    private record CaptionProcessor(
        boolean caption2,
        List<String> words,
        Map<String, Drawable> nonTextParts
    ) implements SingleImageProcessor<CaptionData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, CaptionData constantData) throws IOException {
            BufferedImage image = frame.content();
            BufferedImage captionedImage = new BufferedImage(image.getWidth(), image.getHeight() + constantData.fillHeight(), ImageUtil.getType(image));
            Graphics2D graphics = captionedImage.createGraphics();
            ImageUtil.configureTextDrawQuality(graphics);

            int imageY;
            int captionY;
            if (caption2) {
                imageY = 0;
                captionY = image.getHeight();
            } else {
                imageY = constantData.fillHeight();
                captionY = 0;
            }

            graphics.drawImage(image, 0, imageY, null);

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, captionY, captionedImage.getWidth(), constantData.fillHeight());

            graphics.setFont(constantData.font());
            graphics.setColor(Color.BLACK);
            constantData.paragraph().draw(
                graphics,
                constantData.padding(),
                captionY + constantData.padding(),
                frame.timestamp()
            );

            graphics.dispose();

            return captionedImage;
        }

        @Override
        public CaptionData constantData(BufferedImage image) {
            int width = image.getWidth();
            int height = image.getHeight();

            int averageDimension = (width + height) / 2;

            String fontName = caption2 ? "Helvetica Neue" : "Futura-CondensedExtraBold";
            float fontRatio = caption2 ? 9 : 7;
            Font font = new Font(fontName, Font.PLAIN, (int) (averageDimension / fontRatio));
            int padding = (int) (averageDimension * 0.04F);
            Graphics2D graphics = image.createGraphics();

            graphics.setFont(font);
            ImageUtil.configureTextDrawQuality(graphics);

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

            GraphicsUtil.fontFitWidth(maxWidth, paragraph, graphics);
            font = graphics.getFont();
            int fillHeight = paragraph.getHeight(graphics) + (padding * 2);
            graphics.dispose();
            return new CaptionData(font, fillHeight, padding, paragraph);
        }

        @Override
        public void close() throws IOException {
            MiscUtil.closeAll(nonTextParts.values());
        }
    }

    private record CaptionData(
        Font font,
        int fillHeight,
        int padding,
        Drawable paragraph
    ) {
    }
}
