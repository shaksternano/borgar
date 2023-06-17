package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class FlipCommand extends FileCommand {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public FlipCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        var resultName = "flipped";
        var vertical = extraArguments.containsKey("v");
        return new NamedFile(
            MediaUtil.processMedia(
                file,
                fileFormat,
                resultName,
                image -> flip(image, vertical),
                maxFileSize
            ),
            resultName,
            fileFormat
        );
    }

    private static BufferedImage flip(BufferedImage image, boolean vertical) {
        var immutableImage = ImmutableImage.wrapAwt(image);
        ImmutableImage flippedImage;
        if (vertical) {
            flippedImage = immutableImage.flipY();
        } else {
            flippedImage = immutableImage.flipX();
        }
        return flippedImage.awt();
    }

    @Override
    public Set<String> getAdditionalParameterNames() {
        return Set.of(
            "v"
        );
    }
}
