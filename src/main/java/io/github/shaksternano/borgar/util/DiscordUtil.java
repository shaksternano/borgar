package io.github.shaksternano.borgar.util;

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
        var displayMessageFuture = CompletableFuture.completedFuture(message.getContentRaw());
        for (User user : message.getMentions().getUsers()) {
            displayMessageFuture = displayMessageFuture.thenCompose(displayMessage ->
                retrieveUserDetails(message)
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

    public static CompletableFuture<UserDetails> retrieveUserDetails(Message message) {
        var author = message.getAuthor();
        if (message.isFromGuild()) {
            return message.getGuild()
                .retrieveMember(author)
                .submit()
                .thenApply(UserDetails::new)
                .exceptionally(throwable -> new UserDetails(author));
        } else {
            return CompletableFuture.completedFuture(new UserDetails(author));
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
