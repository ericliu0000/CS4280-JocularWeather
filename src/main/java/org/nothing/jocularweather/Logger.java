package org.nothing.jocularweather;

public class Logger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * Prints out text with specified message type.
     *
     * @param type type of message as specified in {@code MessageType}
     * @param text text to output to console
     */
    public static void print(MessageType type, String text) {
        String typeColor = switch (type) {
            case JW_INFO -> ANSI_GREEN;
            case JW_WARN -> ANSI_YELLOW;
            case JW_ERROR -> ANSI_RED;
        };

        System.out.println("[" + typeColor + type + ANSI_RESET + "] " + text);
    }
}