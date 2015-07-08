package com.intellectualsites.web.object;

/**
 * These are the logging modes
 */
public class LogModes {

    /**
     * Send debug data
     */
    public static final int MODE_DEBUG = -1;

    /**
     * Send an informational message
     */
    public static final int MODE_INFO = 1;

    /**
     * Send a warning message
     */
    public static final int MODE_WARNING = 2;

    /**
     * Send an error message
     */
    public static final int MODE_ERROR = 3;

    public static int lowestLevel = MODE_DEBUG;
    public static int highestLevel = MODE_ERROR;
}
