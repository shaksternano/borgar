package io.github.shaksternano.mediamanipulator.command;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains registered {@link Command}s.
 */
@SuppressWarnings("unused")
public class Commands {

    private static final List<Command> commandsToRegister = new ArrayList<>();

    /**
     * The caption {@link Command}.
     */
    public static final Command CAPTION = addCommandToRegister(new CaptionCommand(
            "caption",
            "Captions a media file."
    ));

    /**
     * The stretch {@link Command}.
     */
    public static final Command STRETCH = addCommandToRegister(new StretchCommand(
            "stretch",
            "Stretches media with extra processing to smoothen the resulting image. Optional arguments: [width stretch multiplier, default value is " + StretchCommand.DEFAULT_WIDTH_MULTIPLIER + "], [height stretch multiplier, default value is " + StretchCommand.DEFAULT_HEIGHT_MULTIPLIER + "]",
            false
    ));

    public static final Command STRETCH_RAW = addCommandToRegister(new StretchCommand(
            "stretchraw",
            "Stretches media without extra processing. Optional arguments: [width stretch multiplier, default value is " + StretchCommand.DEFAULT_WIDTH_MULTIPLIER + "], [height stretch multiplier, default value is " + StretchCommand.DEFAULT_HEIGHT_MULTIPLIER + "]",
            true
    ));

    public static final Command RESIZE = addCommandToRegister(new ResizeCommand(
            "resize",
            "Resizes media with extra processing to smoothen the resulting image. Equivalent to " + Command.COMMAND_PREFIX + STRETCH.getName() + " x x. Required arguments: [resize multiplier]",
            false
    ));

    public static final Command RESIZE_RAW = addCommandToRegister(new ResizeCommand(
            "resizeraw",
            "Resizes media without extra processing. Equivalent to " + Command.COMMAND_PREFIX + STRETCH_RAW.getName() + " x x. Required arguments: [resize multiplier]",
            true
    ));

    public static final Command SPEED = addCommandToRegister(new SpeedCommand(
            "speed",
            "Speeds up or slows down media. Optional arguments: [speed multiplier, default value is " + SpeedCommand.DEFAULT_SPEED_MULTIPLIER + "]"
    ));

    public static final Command PIXELATE = addCommandToRegister(new PixelateCommand(
            "pixel",
            "Pixelates media. Equivalent to " + Command.COMMAND_PREFIX + RESIZE_RAW.getName() + " 1/x followed by " + Command.COMMAND_PREFIX + RESIZE_RAW.getName() + " x Optional arguments: [pixelation multiplier, default value is " + PixelateCommand.DEFAULT_PIXELATION_MULTIPLIER + "]"
    ));

    public static final Command REDUCE_FPS = addCommandToRegister(new ReduceFpsCommand(
            "redfps",
            "Reduces the FPS of a media file. Optional arguments: [fps reduction multiplier, default value is " + ReduceFpsCommand.DEFAULT_FPS_REDUCTION_MULTIPLIER + "]"
    ));

    /**
     * The speech bubble {@link Command}.
     */
    public static final Command SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
            "sb",
            "Overlays a speech bubble over media.",
            false
    ));

    public static final Command INVERTED_SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
            "sbi",
            "Cuts out a speech bubble from media (Inverted speech bubble).",
            true
    ));

    /**
     * The to-gif {@link Command}.
     */
    public static final Command TO_GIF = addCommandToRegister(new ToGifCommand(
            "gif",
            "Turns media into a GIF.",
            false
    ));

    public static final Command TO_GIF_FALLBACK = addCommandToRegister(new ToGifCommand(
            "gif2",
            "Turns media into a GIF by just renaming the file extension to \".gif\". Use this when there are problems with the " + Command.COMMAND_PREFIX + TO_GIF.getName() + " command.",
            true
    ));

    public static final Command AVATAR = addCommandToRegister(new AvatarCommand(
            "avatar",
            "Gets the avatar of a user. Optional arguments: [User mention]"
    ));

    public static final Command EMOJI_IMAGE = addCommandToRegister(new EmojiImageCommand(
            "emoji",
            "Gets the image of a custom emoji."
    ));

    public static final Command STICKER_IMAGE = addCommandToRegister(new StickerImageCommand(
            "sticker",
            "Gets the image of a custom sticker."
    ));

    public static final Command MEMORY_USAGE = addCommandToRegister(new MemoryUsageCommand(
            "memory",
            "Get the memory usage of the bot."
    ));

    /**
     * The shut-down {@link Command}.
     */
    public static final Command SHUT_DOWN = addCommandToRegister(new ShutDownCommand(
            "shutdown",
            "Shuts down the bot. Only the owner of the bot can use this command."
    ));

    /**
     * The help {@link Command}.
     */
    public static final Command HELP = addCommandToRegister(new HelpCommand(
            "help",
            "Lists all commands."
    ));

    private static <T extends Command> T addCommandToRegister(T command) {
        commandsToRegister.add(command);
        return command;
    }

    /**
     * Registers all the {@link Command}s.
     */
    public static void registerCommands() {
        CommandRegistry.register(commandsToRegister.toArray(new Command[0]));
    }
}
