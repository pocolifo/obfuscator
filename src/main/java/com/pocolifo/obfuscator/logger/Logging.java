package com.pocolifo.obfuscator.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logging {
    public static boolean enable = true;

    // thanks to https://stackoverflow.com/a/5762502
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    private static String getLogPrefix() {
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

        return String.format("[%s] [%s]", date, Thread.currentThread().getName());
    }

    public static void info(String string, Object... objects) {
        if (enable) System.out.printf(ANSI_RESET + "%s [INFO] %s%s%n", getLogPrefix(), String.format(string, objects), ANSI_RESET);
    }

    public static void warn(String string, Object... objects) {
        if (enable) System.err.printf(ANSI_YELLOW + "%s [WARN] %s%s%n", getLogPrefix(), String.format(string, objects), ANSI_RESET);
    }

    public static void err(String string, Object... objects) {
        if (enable) System.err.printf(ANSI_RED + "%s [ERROR] %s%s%n", getLogPrefix(), String.format(string, objects), ANSI_RESET);
    }

    public static void fatal(String string, Object... objects) {
        if (enable) System.err.printf(ANSI_RED + "%s [FATAL] %s%s%n", getLogPrefix(), String.format(string, objects), ANSI_RESET);
        System.exit(-1);
    }
}
