package io.github.shaksternano.mediamanipulator.command.terminal;

import io.github.shaksternano.mediamanipulator.Main;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * For taking input from the terminal.
 */
public class TerminalInputListener implements Runnable {

    /**
     * Whether the thread should be running.
     */
    private boolean running = true;

    /**
     * If "!shutdown! is entered, the program will be terminated.
     */
    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                String command = scanner.nextLine();

                if (command.equals("!shutdown")) {
                    Main.LOGGER.info("Shutting down!");
                    running = false;
                    Main.shutdown();
                }
            }
        } catch (NoSuchElementException e) {
            running = false;
            Main.LOGGER.error("Unable to get terminal input!", e);
        }
    }
}
