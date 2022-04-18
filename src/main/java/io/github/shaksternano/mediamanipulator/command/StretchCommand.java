package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class StretchCommand extends MediaCommand {

    public static final int DEFAULT_WIDTH_MULTIPLIER = 2;
    public static final int DEFAULT_HEIGHT_MULTIPLIER = 1;

    protected StretchCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = DEFAULT_WIDTH_MULTIPLIER;
        float heightMultiplier = DEFAULT_HEIGHT_MULTIPLIER;

        if (arguments.length > 0) {
            try {
                widthMultiplier = Float.parseFloat(arguments[0]);
                if (arguments.length > 1) {
                    try {
                        heightMultiplier = Float.parseFloat(arguments[1]);
                    } catch (NumberFormatException e) {
                        event.getMessage().reply("Height multiplier \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
                    }
                }
            } catch (NumberFormatException e) {
                event.getMessage().reply("Width multiplier \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
            }
        }

        return manipulator.stretch(mediaFile, widthMultiplier, heightMultiplier);
    }
}
