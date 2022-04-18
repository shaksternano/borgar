package io.github.shaksternano.mediamanipulator.util;

import net.dv8tion.jda.api.JDA;

import java.util.Scanner;

public class TerminalInputListener implements Runnable {

    private boolean running = true;
    private final JDA jda;

    public TerminalInputListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void run() {
        while (running) {
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

            if (command.equals("!shutdown")) {
                running = false;
                jda.shutdownNow();
                System.exit(0);
            }
        }
    }
}
