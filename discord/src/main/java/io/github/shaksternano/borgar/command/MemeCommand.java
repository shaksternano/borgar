package io.github.shaksternano.borgar.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.exception.MissingArgumentException;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.graphics.GraphicsUtil;
import io.github.shaksternano.borgar.media.graphics.TextAlignment;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.graphics.drawable.OutlinedTextDrawable;
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
import java.util.Set;

public class MemeCommand extends FileCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public MemeCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var nonTextParts = MessageUtil.getEmojiImages(event.getMessage());
        var bottomWords = extraArguments.get("bottom");
        if (arguments.isEmpty() && bottomWords.isEmpty()) {
            throw new MissingArgumentException("Please specify text!");
        }
        var processor = new ImpactProcessor(arguments, bottomWords, nonTextParts);
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                "impacted",
                processor,
                maxFileSize
            ),
            "impacted",
            fileFormat
        );
    }

    @Override
    public Set<String> parameterNames() {
        return ImmutableSet.of(
            "bottom"
        );
    }

    private record ImpactProcessor(
        List<String> topWords,
        List<String> bottomWords,
        Map<String, Drawable> nonTextParts
    ) implements SingleImageProcessor<ImpactData> {

        @Override
        public BufferedImage transformImage(ImageFrame frame, ImpactData constantData) throws IOException {
            var result = frame.content();
            var graphics = result.createGraphics();
            ImageUtil.configureTextDrawQuality(graphics);

            graphics.setFont(constantData.topFont());
            constantData.topParagraph().draw(
                graphics,
                constantData.topParagraphX(),
                constantData.topParagraphY(),
                frame.timestamp()
            );

            graphics.setFont(constantData.bottomFont());
            constantData.bottomParagraph().draw(
                graphics,
                constantData.bottomParagraphX(),
                constantData.bottomParagraphY(),
                frame.timestamp()
            );

            graphics.dispose();
            return result;
        }

        @Override
        public ImpactData constantData(BufferedImage image) {
            var imageWidth = image.getWidth();
            var imageHeight = image.getHeight();

            var smallestDimension = Math.min(imageWidth, imageHeight);
            var padding = smallestDimension * 0.04F;

            var textWidth = (int) (imageWidth - (padding * 2));
            var textHeight = (imageHeight / 5F) - (padding * 2);

            var bottomParagraphY = (int) (image.getHeight() - textHeight - padding);

            var topParagraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(ImpactProcessor::createText, topWords)
                .build(TextAlignment.CENTRE, textWidth);
            var bottomParagraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(ImpactProcessor::createText, bottomWords)
                .build(TextAlignment.CENTRE, textWidth);

            var font = new Font("Impact", Font.BOLD, smallestDimension);

            var graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
            ImageUtil.configureTextDrawQuality(graphics);
            graphics.setFont(font);
            GraphicsUtil.fontFitHeight((int) textHeight, topParagraph, graphics);
            var topFont = graphics.getFont();
            graphics.setFont(font);
            GraphicsUtil.fontFitHeight((int) textHeight, bottomParagraph, graphics);
            var bottomFont = graphics.getFont();
            graphics.dispose();

            return new ImpactData(
                topParagraph,
                topFont,
                (int) padding,
                (int) padding,
                bottomParagraph,
                bottomFont,
                (int) padding,
                bottomParagraphY
            );
        }

        @Override
        public void close() throws IOException {
            MiscUtil.closeAll(nonTextParts.values());
        }

        private static Drawable createText(String word) {
            return new OutlinedTextDrawable(word, Color.WHITE, Color.BLACK, 0.15F);
        }
    }

    private record ImpactData(
        Drawable topParagraph,
        Font topFont,
        int topParagraphX,
        int topParagraphY,
        Drawable bottomParagraph,
        Font bottomFont,
        int bottomParagraphX,
        int bottomParagraphY
    ) {
    }
}
