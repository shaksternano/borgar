package io.github.shaksternano.mediamanipulator.command.terminal;

import io.github.shaksternano.mediamanipulator.Main;
import net.dv8tion.jda.api.JDA;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class TerminalInputListener implements Runnable {

    private boolean running = true;
    private final JDA jda;

    public TerminalInputListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                String command = scanner.nextLine();

                if (command.equals("!shutdown")) {
                    running = false;
                    jda.shutdownNow();
                    System.exit(0);
                }
            }
        } catch (NoSuchElementException e) {
            running = false;
            Main.LOGGER.error("Unable to get terminal input!", e);
        }
    }
}
