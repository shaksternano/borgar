package io.github.shaksternano.mediamanipulator.logging;

import io.github.shaksternano.mediamanipulator.util.LimitedStringBuilder;
import io.github.shaksternano.mediamanipulator.util.StringUtil;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.regex.Pattern;

public class DiscordLogger extends InterceptLogger {

    private final MessageChannel channel;

    public DiscordLogger(Logger logger, MessageChannel channel) {
        super(logger);

        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null!");
        } else {
            this.channel = channel;
        }
    }

    @Override
    protected void intercept(Level level, String message, @Nullable Throwable t, Object... arguments) {
        LimitedStringBuilder builder = new LimitedStringBuilder(2000);
        String messageWithArguments = formatArguments(message, arguments);
        builder.append("**").append(level).append("** - ").append(getName()).append("\n").append(messageWithArguments);

        if (t != null) {
            builder.append("\nStacktrace:\n").append(StringUtil.getStacktrace(t));
        }

        for (String part : builder.getParts()) {
            channel.sendMessage(part).queue();
        }
    }

    private static String formatArguments(String message, Object... arguments) {
        String messageWithArguments = message;

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i].toString();
            messageWithArguments = messageWithArguments.replaceFirst(Pattern.quote("{}"), argument);
            messageWithArguments = messageWithArguments.replaceAll(Pattern.quote("{" + i + "}"), argument);
        }

        return messageWithArguments;
    }
}
