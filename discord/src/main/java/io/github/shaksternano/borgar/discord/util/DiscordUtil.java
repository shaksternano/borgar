package io.github.shaksternano.borgar.discord.util;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordUtil {

    public static long getMaxUploadSize(@Nullable Guild guild) {
        if (guild == null) {
            return Message.MAX_FILE_SIZE;
        } else {
            return guild.getBoostTier().getMaxFileSize();
        }
    }

    public static long getMaxUploadSize(GenericMessageEvent event) {
        var guild = event.isFromGuild() ? event.getGuild() : null;
        return getMaxUploadSize(guild);
    }

    public static CompletableFuture<String> getContentStrippedKeepEmotes(Message message) {
        var guild = message.isFromGuild() ? message.getGuild() : null;
        var displayMessageFuture = CompletableFuture.completedFuture(message.getContentRaw());
        for (User user : message.getMentions().getUsers()) {
            displayMessageFuture = displayMessageFuture.thenCompose(displayMessage ->
                retrieveUserDetails(user, guild)
                    .thenApply(userDetails -> displayMessage.replaceAll(
                        "<@!?" + Pattern.quote(user.getId()) + '>', '@' + Matcher.quoteReplacement(userDetails.username())
                    ))
            );
        }
        return displayMessageFuture.thenApply(displayMessage -> {
            for (GuildChannel mentionedChannel : message.getMentions().getChannels()) {
                displayMessage = displayMessage.replace(mentionedChannel.getAsMention(), '#' + mentionedChannel.getName());
            }
            for (Role mentionedRole : message.getMentions().getRoles()) {
                displayMessage = displayMessage.replace(mentionedRole.getAsMention(), '@' + mentionedRole.getName());
            }
            return displayMessage;
        });
    }

    public static CompletableFuture<UserDetails> retrieveUserDetails(User user, @Nullable Guild guild) {
        if (guild == null) {
            return CompletableFuture.completedFuture(new UserDetails(user));
        } else {
            return guild.retrieveMember(user)
                .submit()
                .thenApply(UserDetails::new)
                .exceptionally(throwable -> new UserDetails(user));
        }
    }

    public record UserDetails(String username, String avatarUrl) {

        public UserDetails(User user) {
            this(user.getEffectiveName(), user.getEffectiveAvatarUrl());
        }

        public UserDetails(Member member) {
            this(member.getEffectiveName(), member.getEffectiveAvatarUrl());
        }
    }
}
