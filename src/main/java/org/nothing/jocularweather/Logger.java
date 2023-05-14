package org.nothing.jocularweather;

public class Logger {

    // Constants for ANSI color codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    /**
     * Handles types of messages in program console output.
     */
    enum MessageType {
        /**
         * Informational output; corresponds to green text.
         */
        JW_INFO,
        /**
         * Warning output: corresponds to amber text.
         */
        JW_WARN,
        /**
         * Output to indicate program error: corresponds to red text.
         */
        JW_ERROR
    }

    public static void print(MessageType type, String text) {
        String typeColor;

        switch (type) {
            case JW_INFO:
                typeColor = ANSI_GREEN;
                break;
            case JW_WARN:
                typeColor = ANSI_YELLOW;
                break;
            case JW_ERROR:
                typeColor = ANSI_RED;
                break;
            default:
                typeColor = ANSI_RESET;
        }

        System.out.print("[" + typeColor + type + ANSI_RESET + "]");
        System.out.println(" " + text);
    }
}