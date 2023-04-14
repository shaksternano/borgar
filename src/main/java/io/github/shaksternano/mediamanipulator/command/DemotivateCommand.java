package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.ImageProcessor;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.MediaUtil;
import io.github.shaksternano.mediamanipulator.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DemotivateCommand extends FileCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public DemotivateCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected File modifyFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        var nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        var processor = new DemotivateProcessor(arguments, extraArguments.get("sub"), nonTextParts);
        return MediaUtil.processMedia(file, fileFormat, "demotivated", processor);
    }

    @Override
    public Set<String> getAdditionalParameterNames() {
        return ImmutableSet.of(
            "sub"
        );
    }

    private record DemotivateProcessor(
        List<String> words,
        List<String> subText,
        Map<String, Drawable> nonTextParts
    ) implements ImageProcessor<DemotivateData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, DemotivateData constantData) throws IOException {
            BufferedImage image = frame.content();
            BufferedImage result = new BufferedImage(constantData.width(), constantData.height(), ImageUtil.getType(image));
            Graphics2D graphics = result.createGraphics();

            // Draw background
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, 0, constantData.width(), constantData.height());

            // Draw image
            graphics.drawImage(image, constantData.imagePadding(), constantData.imagePadding(), null);

            // Draw border
            graphics.setColor(Color.WHITE);
            fillRect(graphics, constantData.topBorder());
            fillRect(graphics, constantData.bottomBorder());
            fillRect(graphics, constantData.leftBorder());
            fillRect(graphics, constantData.rightBorder());

            // Draw text
            graphics.setFont(constantData.font());
            ImageUtil.configureTextDrawQuality(graphics);
            draw(graphics, constantData.paragraph, constantData.paragraphPosition(), frame.timestamp());
            graphics.setFont(constantData.subFont());
            draw(graphics, constantData.subParagraph, constantData.subParagraphPosition(), frame.timestamp());

            graphics.dispose();
            return result;
        }

        private static void fillRect(Graphics2D graphics, Rectangle rectangle) {
            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

        private static void draw(Graphics2D graphics, Drawable drawable, Position position, long timestamp) throws IOException {
            drawable.draw(graphics, position.x(), position.y(), timestamp);
        }

        @Override
        public DemotivateData constantData(BufferedImage image) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int contentAverageDimension = (imageWidth + imageHeight) / 2;

            int demotivateImagePadding = (int) (contentAverageDimension * 0.2F);

            Graphics2D graphics = image.createGraphics();

            Font font = new Font("Times", Font.PLAIN, contentAverageDimension / 6);
            Font subFont = font.deriveFont(font.getSize() / 3F);
            graphics.setFont(font);
            ImageUtil.configureTextDrawQuality(graphics);

            TextAlignment textAlignment = TextAlignment.CENTER;
            Drawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(null, words)
                .build(textAlignment, imageWidth);

            int paragraphHeight = paragraph.getHeight(graphics);

            Drawable subParagraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(null, subText)
                .build(textAlignment, imageWidth);
            graphics.setFont(subFont);
            int subParagraphHeight = subParagraph.getHeight(graphics);
            int mainToSubParagraphSpacing = subParagraphHeight / 4;

            graphics.dispose();

            int demotivateWidth = imageWidth + (demotivateImagePadding * 2);
            int demotivateHeight = imageHeight + (demotivateImagePadding * 2) + paragraphHeight + mainToSubParagraphSpacing + subParagraphHeight;

            int lineDiameter = Math.max(Math.round(contentAverageDimension * 0.005F), 1);
            int lineImageSpacing = lineDiameter * 3;

            Position paragraphPosition = new Position(
                demotivateImagePadding,
                demotivateImagePadding + imageHeight + (demotivateImagePadding / 2)
            );
            Position subParagraphPosition = new Position(
                demotivateImagePadding,
                demotivateImagePadding + imageHeight + (demotivateImagePadding / 2) + paragraphHeight + mainToSubParagraphSpacing
            );

            Rectangle topBorder = new Rectangle(
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                imageWidth + (lineDiameter * 2) + (lineImageSpacing * 2),
                lineDiameter
            );
            Rectangle bottomBorder = new Rectangle(
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                demotivateImagePadding + imageHeight + lineImageSpacing,
                imageWidth + (lineDiameter * 2) + (lineImageSpacing * 2),
                lineDiameter
            );
            Rectangle leftBorder = new Rectangle(
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                lineDiameter,
                imageHeight + (lineDiameter * 2) + (lineImageSpacing * 2)
            );
            Rectangle rightBorder = new Rectangle(
                demotivateImagePadding + imageWidth + lineImageSpacing,
                demotivateImagePadding - (lineDiameter + lineImageSpacing),
                lineDiameter,
                imageHeight + (lineDiameter * 2) + (lineImageSpacing * 2)
            );

            return new DemotivateData(
                demotivateWidth,
                demotivateHeight,
                demotivateImagePadding,
                font,
                paragraph,
                paragraphPosition,
                subFont,
                subParagraph,
                subParagraphPosition,
                topBorder,
                bottomBorder,
                leftBorder,
                rightBorder
            );
        }

        @Override
        public void close() throws IOException {
            IOException exception = null;
            for (Drawable drawable : nonTextParts.values()) {
                try {
                    drawable.close();
                } catch (IOException e) {
                    exception = e;
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
    }

    private record DemotivateData(
        int width,
        int height,
        int imagePadding,
        Font font,
        Drawable paragraph,
        Position paragraphPosition,
        Font subFont,
        Drawable subParagraph,
        Position subParagraphPosition,
        Rectangle topBorder,
        Rectangle bottomBorder,
        Rectangle leftBorder,
        Rectangle rightBorder
    ) {
    }

    private record Position(int x, int y) {
    }
}
