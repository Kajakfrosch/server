package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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