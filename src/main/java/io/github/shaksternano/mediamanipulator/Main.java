package io.github.shaksternano.mediamanipulator;

import io.github.shaksternano.mediamanipulator.command.Commands;
import io.github.shaksternano.mediamanipulator.listener.CommandListener;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulators;
import io.github.shaksternano.mediamanipulator.command.terminal.TerminalInputListener;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger("Media Manipulator");
    
    private static final String DISCORD_BOT_TOKEN_ENVIRONMENT_VARIABLE = "DISCORD_BOT_TOKEN";
    private static final String TENOR_API_KEY_ENVIRONMENT_VARIABLE = "TENOR_API_KEY";
    
    private static long ownerId = 0;

    /**
     * The Tenor API key. The default value set is a restricted, rate limited example key (LIVDSRZULELA).
     */
    private static String tenorApiKey = "LIVDSRZULELA";

    public static void main(String[] args) {
        FileUtil.cleanTempDirectory();

        String token = parseDiscordBotToken(args);
        
        if (token.isEmpty()) {
            token = System.getenv(DISCORD_BOT_TOKEN_ENVIRONMENT_VARIABLE);
            
            if (token == null) {
                LOGGER.error("Please provide a Discord bot token as the first argument!");
                System.exit(1);
            } else {
                LOGGER.info("Using Discord bot token from environment variable " + DISCORD_BOT_TOKEN_ENVIRONMENT_VARIABLE + ".");
            }
        } else {
            LOGGER.info("Using Discord bot token from program arguments.");
        }

        String tenorApiKey = parseTenorApiKey(args);
        if (tenorApiKey.isEmpty()) {
            tenorApiKey = System.getenv(TENOR_API_KEY_ENVIRONMENT_VARIABLE);

            if (tenorApiKey == null) {
                LOGGER.warn("No Tenor API key provided as the second argument or from environment variable " + TENOR_API_KEY_ENVIRONMENT_VARIABLE + ", using default, restricted, rate limited example key (" + getTenorApiKey() + ").");
            } else {
                if (tenorApiKey.equals(Main.getTenorApiKey())) {
                    LOGGER.warn("Tenor API key provided from environment variable " + TENOR_API_KEY_ENVIRONMENT_VARIABLE + " is the same as the default, restricted, rate limited example key (" + getTenorApiKey() + ")!");
                } else {
                    Main.tenorApiKey = tenorApiKey;
                    LOGGER.info("Using Tenor API key from environment variable " + TENOR_API_KEY_ENVIRONMENT_VARIABLE + ".");
                }
            }
        } else {
            if (tenorApiKey.equals(Main.getTenorApiKey())) {
                LOGGER.warn("Tenor API key provided as the second argument is the same as the default, restricted, rate limited example key (" + getTenorApiKey() + ")!");
            } else {
                Main.tenorApiKey = tenorApiKey;
                LOGGER.info("Using Tenor API key from program arguments.");
            }
        }

        JDA jda = null;

        try {
            jda = JDABuilder.createDefault(token).build();
        } catch (LoginException e) {
            LOGGER.error("Invalid token!");
            System.exit(1);
        }

        Thread commandThread = new Thread(new TerminalInputListener(jda));
        commandThread.start();

        jda.getPresence().setActivity(Activity.playing("gaming"));
        jda.addEventListener(CommandListener.INSTANCE);
        jda.updateCommands()
                .addCommands(net.dv8tion.jda.api.interactions.commands.build.Commands.slash(Commands.HELP.getName(), Commands.HELP.getDescription()))
                .queue();

        jda.retrieveApplicationInfo().queue(
                applicationInfo -> ownerId = applicationInfo.getOwner().getIdLong(),
                throwable -> LOGGER.error("Failed to get the owner ID of this bot, owner exclusive functionality won't available!", throwable)
        );

        Commands.registerCommands();
        Fonts.registerFonts();
        MediaManipulators.registerMediaManipulators();
    }

    public static long getOwnerId() {
        return ownerId;
    }

    private static String parseDiscordBotToken(String[] args) {
        if (args.length > 0) {
            return args[0];
        } else {
            return "";
        }
    }

    private static String parseTenorApiKey(String[] args) {
        if (args.length > 1) {
            return args[1];
        } else {
            return "";
        }
    }

    public static String getTenorApiKey() {
        return tenorApiKey;
    }
}
