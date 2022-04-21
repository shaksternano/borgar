package io.github.shaksternano.mediamanipulator.command;

/**
 * Contains registered {@link Command}s.
 */
public class Commands {

    /**
     * The caption {@link Command}.
     */
    public static final Command CAPTION = new CaptionCommand(
            "caption",
            "Captions a media file."
    );

    /**
     * The stretch {@link Command}.
     */
    public static final Command STRETCH = new StretchCommand(
            "stretch",
            "Stretches media. Optional parameters: [width stretch multiplier, default value is " + StretchCommand.DEFAULT_WIDTH_MULTIPLIER + "], [height stretch multiplier, default value is " + StretchCommand.DEFAULT_HEIGHT_MULTIPLIER + "]"
    );

    public static final Command RESIZE = new ResizeCommand(
            "resize",
            "Resizes media. Equivalent to " + CommandParser.COMMAND_PREFIX + STRETCH.getName() + " x x. Required parameters: [resize multiplier]"
    );

    public static final Command PIXELATE = new PixelateCommand(
            "pixelate",
            "Pixelates media. Equivalent to " + CommandParser.COMMAND_PREFIX + STRETCH.getName() + " 1/x 1/x followed by " + CommandParser.COMMAND_PREFIX + STRETCH.getName() + " x x Optional parameters: [pixelation multiplier, default value is " + PixelateCommand.DEFAULT_PIXELATION_MULTIPLIER + "]"
    );

    public static final Command REDUCE_FPS = new ReduceFpsCommand(
            "reducefps",
            "Reduces the FPS of a media file. Optional parameters: [fps reduction multiplier, default value is " + ReduceFpsCommand.DEFAULT_FPS_REDUCTION_MULTIPLIER + "]"
    );

    /**
     * The speech bubble {@link Command}.
     */
    public static final Command SPEECH_BUBBLE = new SpeechBubbleCommand(
            "speechbubble",
            "Overlays a speech bubble over media."
    );

    /**
     * The to-gif {@link Command}.
     */
    public static final Command TO_GIF = new ToGifCommand(
            "gif",
            "Turns media into a GIF."
    );

    public static final Command EMOJI_TO_IMAGE = new EmojiToImageCommand(
            "emojiimage",
            "Gets the image of an emoji. Only works with custom emoji."
    );

    /**
     * The shut-down {@link Command}.
     */
    public static final Command SHUT_DOWN = new ShutDownCommand(
            "shutdown",
            "Shuts down the bot. Only the owner of the bot can use this command."
    );

    /**
     * The help {@link Command}.
     */
    public static final Command HELP = new HelpCommand(
            "help",
            "Lists all commands."
    );

    /**
     * Registers all the {@link Command}s.
     */
    public static void registerCommands() {
        CommandRegistry.register(
                CAPTION,
                STRETCH,
                RESIZE,
                PIXELATE,
                REDUCE_FPS,
                SPEECH_BUBBLE,
                TO_GIF,
                EMOJI_TO_IMAGE,
                SHUT_DOWN,
                HELP
        );
    }
}
