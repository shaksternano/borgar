package io.github.shaksternano.mediamanipulator.command;

public class Commands {

    public static final Command CAPTION = new CaptionCommand(
            "caption",
            "Captions a media file."
    );
    public static final Command STRETCH = new StretchCommand(
            "stretch",
            "Stretches media. Optional parameters: [width stretch multiplier, default value is " + StretchCommand.DEFAULT_WIDTH_MULTIPLIER + "], [height stretch multiplier, default value is " + StretchCommand.DEFAULT_HEIGHT_MULTIPLIER + "]"
    );
    public static final Command TO_GIF = new ToGifCommand(
            "gif",
            "Turns media into a GIF."
    );
    public static final Command SHUT_DOWN = new ShutDownCommand(
            "shutdown",
            "Shuts down the bot. Only the owner of the bot can use this command."
    );
    public static final Command HELP = new HelpCommand(
            "help",
            "Lists all commands."
    );

    public static void registerCommands() {
        CommandRegistry.register(
                CAPTION,
                STRETCH,
                TO_GIF,
                SHUT_DOWN,
                HELP
        );
    }
}
