package io.github.shaksternano.borgar.discord.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.discord.Main;
import io.github.shaksternano.borgar.discord.command.util.CommandResponse;
import io.github.shaksternano.borgar.discord.util.MessageUtil;
import io.github.shaksternano.borgar.discord.util.tenor.TenorMediaType;
import io.github.shaksternano.borgar.discord.util.tenor.TenorUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TenorUrlCommand extends BaseCommand<Void> {

    public static final String DEFAULT_MEDIA_TYPE = "mediumgif";

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public TenorUrlCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        String mediaType;
        if (arguments.isEmpty()) {
            mediaType = DEFAULT_MEDIA_TYPE;
        } else {
            mediaType = arguments.get(0);
        }
        if (!TenorMediaType.isValidMediaType(mediaType)) {
            return new CommandResponse<Void>("Media type `" + mediaType + "` is invalid.").asFuture();
        }
        return MessageUtil.getUrl(event.getMessage()).thenCompose(urlOptional ->
            urlOptional.map(url -> {
                try {
                    return TenorUtil.retrieveTenorMediaUrl(url, mediaType, Main.getTenorApiKey())
                        .thenApply(tenorUrl -> new CommandResponse<Void>(tenorUrl));
                } catch (IllegalArgumentException e) {
                    return new CommandResponse<Void>("Invalid Tenor URL!").asFuture();
                }
            }).orElseGet(() -> new CommandResponse<Void>("No URL found!").asFuture())
        );
    }
}
