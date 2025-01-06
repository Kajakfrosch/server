package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class ServerEventListener implements Listener {

    private final MapGenerator mapGenerator;
    private final Server plugin;

    public ServerEventListener(MapGenerator mapGenerator, Server plugin) {
        this.mapGenerator = mapGenerator;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(ChatColor.GOLD +"Willkommen auf dem Server! " + event.getPlayer().getName());
        // MOTD aus der Config lesen
        String motd = plugin.getConfig().getString("MOTD");
        String instanz = plugin.getConfig().getString("Instanz");
        String knoten = plugin.getConfig().getString("Knoten");
        String webseite = plugin.getConfig().getString("Webseite");

        // Nachricht an den Spieler senden
        //event.getPlayer().sendMessage(ChatColor.GOLD + motd);
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Instanz: " + ChatColor.AQUA + instanz);
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Knoten: " + ChatColor.AQUA + knoten);
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Live-Map: " + ChatColor.AQUA + webseite);

        if (ServerPerformanceMonitor.isServerOverloaded()) {
         //   Bukkit.broadcastMessage(ChatColor.RED + "Warnung: Der Server ist überlastet!");
            Bukkit.getLogger().info(ChatColor.RED + "Warnung: Der Server ist überlastet!");
        }else {mapGenerator.reloadMapandHTML();
            //mapGenerator.generateMap();
            }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ServerPerformanceMonitor.isServerOverloaded()) {
         //   Bukkit.broadcastMessage(ChatColor.RED + "Warnung: Der Server ist überlastet!");
            Bukkit.getLogger().info(ChatColor.RED + "Warnung: Der Server ist überlastet!");
        }else {mapGenerator.reloadMapandHTML();}
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        if (ServerPerformanceMonitor.isServerOverloaded()) {
           // Bukkit.broadcastMessage(ChatColor.RED + "Warnung: Der Server ist überlastet!");
            Bukkit.getLogger().info(ChatColor.RED + "Warnung: Der Server ist überlastet!");
        }else {mapGenerator.generateMap();}
    }
    @EventHandler
    public void ChunkLoadEvent(WorldSaveEvent event){
        if (ServerPerformanceMonitor.isServerOverloaded()) {
           // Bukkit.broadcastMessage(ChatColor.RED + "Warnung: Der Server ist überlastet!");
            Bukkit.getLogger().info(ChatColor.RED + "Warnung: Der Server ist überlastet!");
        }else {mapGenerator.generateMap();        }

    }
}