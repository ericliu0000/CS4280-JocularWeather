package org.nothing.jocularweather;

/**
 * Helper class to log program operation information.
 *
 * @author Ganning Xu
 * @author Eric Liu
 */
public class Logger {
    /**
     * Character to set text color to default color.
     */
    public static final String ANSI_RESET = "\u001B[0m";
    /**
     * Character to set text color to red.
     */
    public static final String ANSI_RED = "\u001B[31m";
    /**
     * Character to set text color to green.
     */
    public static final String ANSI_GREEN = "\u001B[32m";
    /**
     * Character to set text color to yellow.
     */
    public static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * Prints out text with specified message type.
     *
     * @param type type of message as specified in {@link MessageType}
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