package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MemoryUsageCommand extends Command {

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    protected MemoryUsageCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public void execute(String[] arguments, MessageReceivedEvent event) {
        int toMb = 1024 * 1024;
        event.getMessage().reply("Current memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / toMb  + "/" + Runtime.getRuntime().totalMemory() / toMb + "MB").queue();
    }
}
