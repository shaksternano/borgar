package io.github.shaksternano.mediamanipulator;

import io.github.shaksternano.mediamanipulator.command.Command;
import io.github.shaksternano.mediamanipulator.command.Commands;
import io.github.shaksternano.mediamanipulator.command.terminal.TerminalInputListener;
import io.github.shaksternano.mediamanipulator.listener.CommandListener;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulators;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ProgramArguments;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.RestAction;
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
    public static final Logger LOGGER = LoggerFactory.getLogger("Media Manipulator");

    /**
     * The name of the program argument or environment variable that contains the Discord bot token.
     */
    private static final String DISCORD_BOT_TOKEN_ARGUMENT_NAME = "DISCORD_BOT_TOKEN";

    /**
     * The name of the program argument or environment variable that contains the Tenor API key.
     */
    private static final String TENOR_API_KEY_ARGUMENT_NAME = "TENOR_API_KEY";

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
    public static void main(String[] args) {
        FileUtil.cleanTempDirectory();
        arguments = new ProgramArguments(args);
        String discordBotToken = initDiscordBotToken();
        initTenorApiKey();
        Commands.registerCommands();
        MediaManipulators.registerMediaManipulators();
        initJda(discordBotToken);
        Fonts.registerFonts();
        RestAction.setDefaultFailure(throwable -> Main.LOGGER.error("An error occurred while executing a REST action.", throwable));
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
            LOGGER.error("Please provide a Discord bot token as an argument in the form of " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + "=<token> or set the environment variable " + DISCORD_BOT_TOKEN_ARGUMENT_NAME + " to the Discord bot token.");
            System.exit(1);
            throw new AssertionError("The program should not reach this point!");
        }
    }

    /**
     * Sets the Tenor API key from the program arguments or the environment variable.
     */
    private static void initTenorApiKey() {
        Optional<String> apiKeyOptional = arguments.getArgumentOrEnvironmentVariable(TENOR_API_KEY_ARGUMENT_NAME);

        if (apiKeyOptional.isPresent()) {
            String tenorApiKey = apiKeyOptional.orElseThrow();

            if (tenorApiKey.equals(Main.getTenorApiKey())) {
                LOGGER.warn("Tenor API key provided is the same as the default, restricted, rate limited example key (" + getTenorApiKey() + ")!");
            } else {
                Main.tenorApiKey = tenorApiKey;
                LOGGER.info("Using custom Tenor API key!");
            }
        } else {
            LOGGER.warn("No Tenor API key provided, using default, restricted, rate limited example key (" + getTenorApiKey() + ").");
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
        } catch (LoginException e) {
            LOGGER.error("Invalid token!");
            System.exit(1);
        }

        Thread commandThread = new Thread(new TerminalInputListener());
        commandThread.start();

        jda.getPresence().setActivity(Activity.playing("gaming"));
        jda.addEventListener(CommandListener.INSTANCE);

        Command helpCommand = Commands.HELP;
        jda.updateCommands()
                .addCommands(net.dv8tion.jda.api.interactions.commands.build.Commands.slash(helpCommand.getName(), helpCommand.getDescription()))
                .queue();

        jda.retrieveApplicationInfo().queue(
                applicationInfo -> ownerId = applicationInfo.getOwner().getIdLong(),
                throwable -> LOGGER.error("Failed to get the owner ID of this bot, owner exclusive functionality won't available!", throwable)
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
