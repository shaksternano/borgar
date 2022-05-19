package io.github.shaksternano.mediamanipulator.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MemoryUsageCommand extends BotOwnerCommand {

    private static final int TO_MB = 1024 * 1024;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public MemoryUsageCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void botOwnerOperation(String[] arguments, MessageReceivedEvent event) {
        event.getMessage().reply("Current memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / TO_MB + "/" + Runtime.getRuntime().maxMemory() / TO_MB + "MB").queue();
    }
}
