package io.github.shaksternano.mediamanipulator;

import io.github.shaksternano.mediamanipulator.command.Command;
import io.github.shaksternano.mediamanipulator.command.Commands;
import io.github.shaksternano.mediamanipulator.command.terminal.TerminalInputListener;
import io.github.shaksternano.mediamanipulator.listener.CommandListener;
import io.github.shaksternano.mediamanipulator.logging.DiscordLogger;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulators;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ProgramArguments;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Optional;

/**
 * The program's main class.
 */
public class Main {

    /**
     * The program's {@link Logger}.
     */
    private static final Logger logger = LoggerFactory.getLogger("Media Manipulator");

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
     * The entry point of the program.
     *
     * @param args The program arguments.
     */
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        arguments = new ProgramArguments(args);

        initJda(initDiscordBotToken());
        jda.awaitReady();

        arguments.getArgumentOrEnvironmentVariable(DISCORD_LOG_CHANNEL_ID_ARGUMENT_NAME).ifPresentOrElse(logChannelIdString -> {
            try {
                long logChannelIdLong = Long.parseLong(logChannelIdString);
                getLogChannel(logChannelIdLong).ifPresentOrElse(logChannel -> {
                    discordLogger = new DiscordLogger(logger, logChannel);
                    logger.info("Logging to Discord channel with ID!");
                }, () -> getLogger().error("Could not find Discord channel with ID!"));
            } catch (NumberFormatException e) {
                getLogger().error("Provided Discord channel ID is not a number!");
            }
        }, () -> getLogger().info("No log channel ID provided."));

        getLogger().info("Starting!");
        FileUtil.cleanTempDirectory();

        initTenorApiKey();

        Commands.registerCommands();
        MediaManipulators.registerMediaManipulators();
        Fonts.registerFonts();

        Thread commandThread = new Thread(new TerminalInputListener());
        commandThread.start();

        configureJda();
    }

    /**
     * Gets the Discord bot token from the program arguments or the environment variable.
     * If the Discord bot token is not set, the program terminates.
     *
     * @return The Discord bot token.
     */
    private static String initDiscordBotToken() {
        Optional<String> tokenOptional = arguments.getArgumentOrEnvironmentVariable(DISCORD_BOT_TOKEN_ARGUMENT_NAME);

        if (tokenOptional.isPresent()) {
            return tokenOptional.orElseThrow();
        } else {
            getLogger().error("Please provide a Discord bot token as an argument in the form of " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + "=<token> or set the environment variable " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + " to the Discord bot token.");
            System.exit(1);
            throw new AssertionError("The program should not reach this point!");
        }
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

        if (apiKeyOptional.isPresent()) {
            String tenorApiKey = apiKeyOptional.orElseThrow();

            if (tenorApiKey.equals(Main.getTenorApiKey())) {
                getLogger().warn("Tenor API key provided is the same as the default, restricted, rate limited example key (" + getTenorApiKey() + ")!");
            } else {
                Main.tenorApiKey = tenorApiKey;
                getLogger().info("Using custom Tenor API key!");
            }
        } else {
            getLogger().warn("No Tenor API key provided, using default, restricted, rate limited example key (" + getTenorApiKey() + ").");
        }
    }

    /**
     * Initializes the JDA instance.
     *
     * @param token The Discord bot token.
     */
    private static void initJda(String token) {
        try {
            jda = JDABuilder.createDefault(token).build();
            RestAction.setDefaultFailure(throwable -> logger.error("An error occurred while executing a REST action.", throwable));
        } catch (LoginException e) {
            getLogger().error("Invalid token!");
            System.exit(1);
        }
    }

    private static void configureJda() {
        jda.getPresence().setActivity(Activity.playing("gaming"));
        jda.addEventListener(CommandListener.INSTANCE);

        Command helpCommand = Commands.HELP;
        jda.updateCommands()
                .addCommands(net.dv8tion.jda.api.interactions.commands.build.Commands.slash(helpCommand.getName(), helpCommand.getDescription()))
                .queue();

        jda.retrieveApplicationInfo().queue(
                applicationInfo -> ownerId = applicationInfo.getOwner().getIdLong(),
                throwable -> getLogger().error("Failed to get the owner ID of this bot, owner exclusive functionality won't available!", throwable)
        );
    }

    /**
     * Terminates the program.
     */
    public static void shutdown() {
        if (jda != null) {
            jda.shutdownNow();
        }

        FileUtil.cleanTempDirectory();
        System.exit(0);
    }

    public static Logger getLogger() {
        return discordLogger == null ? logger : discordLogger;
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
}
