package io.github.shaksternano.mediamanipulator.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.mediamanipulator.command.util.CommandParser;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.JavaCVUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A command that stretches media.
 */
public class StretchCommand extends FileCommand {

    /**
     * The default stretch width multiplier.
     */
    public static final float DEFAULT_WIDTH_MULTIPLIER = 2;

    /**
     * The default stretch height multiplier.
     */
    public static final float DEFAULT_HEIGHT_MULTIPLIER = 1;

    private final boolean RAW;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public StretchCommand(String name, String description, boolean raw) {
        super(name, description);
        RAW = raw;
    }

    @Override
    public File editFile(File file, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = CommandParser.parseFloatArgument(
                arguments,
                0,
                DEFAULT_WIDTH_MULTIPLIER,
                null,
                event.getChannel(),
                (argument, defaultValue) -> "Width multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );
        float heightMultiplier = CommandParser.parseFloatArgument(
                arguments,
                1,
                DEFAULT_HEIGHT_MULTIPLIER,
                null,
                event.getChannel(),
                (argument, defaultValue) -> "Height multiplier \"" + argument + "\" is not a number. Using default value of " + defaultValue + "."
        );

        File output = FileUtil.getUniqueTempFile("stretched.mov");
        FFmpegFrameRecorder recorder = null;
        try (
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
                Java2DFrameConverter converter = new Java2DFrameConverter()
        ) {
            grabber.start();
            double fps = grabber.getFrameRate();
            Frame imageFrame;
            while ((imageFrame = grabber.grabImage()) != null) {
                BufferedImage image = converter.convert(imageFrame);
                BufferedImage stretched = ImageUtil.stretch(
                        image,
                        (int) (image.getWidth() * widthMultiplier),
                        (int) (image.getHeight() * heightMultiplier),
                        RAW
                );

                if (recorder == null) {
                    recorder = JavaCVUtil.createFFmpegRecorder(
                            output,
                            "mp4",
                            stretched.getWidth(),
                            stretched.getHeight(),
                            grabber.getAudioChannels(),
                            fps
                    );
                    recorder.start();
                }
                recorder.record(converter.getFrame(stretched));
            }

            grabber.setTimestamp(0);
            Frame audioFrame;
            while ((audioFrame = grabber.grabSamples()) != null) {
                if (recorder == null) {
                    recorder = JavaCVUtil.createFFmpegRecorder(
                            output,
                            "mp4",
                            grabber.getImageWidth(),
                            grabber.getImageHeight(),
                            grabber.getAudioChannels(),
                            fps
                    );
                    recorder.start();
                }
                recorder.record(audioFrame);
            }
        } finally {
            if (recorder != null) {
                recorder.close();
            }
        }
        return output;
    }
}
