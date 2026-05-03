package util;

/**
 * GlobalSettings.java
 * Central place for system-wide constants.
 * Change LOW_STOCK_THRESHOLD here to affect the entire system.
 */
public class GlobalSettings {

    // Low-stock alert fires when stock falls AT OR BELOW this number
    public static final int LOW_STOCK_THRESHOLD = 5;

    // Prevent instantiation
    private GlobalSettings() {}
}