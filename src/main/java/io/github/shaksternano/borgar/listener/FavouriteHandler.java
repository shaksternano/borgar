package io.github.shaksternano.borgar.listener;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.io.Files;
import io.github.shaksternano.borgar.Main;
import io.github.shaksternano.borgar.command.AddFavouriteCommand;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.StringUtil;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FavouriteHandler {

    public static void sendFavouriteFile(MessageReceivedEvent event) {
        var message = event.getMessage();
        var urls = StringUtil.extractUrls(message.getContentRaw());
        if (urls.isEmpty()) {
            return;
        }
        var aliasUrl = urls.get(0);

        var fileName = Files.getNameWithoutExtension(aliasUrl);
        if (!fileName.startsWith(AddFavouriteCommand.ALIAS_PREFIX)) {
            return;
        }

        getUrl(aliasUrl).ifPresent(url ->
                sendUrl(url, event).thenAccept(unused -> {
                    try {
                        event.getMessage().delete().queue();
                    } catch (Exception ignored) {
                    }
                })
        );
    }

    private static Optional<String> getUrl(String aliasUrl) {
        var fileName = Files.getNameWithoutExtension(aliasUrl);
        var nameParts = fileName.split("_", 2);
        if (nameParts.length == 2) {
            try {
                var decodedBytes = Base64.getDecoder().decode(nameParts[1]);
                return Optional.of(new String(decodedBytes));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Optional.empty();
    }

    private static CompletableFuture<Void> sendUrl(String url, MessageReceivedEvent event) {
        return getOrCreateWebhook(event.getChannel())
                .thenApply(webhook -> WebhookMessageSender.create(webhook, event.getChannel()))
                .exceptionally(throwable -> new StandardMessageSender(event.getChannel()))
                .thenCompose(sender -> sendMessage(url, sender, event));
    }

    private static CompletableFuture<Webhook> getOrCreateWebhook(Channel channel) {
        return getWebhookContainer(channel).map(webhookContainer -> {
            try {
                return webhookContainer.retrieveWebhooks()
                        .submit()
                        .thenCompose(webhooks -> getOrCreateWebhook(webhooks, webhookContainer));
            } catch (Exception e) {
                return CompletableFuture.<Webhook>failedFuture(e);
            }
        }).orElseGet(() -> CompletableFuture.failedFuture(new NoSuchElementException()));
    }

    private static Optional<IWebhookContainer> getWebhookContainer(Channel channel) {
        if (channel instanceof ThreadChannel threadChannel) {
            channel = threadChannel.getParentChannel();
        }
        if (channel instanceof IWebhookContainer webhookContainer) {
            return Optional.of(webhookContainer);
        } else {
            return Optional.empty();
        }
    }

    private static CompletableFuture<Webhook> getOrCreateWebhook(Collection<Webhook> webhooks, IWebhookContainer webhookContainer) {
        return getOwnWebhook(webhooks)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> createWebhook(webhookContainer));
    }

    private static Optional<Webhook> getOwnWebhook(Collection<Webhook> webhooks) {
        return webhooks.stream()
                .filter(FavouriteHandler::isOwnWebhook)
                .findAny();
    }

    private static boolean isOwnWebhook(Webhook webhook) {
        return webhook.getJDA().getSelfUser().equals(webhook.getOwnerAsUser());
    }

    private static CompletableFuture<Webhook> createWebhook(IWebhookContainer webhookContainer) {
        var icon = getIcon(webhookContainer.getJDA().getSelfUser()).orElse(null);
        return createWebhook(webhookContainer, icon);
    }

    private static Optional<Icon> getIcon(User user) {
        var avatarUrl = user.getEffectiveAvatarUrl();
        try (var iconStream = new URL(avatarUrl).openStream()) {
            return Optional.of(Icon.from(iconStream));
        } catch (IOException e) {
            Main.getLogger().error("Failed to create icon for " + user + ".", e);
            return Optional.empty();
        }
    }

    private static CompletableFuture<Webhook> createWebhook(IWebhookContainer webhookContainer, @Nullable Icon icon) {
        try {
            return webhookContainer.createWebhook(webhookContainer.getJDA().getSelfUser().getName())
                    .setAvatar(icon)
                    .submit();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static CompletableFuture<Void> sendMessage(String content, MessageSender sender, MessageReceivedEvent event) {
        return DiscordUtil.retrieveUserDetails(event.getMessage()).thenCompose(userDetails ->
                sender.sendMessage(content, userDetails.username(), userDetails.avatarUrl())
        ).thenAccept(unused -> sender.close());
    }

    @FunctionalInterface
    private interface MessageSender extends Closeable {

        CompletableFuture<Void> sendMessage(String content, String username, String avatarUrl);

        @Override
        default void close() {
        }
    }

    private record WebhookMessageSender(WebhookClient client) implements MessageSender {

        @Override
        public CompletableFuture<Void> sendMessage(String content, String username, String avatarUrl) {
            var message = new WebhookMessageBuilder()
                    .setContent(content)
                    .setUsername(username)
                    .setAvatarUrl(avatarUrl)
                    .build();
            return client.send(message).thenAccept(unused -> {
            });
        }

        @Override
        public void close() {
            client.close();
        }

        private static MessageSender create(Webhook webhook, Channel channel) {
            var client = JDAWebhookClient.from(webhook);
            if (channel instanceof ThreadChannel) {
                client = client.onThread(channel.getIdLong());
            }
            return new WebhookMessageSender(client);
        }
    }

    private record StandardMessageSender(MessageChannel channel) implements MessageSender {

        @Override
        public CompletableFuture<Void> sendMessage(String content, String username, String avatarUrl) {
            return channel.sendMessage(content)
                    .submit()
                    .thenAccept(unused -> {
                    });
        }
    }
}
