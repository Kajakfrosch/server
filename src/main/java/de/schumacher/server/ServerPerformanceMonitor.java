package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ServerPerformanceMonitor {
    private static double tpsThreshold = 19.0; // Standardwert
    private static double memoryThreshold = 0.8; // Standardwert
    private static int chunkThreshold = 2000; // Standardwert

    // Initialisiere Werte aus der Config
    public static void initialize(FileConfiguration config) {
        // Lade Werte aus der Konfiguration
        tpsThreshold = config.getDouble("performance.tps_threshold", 19.0);
        memoryThreshold = config.getDouble("performance.memory_threshold", 0.8);
        chunkThreshold = config.getInt("performance.chunk_threshold", 2000);
    }
    public static boolean isServerOverloaded() {
        // Prüfe TPS
        if (isTpsOverloaded()) {
            return true;
        }

        // Prüfe Speicher
        if (isMemoryOverloaded()) {
            return true;
        }

        // Prüfe geladene Chunks
        if (isChunkLoadOverloaded()) {
            return true;
        }

        // Wenn keine der Bedingungen erfüllt ist, ist der Server nicht überlastet
        return false;
    }

    protected static boolean isTpsOverloaded() {
        double[] recentTps = getTPS();
        return recentTps[0] < tpsThreshold; // Nutze den dynamischen Schwellenwert
    }

    protected static boolean isMemoryOverloaded() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        return ((double) usedMemory / maxMemory > memoryThreshold); // Memory usage above 80% // Dynamischer Schwellenwert
    }

    protected static boolean isChunkLoadOverloaded() {
        int totalLoadedChunks = 0;
        for (World world : Bukkit.getWorlds()) {
            totalLoadedChunks += world.getLoadedChunks().length;
        }
        return totalLoadedChunks > chunkThreshold; // More than 5000 loaded chunks
    }

    protected static double[] getTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            return (double[]) server.getClass().getField("recentTps").get(server);
        } catch (Exception e) {
            e.printStackTrace();
            return new double[0];
        }
    }
}