package de.schumacher.server;

import de.schumacher.server.Server;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InvasionCommand implements CommandExecutor {

    private final Server plugin;
    private final Random random = new Random();

    private int defaultDistance = 10;
    private int maxDifficulty = 20;
    private int spawnRate = 2;
    private List<Reward> rewards;

    // Speichert aktive Invasionen pro Spieler
    private final HashMap<Player, Boolean> activeInvasions = new HashMap<>();

    public InvasionCommand(Server plugin) {
        this.plugin = plugin;
        loadConfigValues(plugin.getConfig()); // Lade Config-Werte
        plugin.getCommand("invasion").setExecutor(this); // Registriere den Command
    }
    private void loadConfigValues(FileConfiguration config) {
        // Invasionseinstellungen laden
        defaultDistance = config.getInt("invasion.default_distance", 10);
        maxDifficulty = config.getInt("invasion.max_difficulty", 20);
        spawnRate = config.getInt("invasion.zombie_spawn_rate", 2);

        // Belohnungen laden
        rewards = config.getMapList("invasion.rewards").stream()
                .map(Reward::new) // Mapping auf Reward-Objekte (siehe unten)
                .toList();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Nur Spieler können diesen Befehl ausführen!");
            return true;
        }

        // /invasion stop
        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            try {
                stopAllInvasions(player);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        // /invasion start <Abstand> <Schwierigkeit>
        if (args.length != 3 || !args[0].equals("start")) {
            sender.sendMessage(ChatColor.RED + "Verwendung: /invasion start <Abstand> <Schwierigkeit> oder /invasion stop");
            return true;
        }

        try {
            int distance = Integer.parseInt(args[1]);
            int difficulty = Integer.parseInt(args[2]);

            if (difficulty < 1 || difficulty > 20) {
                player.sendMessage(ChatColor.RED + "Die Schwierigkeit muss zwischen 1 und 20 liegen!");
                return true;
            }

            startInvasion(player, distance, difficulty);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Der Abstand und die Schwierigkeit müssen Ganzzahlen sein!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private void stopAllInvasions(Player initiator) throws InterruptedException {
        // Abbrechen aller aktiven Invasionen
        for (Player player : activeInvasions.keySet()) {
            if (activeInvasions.get(player)) { // Wenn eine Invasion aktiv ist
                activeInvasions.put(player, false); // Setze den Status zurück
                initiator.sendMessage(ChatColor.RED + "Invasion von " + player.getName() + " wurde gestoppt.");
                player.sendMessage(ChatColor.RED + "Deine Invasion wurde vom Server-Administrator gestoppt.");
                Difficulty difficulty = Bukkit.getServer().getWorld("world").getDifficulty();
                Bukkit.getServer().getWorld("world").setDifficulty(Difficulty.PEACEFUL);
                Thread.sleep(5000);
                Bukkit.getServer().getWorld("world").setDifficulty(difficulty);
                Bukkit.getServer().getWorld("world").setTime(1000);

            }
        }

        // Nachrichten für den Spieler
        initiator.sendMessage(ChatColor.GREEN + "Alle aktiven Invasionen wurden gestoppt.");
    }

    private void startInvasion(Player player, int distance, int difficulty) throws InterruptedException {
        if (activeInvasions.getOrDefault(player, false)) {
            player.sendMessage(ChatColor.RED + "Es läuft bereits eine Invasion!");
            return;
        }
        if (distance <= 0) {
            distance = defaultDistance; // Fallback auf Standardwert
        }

        if (difficulty > maxDifficulty) {
            player.sendMessage(ChatColor.RED + "Die Schwierigkeit darf maximal " + maxDifficulty + " betragen!");
            return;
        }
        Difficulty difficult = Bukkit.getServer().getWorld("world").getDifficulty();
        Bukkit.getServer().getWorld("world").setDifficulty(Difficulty.PEACEFUL);
        Thread.sleep(5000);
        Bukkit.getServer().getWorld("world").setDifficulty(difficult);
        Bukkit.getServer().getWorld("world").setTime(14000);
        activeInvasions.put(player, true); // Markiere Invasion als aktiv

        // Zielpunkt setzen (Punkt, den Spieler verteidigen muss)
        Location targetLocation = player.getLocation();

        player.sendMessage(ChatColor.GREEN + "Die Invasion beginnt! Zombies kommen aus " + distance + " Blöcken Entfernung. Schwierigkeit: " + difficulty);

        // Spawne Zombies in regelmäßigen Abständen
        int finalDistance = distance;
        new BukkitRunnable() {
            int tickCount = 0;
            int maxTicks = difficulty * 30; // Dynamische Dauer basierend auf Schwierigkeit

            @Override
            public void run() {
                if (tickCount >= maxTicks || !player.isOnline()) {
                    this.cancel();
                    if (player.isOnline()) {
                        player.sendMessage(ChatColor.GREEN + "Die Invasion wurde erfolgreich abgewehrt! Du erhältst eine Belohnung.");
                        giveRewards(player, difficulty);
                    }
                    activeInvasions.put(player, false);
                    return;
                }

                // Spawne Zombie in der Nähe des Zielpunkts (im Abstand)
                spawnZombieNear(targetLocation, finalDistance);

                tickCount += 20; // 20 Ticks = 1 Sekunde
            }
        }.runTaskTimer(plugin, 0, 20); // 1-Zweiten-Takt
    }

    private void spawnZombieNear(Location target, int distance) {
        // Zufällige Position in der Nähe des Zielpunkts
        double angle = random.nextDouble() * 2 * Math.PI; // Zufälliger Winkel
        double x = target.getX() + distance * Math.cos(angle);
        double z = target.getZ() + distance * Math.sin(angle);
        Location spawnLocation = new Location(target.getWorld(), x, target.getY(), z);

        // Spawne Zombie
        Zombie zombie = (Zombie) target.getWorld().spawnEntity(spawnLocation, EntityType.ZOMBIE);
        zombie.setCustomName(ChatColor.RED + "Invasions-Zombie");
        zombie.setCustomNameVisible(true);

        // Verstärkt den Zombie basierend auf der Schwierigkeit
        int health = 20;// Grundgesundheit des Zombies
        zombie.setHealth(health);
        // Das Ziel der Zombies setzen (Spieler oder Entität)
        if (target.getWorld() != null) {
            Player nearestPlayer = getNearestPlayer(target);
            if (nearestPlayer != null) {
                zombie.setTarget(nearestPlayer); // Verfolgt den nächstgelegenen Spieler
            }
        }
    }

    // Hilfsmethode: Finde den nächsten Spieler
    private Player getNearestPlayer(Location location) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private void giveRewards(Player player, int difficulty) {
        // Dynamische Belohnung basierend auf Schwierigkeit
        for (Reward reward : rewards) {
            if (random.nextInt(100) < reward.getChance()) {
                ItemStack item = new ItemStack(Material.valueOf(reward.getMaterial()), random.nextInt(reward.getMax() - reward.getMin() + 1) + reward.getMin());
                player.getInventory().addItem(item);
                player.sendMessage(ChatColor.GOLD + "Du hast " + item.getAmount() + " " + item.getType() + " erhalten!");
            }
        }
    }

    private void punishFailure(Player player) {
        player.sendMessage(ChatColor.RED + "Die Invasion wurde verloren! Du verlierst etwas Erfahrung.");
        player.setExp(Math.max(0, player.getExp() - 0.1f)); // Spieler verlieren 10% ihrer aktuellen EXP
    }
    private static class Reward {
        private final String material;
        private final int min;
        private final int max;
        private final int chance;

        public Reward(Map<?, ?> map) {
            this.material = (String) map.get("material");
            this.min = (int) map.get("min");
            this.max = (int) map.get("max");
            this.chance = (int) map.get("chance");
        }

        public String getMaterial() {
            return material;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public int getChance() {
            return chance;
        }
    }

}