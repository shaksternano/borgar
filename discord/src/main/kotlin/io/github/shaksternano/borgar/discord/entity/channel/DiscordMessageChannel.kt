package io.github.shaksternano.borgar.discord.entity.channel

import io.github.shaksternano.borgar.core.io.DataSource
import io.github.shaksternano.borgar.core.io.useHttpClient
import io.github.shaksternano.borgar.discord.asFlow
import io.github.shaksternano.borgar.discord.await
import io.github.shaksternano.borgar.discord.entity.DiscordMessage
import io.github.shaksternano.borgar.discord.toFileUpload
import io.github.shaksternano.borgar.messaging.builder.MessageCreateBuilder
import io.github.shaksternano.borgar.messaging.entity.Message
import io.github.shaksternano.borgar.messaging.entity.channel.MessageChannel
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Webhook
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData

data class DiscordMessageChannel(
    private val discordMessageChannel: net.dv8tion.jda.api.entities.channel.middleman.MessageChannel,
) : MessageChannel, DiscordChannel(discordMessageChannel) {

    override suspend fun sendTyping() {
        discordMessageChannel.sendTyping().await()
    }

    override suspend fun stopTyping() = Unit

    override suspend fun createMessage(block: MessageCreateBuilder.() -> Unit): Message {
        val builder = MessageCreateBuilder().apply(block)
        val discordMessage = if (builder.username == null && builder.avatarUrl == null) {
            sendStandardMessage(builder)
        } else {
            sendWebhookMessage(builder)
        }
        return DiscordMessage(discordMessage)
    }

    private suspend fun sendStandardMessage(messageBuilder: MessageCreateBuilder): net.dv8tion.jda.api.entities.Message =
        discordMessageChannel.sendMessage(messageBuilder.convert())
            .setMessageReference(messageBuilder.referencedMessageIds.firstOrNull())
            .setSuppressEmbeds(messageBuilder.suppressEmbeds)
            .await()

    private suspend fun sendWebhookMessage(messageBuilder: MessageCreateBuilder): net.dv8tion.jda.api.entities.Message {
        val webhook = getOrCreateWebhook()
        val messageAction = webhook.sendMessage(messageBuilder.convert())
        if (discordMessageChannel is ThreadChannel) {
            messageAction.setThread(discordMessageChannel)
        }
        messageAction.setSuppressEmbeds(messageBuilder.suppressEmbeds)

        val selfUser = discordMessageChannel.jda.selfUser
        var selfMember: Member? = null
        var setMember = false
        val guild =
            if (discordMessageChannel is GuildChannel) discordMessageChannel.guild
            else null
        val username = messageBuilder.username ?: run {
            selfMember = guild?.retrieveMember(selfUser)?.await()
            setMember = true
            selfMember?.effectiveName ?: selfUser.effectiveName
        }
        val avatarUrl = messageBuilder.avatarUrl ?: run {
            if (!setMember) {
                selfMember = guild?.retrieveMember(selfUser)?.await()
            }
            selfMember?.effectiveAvatarUrl ?: selfUser.effectiveAvatarUrl
        }

        messageAction.setUsername(username)
        messageAction.setAvatarUrl(avatarUrl)
        return messageAction.await()
    }

    private suspend fun getOrCreateWebhook(): Webhook {
        val webhookContainer = getWebhookContainer()
            ?: throw UnsupportedOperationException("Webhook container not found")
        val webhooks = webhookContainer.retrieveWebhooks().await()
        return getOrCreateWebhook(webhooks, webhookContainer)
    }

    private fun getWebhookContainer(): IWebhookContainer? {
        val channel = if (discordMessageChannel is ThreadChannel) {
            discordMessageChannel.parentChannel
        } else {
            discordMessageChannel
        }
        return channel as? IWebhookContainer
    }

    private suspend fun getOrCreateWebhook(webhooks: List<Webhook>, webhookContainer: IWebhookContainer): Webhook {
        val selfUser = webhookContainer.jda.selfUser
        val ownWebhook = webhooks.find {
            it.ownerAsUser == selfUser
        }
        if (ownWebhook != null) return ownWebhook
        val avatarUrl = selfUser.effectiveAvatarUrl
        val avatarBytes = useHttpClient {
            it.get(avatarUrl).readBytes()
        }
        val icon = Icon.from(avatarBytes)
        return webhookContainer.createWebhook(selfUser.name)
            .setAvatar(icon)
            .await()
    }

    private fun MessageCreateBuilder.convert(): MessageCreateData {
        val builder = net.dv8tion.jda.api.utils.messages.MessageCreateBuilder()
        builder.setContent(content)
        builder.setFiles(files.map(DataSource::toFileUpload))
        return builder.build()
    }

    override fun getPreviousMessages(beforeId: String): Flow<Message> {
        return discordMessageChannel.iterableHistory
            .skipTo(beforeId.toLong())
            .asFlow()
            .map { DiscordMessage(it) }
    }
}
