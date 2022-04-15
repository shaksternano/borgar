package io.github.shaksternano.mediamanipulator;

import io.github.shaksternano.mediamanipulator.command.CaptionCommand;
import io.github.shaksternano.mediamanipulator.listener.CommandListener;
import io.github.shaksternano.mediamanipulator.command.CommandRegistry;
import io.github.shaksternano.mediamanipulator.command.HelpCommand;
import io.github.shaksternano.mediamanipulator.mediamanipulation.ImageManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulation.MediaManipulatorRegistry;
import io.github.shaksternano.mediamanipulator.util.GraphicsUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class Main {

    public static final Logger LOGGER = LoggerFactory.getLogger("Media Manipulator");

    public static void main(String[] args) {
        String token = null;

        try {
            token = getToken(args);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Please provide a token!");
            System.exit(1);
        }

        JDA jda = null;

        try {
            jda = JDABuilder.createDefault(token).build();
        } catch (LoginException e) {
            LOGGER.error("Invalid token!");
            System.exit(1);
        }

        jda.getPresence().setActivity(Activity.playing("gaming"));
        jda.addEventListener(new CommandListener());
        jda.updateCommands()
                .addCommands(Commands.slash(HelpCommand.INSTANCE.getName(), "Lists all commands"))
                .queue();

        registerCommands();
        GraphicsUtil.registerFonts();
        registerMediaManipulators();
    }

    private static String getToken(String[] args) {
        if (args.length > 0) {
            return args[0];
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static void registerCommands() {
        CommandRegistry.INSTANCE.register(
                HelpCommand.INSTANCE,
                CaptionCommand.INSTANCE
        );
    }

    private static void registerMediaManipulators() {
        MediaManipulatorRegistry.register(ImageManipulator.INSTANCE,
                "png",
                "jpg",
                "jpeg"
        );
    }
}
