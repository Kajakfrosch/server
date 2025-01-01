package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Server extends JavaPlugin {

    private MapGenerator mapGenerator;

    @Override
    public void onEnable() {
        // Standard-Konfiguration laden, falls nicht vorhanden
        this.saveDefaultConfig();
        // Initialisiere den MapGenerator
        mapGenerator = new MapGenerator(this);
        // Initialisiere Performance-Monitor mit Config
        ServerPerformanceMonitor.initialize(this.getConfig());
        // Initialisiere Debugging
        DebugLogger.initialize(this.getConfig());

        // Beispiel-Nutzung
        DebugLogger.log("INFO", "Server Plugin wird aktiviert...");
        DebugLogger.log("DEBUG", "Dies ist eine Debug-Nachricht.");
        // Registriere die Event-Listener
        getServer().getPluginManager().registerEvents(new ServerEventListener(mapGenerator,this), this);
        getCommand("invasion").setExecutor(new InvasionCommand(this));
        this.getCommand("serverstatus").setExecutor(new ServerStatusCommand());
        // Generiere die Karte beim Start des Plugins
        getLogger().info("Server Plugin aktiviert!");
        mapGenerator.generateMap();

    }

    @Override
    public void onDisable() {
        getLogger().info("Server Plugin deaktiviert!");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dynmap")) {
            if (sender instanceof Player && !sender.isOp()) {
                sender.sendMessage("Du hast keine Berechtigung, diesen Befehl auszuführen!");
                return true;
            }

            sender.sendMessage("Karte wird erstellt...");
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                mapGenerator.generateMap();
                sender.sendMessage("Karte erfolgreich erstellt!");
            });

            return true;
        }
        return false;
    }



}