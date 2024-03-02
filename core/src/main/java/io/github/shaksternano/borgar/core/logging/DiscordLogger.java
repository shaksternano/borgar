package io.github.shaksternano.borgar.core.logging;

import io.github.shaksternano.borgar.core.util.CompletableFutureUtil;
import io.github.shaksternano.borgar.core.util.LimitedStringBuilder;
import io.github.shaksternano.borgar.core.util.StringUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Optional;
import java.util.regex.Pattern;

public class DiscordLogger extends InterceptLogger {

    private final long logChannelId;
    private final JDA jda;
    private boolean notFoundLogged = false;

    public DiscordLogger(Logger logger, long logChannelId, JDA jda) {
        super(logger);
        this.logChannelId = logChannelId;
        this.jda = jda;
    }

    @Override
    protected void intercept(@NotNull Level level, @Nullable String message, @Nullable Throwable t, Object... arguments) {
        if (message == null) {
            return;
        }
        getLogChannel(logChannelId, jda).ifPresentOrElse(channel -> {
            LimitedStringBuilder builder = new LimitedStringBuilder(2000);
            String messageWithArguments = formatArguments(message, arguments);
            builder.append("**").append(level).append("** - ").append(getName()).append("\n").append(messageWithArguments);

            if (t != null) {
                builder.append("\nStacktrace:\n").append(StringUtil.getStacktrace(t));
            }

            CompletableFutureUtil.forEachSequentiallyAsync(
                builder.getParts(),
                (part, index) -> channel.sendMessage(part).submit()
            ).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    getDelegate().error("Failed to send message to Discord", throwable);
                }
            });
        }, () -> {
            if (!notFoundLogged) {
                notFoundLogged = true;
                getDelegate().warn("Message channel with ID {} not found!", logChannelId);
            }
        });
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

    private static Optional<MessageChannel> getLogChannel(long logChannelId, JDA jda) {
        return Optional.ofNullable(jda.getChannelById(MessageChannel.class, logChannelId));
    }
}
