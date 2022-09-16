package io.github.shaksternano.mediamanipulator;

import io.github.shaksternano.mediamanipulator.command.Command;
import io.github.shaksternano.mediamanipulator.command.util.Commands;
import io.github.shaksternano.mediamanipulator.command.util.TerminalInputListener;
import io.github.shaksternano.mediamanipulator.emoji.EmojiUtil;
import io.github.shaksternano.mediamanipulator.image.backgroundimage.ResourceContainerImageInfo;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.image.writer.util.ImageWriters;
import io.github.shaksternano.mediamanipulator.listener.CommandListener;
import io.github.shaksternano.mediamanipulator.logging.DiscordLogger;
import io.github.shaksternano.mediamanipulator.mediamanipulator.util.MediaManipulators;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.MiscUtil;
import io.github.shaksternano.mediamanipulator.util.ProgramArguments;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The program's main class.
 */
public class Main {

    /**
     * The program's {@link Logger}.
     */
    private static final Logger LOGGER = MiscUtil.createLogger("Media Manipulator");

    @Nullable
    private static Logger discordLogger;

    /**
     * The name of the program argument or environment variable that contains the Discord bot token.
     */
    private static final String DISCORD_BOT_TOKEN_ARGUMENT_NAME = "DISCORD_BOT_TOKEN";

    /**
     * The name of the program argument or environment variable that contains the Tenor API key.
     */
    private static final String TENOR_API_KEY_ARGUMENT_NAME = "TENOR_API_KEY";

    private static final String DISCORD_LOG_CHANNEL_ID_ARGUMENT_NAME = "DISCORD_LOG_CHANNEL_ID";

    /**
     * The program's {@link JDA} instance.
     */
    private static JDA jda;

    /**
     * The ID of the user that owns the Discord bot.
     */
    private static long ownerId = 0;

    /**
     * The Tenor API key. The default value set is a restricted, rate limited example key (LIVDSRZULELA).
     */
    private static String tenorApiKey = "LIVDSRZULELA";

    private static ProgramArguments arguments;

    /**
     * The program's main class.
     *
     * @param args The program arguments.
     */
    public static void main(String[] args) {
        ImageReaders.registerImageReaders();
        ImageWriters.registerImageWriters();
        Fonts.registerFonts();
        ResourceContainerImageInfo.validate();

        arguments = new ProgramArguments(args);

        initJda(initDiscordBotToken());

        initDiscordLogger();

        getLogger().info("Starting!");

        initTenorApiKey();

        Commands.registerCommands();
        MediaManipulators.registerMediaManipulators();

        Thread commandThread = new Thread(new TerminalInputListener());
        commandThread.start();

        EmojiUtil.initEmojiUnicodeSet();
        configureJda();

        getLogger().info("Initialised!");
    }

    /**
     * Gets the Discord bot token from the program arguments or the environment variable.
     * If the Discord bot token is not set, the program terminates.
     *
     * @return The Discord bot token.
     */
    private static String initDiscordBotToken() {
        Optional<String> tokenOptional = arguments.getArgumentOrEnvironmentVariable(DISCORD_BOT_TOKEN_ARGUMENT_NAME);
        return tokenOptional.orElseThrow(() -> {
            getLogger().error("Please provide a Discord bot token as an argument in the form of " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + "=<token> or set the environment variable " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + " to the Discord bot token.");
            Main.shutdown(1);
            return new AssertionError("The program should not reach this point!");
        });
    }

    private static void initDiscordLogger() {
        arguments.getArgumentOrEnvironmentVariable(DISCORD_LOG_CHANNEL_ID_ARGUMENT_NAME).ifPresentOrElse(logChannelIdString -> {
            try {
                long logChannelIdLong = Long.parseLong(logChannelIdString);
                getLogChannel(logChannelIdLong).ifPresentOrElse(logChannel -> {
                    discordLogger = new DiscordLogger(LOGGER, logChannel);
                    LOGGER.info("Logging to Discord channel with ID!");
                }, () -> getLogger().error("Could not find Discord channel with ID!"));
            } catch (NumberFormatException e) {
                getLogger().error("Provided Discord channel ID is not a number!");
            }
        }, () -> getLogger().info("No log channel ID provided."));
    }

    private static Optional<MessageChannel> getLogChannel(long channelId) {
        Optional<MessageChannel> channelOptional = Optional.ofNullable(jda.getChannelById(MessageChannel.class, channelId));

        if (channelOptional.isEmpty()) {
            getLogger().error("Could not find channel with ID " + channelId);
        }

        return channelOptional;
    }

    /**
     * Sets the Tenor API key from the program arguments or the environment variable.
     */
    private static void initTenorApiKey() {
        Optional<String> apiKeyOptional = arguments.getArgumentOrEnvironmentVariable(TENOR_API_KEY_ARGUMENT_NAME);

        apiKeyOptional.ifPresentOrElse(tenorApiKey -> {
            if (tenorApiKey.equals(Main.getTenorApiKey())) {
                getLogger().warn("Tenor API key provided is the same as the default, restricted, rate limited example key (" + getTenorApiKey() + ")!");
            } else {
                Main.tenorApiKey = tenorApiKey;
                getLogger().info("Using custom Tenor API key!");
            }
        }, () -> getLogger().warn("No Tenor API key provided, using default, restricted, rate limited example key (" + getTenorApiKey() + ")."));
    }

    /**
     * Initializes the JDA instance.
     *
     * @param token The Discord bot token.
     */
    private static void initJda(String token) {
        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build();
            RestAction.setDefaultFailure(throwable -> LOGGER.error("An error occurred while executing a REST action.", throwable));
            jda.awaitReady();
            return;
        } catch (LoginException e) {
            getLogger().error("Invalid token!");
        } catch (InterruptedException e) {
            getLogger().error("Interrupted while waiting for JDA to be ready!", e);
        }

        Main.shutdown(1);
    }

    private static void configureJda() {
        jda.getPresence().setActivity(Activity.playing("fortnite battle pass"));

        jda.retrieveApplicationInfo().queue(
                applicationInfo -> ownerId = applicationInfo.getOwner().getIdLong(),
                throwable -> getLogger().error("Failed to get the owner ID of this bot, owner exclusive functionality won't available!", throwable)
        );

        Command helpCommand = Commands.HELP;
        jda.updateCommands()
                .addCommands(net.dv8tion.jda.api.interactions.commands.build.Commands.slash(helpCommand.getName(), helpCommand.getDescription()))
                .queue(commands -> {
                }, throwable -> getLogger().error("Failed to add slash commands!", throwable));
        jda.addEventListener(new CommandListener());
    }

    /**
     * Terminates the program.
     */
    public static void shutdown(int exitCode) {
        try {
            TimeUnit.SECONDS.sleep(1);

            if (jda != null) {
                jda.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for the program to terminate!", e);
        } catch (Throwable t) {
            LOGGER.error("An error occurred while terminating the program!", t);
        }

        try {
            System.exit(exitCode);
        } catch (Throwable t) {
            LOGGER.error("Failed to terminate the program!", t);
        }
    }

    public static Logger getLogger() {
        return discordLogger == null ? LOGGER : discordLogger;
    }

    /**
     * The ID of the user that owns the Discord bot.
     *
     * @return The ID of the user that owns the Discord bot.
     */
    public static long getOwnerId() {
        return ownerId;
    }

    /**
     * Gets the Tenor API key.
     *
     * @return The Tenor API key.
     */
    public static String getTenorApiKey() {
        return tenorApiKey;
    }

    public static String getRootPackage() {
        return Main.class.getPackageName();
    }
}
