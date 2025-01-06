package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The DebugLogger class is used for managing and controlling logging operations within the application.
 * It allows configurable debug and log levels through an external configuration file.
 * Logs are outputted to the server console depending on the current log level.
 */
public class DebugLogger {

    private static boolean enabled = false; // Standardwert
    private static String logLevel = "INFO"; // Standardlogstufe

    public static void initialize(FileConfiguration config) {
        enabled = config.getBoolean("debug.enabled", false);
        logLevel = config.getString("debug.log_level", "INFO");
    }

    public static void log(String level, String message) {
        if (!enabled) return; // Logging deaktiviert

        switch (logLevel) {
            case "DEBUG" -> Bukkit.getLogger().info("[DEBUG] " + message);
            case "INFO" -> {
                if (!level.equalsIgnoreCase("DEBUG")) {
                    Bukkit.getLogger().info("[INFO] " + message);
                }
            }
            case "WARN" -> {
                if (level.equalsIgnoreCase("WARN") || level.equalsIgnoreCase("ERROR")) {
                    Bukkit.getLogger().warning("[WARN] " + message);
                }
            }
            case "ERROR" -> {
                if (level.equalsIgnoreCase("ERROR")) {
                    Bukkit.getLogger().severe("[ERROR] " + message);
                }
            }
        }
    }
}