package io.github.shaksternano.mediamanipulator.util;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordUtil {

    public static final int DISCORD_MAX_DISPLAY_WIDTH = 400;
    public static final int DISCORD_MAX_DISPLAY_HEIGHT = 300;

    public static long getMaxUploadSize(@Nullable Guild guild) {
        if (guild == null) {
            return Message.MAX_FILE_SIZE;
        } else {
            return guild.getBoostTier().getMaxFileSize();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static String getContentStrippedKeepEmotes(Message message) {
        String displayMessage = message.getContentRaw();
        for (User user : message.getMentions().getUsers()) {
            String name = message.getGuild()
                .retrieveMember(user)
                .submit()
                .thenApply(Member::getEffectiveName)
                .exceptionally(throwable -> user.getName())
                .join();
            displayMessage = displayMessage.replaceAll("<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(name));
        }
        for (GuildChannel mentionedChannel : message.getMentions().getChannels()) {
            displayMessage = displayMessage.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
        }
        for (Role mentionedRole : message.getMentions().getRoles()) {
            displayMessage = displayMessage.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
        }

        return MarkdownSanitizer.sanitize(displayMessage);
    }
}
