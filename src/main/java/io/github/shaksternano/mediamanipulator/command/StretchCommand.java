package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulation.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class StretchCommand extends MediaCommand {

    public static final StretchCommand INSTANCE = new StretchCommand("stretch", "Stretches media.");

    protected StretchCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File mediaFile, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float widthMultiplier = 1;
        float heightMultiplier = 1;

        if (arguments.length > 0) {
            try {
                widthMultiplier = Float.parseFloat(arguments[0]);
                if (arguments.length > 1) {
                    try {
                        heightMultiplier = Float.parseFloat(arguments[1]);
                    } catch (NumberFormatException e) {
                        event.getMessage().reply("Height \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
                    }
                }
            } catch (NumberFormatException e) {
                event.getMessage().reply("Width \"" + arguments[0] + "\" is not a number. Using default value of " + widthMultiplier + ".").queue();
            }
        }

        return manipulator.stretch(mediaFile, widthMultiplier, heightMultiplier);
    }
}
