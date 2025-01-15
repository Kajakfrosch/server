package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The PlayerPositionExporter class provides functionality to export the positions
 * of all online players in the server to a specified output file.
 *
 * This is useful for logging, debugging, or external processing of player locations.
 * Each player's data is written in the format:
 * playerName:x-coordinate:y-coordinate:z-coordinate
 *
 * The file will be overwritten if it already exists.
 *
 * Note: This class includes basic exception handling for IO operations
 * and logs any encountered errors to the console.
 */
public class PlayerPositionExporter {
    public static void exportPlayerPositions(File outputFile) {
        try (FileWriter writer = new FileWriter(String.valueOf(outputFile))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String line = String.format("%s:%f:%f:%f\n", player.getName(),
                        player.getLocation().getX(),
                        player.getLocation().getY(),
                        player.getLocation().getZ());
                writer.write(line);
            }
            System.out.println("Spielerpositionen wurden in " + outputFile.getAbsolutePath() + " exportiert!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}