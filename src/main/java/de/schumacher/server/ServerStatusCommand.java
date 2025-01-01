package de.schumacher.server;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class ServerStatusCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Abfrage des Serverzustands
        boolean overloaded = ServerPerformanceMonitor.isServerOverloaded();

        // Abrufen von Einzelheiten
        double[] tps = ServerPerformanceMonitor.getTPS();
        boolean memoryOverloaded = ServerPerformanceMonitor.isMemoryOverloaded();
        boolean chunkOverloaded = ServerPerformanceMonitor.isChunkLoadOverloaded();

        // Nachricht an den Spieler/Absender
        sender.sendMessage(ChatColor.GOLD + "--- Serverstatus ---");

        // TPS-Status
        sender.sendMessage(ChatColor.YELLOW + "Aktuelle TPS (1 Min): " 
                + ChatColor.AQUA + String.format("%.2f", tps[0])
                + statusSymbol(tps[0] < 18.0)); // Warnung bei niedrigen TPS

        // Speicher-Status
        sender.sendMessage(ChatColor.YELLOW + "Speicherstatus: "
                + (memoryOverloaded ? ChatColor.RED + "Überlastet!" : ChatColor.GREEN + "Normal"));

        // Chunk-Status
        sender.sendMessage(ChatColor.YELLOW + "Chunkstatus: "
                + (chunkOverloaded ? ChatColor.RED + "Zu viele Chunks!" : ChatColor.GREEN + "Normal"));

        // Gesamter Zustand
        if (overloaded) {
            sender.sendMessage(ChatColor.RED + "Warnung: Der Server ist überlastet!");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Alles in Ordnung. Der Server läuft stabil!");
        }
        return true;
    }

    // Hilfsmethode für Warnsymbole
    private String statusSymbol(boolean condition) {
        return condition ? ChatColor.RED + " (WARNUNG)" : ChatColor.GREEN + " (OK)";
    }
}