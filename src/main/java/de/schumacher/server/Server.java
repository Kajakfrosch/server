package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Main class for the Server plugin implementation.
 * This class manages the lifecycle of the plugin, initializes components, and handles commands and events.
 * It also provides an embedded Tomcat server and map generation functionality.
 *
 * The plugin is built for a Minecraft Spigot server and handles features such as server performance monitoring,
 * database management, generated maps, and player export capabilities.
 *
 * The server plugin automatically initializes required components, manages configurations,
 * registers command executors, and handles the lifecycle events during enabling and disabling states.
 *
 * Functionalities include:
 * - Map generation via the {@code MapGenerator}.
 * - Debugging through the {@code DebugLogger}.
 * - Player export management.
 * - Command handling for backup, invasion, and server status.
 * - Embedded Tomcat server management.
 * - Handling the `/dynmap` command for generating or re-rendering maps.
 */
public final class Server extends JavaPlugin {

    private MapGenerator mapGenerator;
    private DebugLogger debugLogger;
    private ServerPerformanceMonitor serverPerformanceMonitor;
    private PlayerPositionExporter playerPositionExporter;
    private InvasionCommand invasionCommand;
    private ServerStatusCommand serverStatusCommand;
    private BackupCommand backupCommand;
    private Thread tomcatThread;
    private EmbeddedTomcatServer tomcatServer;
    private DatabaseManager databaseManager;
    @Override
    public void onEnable() {
        // Standard-Konfiguration laden, falls nicht vorhanden
        this.saveDefaultConfig();
        // Ordner des Plugins
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // SQLite-Datenbank verbinden
        databaseManager = new DatabaseManager(dataFolder);
        databaseManager.connect();
        // Initialisiere den MapGenerator
        mapGenerator = new MapGenerator(this,databaseManager);
        // Initialisiere Performance-Monitor mit Config
        ServerPerformanceMonitor.initialize(this.getConfig());
        extractTemplateIfNotExists();
        // Initialisiere Debugging
        DebugLogger.initialize(this.getConfig());
        getCommand("backup").setExecutor(new BackupCommand(this));
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
        //ini Tomcat
        // Konfiguration für den Embedded Tomcat
        boolean isTomcatEnabled = this.getConfig().getBoolean("tomcat.enabled", false);
        int httpPort = this.getConfig().getInt("tomcat.http_port", 8080);
        int httpsPort = this.getConfig().getInt("tomcat.https_port", 8443);

        // Warnung: Embedded Tomcat ist nicht für Produktionsumgebungen geeignet
        if (isTomcatEnabled) {
            System.err.println("WARNUNG: Der eingebettete Tomcat-Server ist nicht für den Produktionsbetrieb geeignet!");
            System.err.println("Bitte verwenden Sie einen externen Tomcat, Nginx oder Apache HTTPD für Ihre Anwendung.");
            // Tomcat starten
            tomcatServer = new EmbeddedTomcatServer(httpPort, this.getDataFolder().getAbsolutePath().replace("\\", "/") + "/work");
            tomcatThread = new Thread(() -> {
                try {
                    tomcatServer.start();
                } catch (Exception e) {
                    System.err.println("Fehler beim Start des eingebetteten Tomcat-Servers:");
                    e.printStackTrace();
                }
            });

            tomcatThread.setDaemon(true);
            tomcatThread.start();
            System.out.println("Tomcat wird im Hintergrund gestartet...");
        } else {
            System.out.println("Tomcat-Server ist deaktiviert. Start wird übersprungen.");
        }

    }

    @Override
    public void onDisable() {

        // Stoppe den Tomcat-Server
        if (tomcatServer != null) {
            tomcatServer.stop();
        }

        // Warte darauf, dass der Thread beendet wird
        if (tomcatThread != null && tomcatThread.isAlive()) {
            tomcatThread.interrupt();
            try {
                tomcatThread.join();
            } catch (InterruptedException e) {
                System.err.println("Fehler beim Warten auf das Beenden des Tomcat-Threads:");
                e.printStackTrace();
            }
        }

        getLogger().info("Server Plugin deaktiviert!");

    }


    /**
     * Executes the specified command when it is triggered by a sender. This method processes the "dynmap"
     * command to generate a map asynchronously.
     *
     * @param sender The entity (e.g., player, console) that issued the command.
     * @param command The command that was executed.
     * @param label The alias of the command that was used.
     * @param args The arguments passed along with the command.
     * @return true if the command was successfully executed and should not be passed to other handlers;
     *         false if the command is not recognized or should be handled elsewhere.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dynmap")) {
            // Nur Spieler mit Operator-Berechtigungen dürfen den Befehl ausführen
            if (sender instanceof Player && !sender.isOp()) {
                sender.sendMessage("Du hast keine Berechtigung, diesen Befehl auszuführen!");
                return true;
            }

            // Kein Argument - Hinweis ausgeben
            if (args.length == 0) {
                sender.sendMessage("Verwendung: /dynmap [fullrender|rerender|render]");
                return true;
            }

            // Argumente auswerten
            String action = args[0].toLowerCase();
            if (action.equals("render")) {
                sender.sendMessage("Starte vollständiges Rendern der Karte...");
                sender.sendMessage("Dieser Vorgang kann einige Zeit dauern, bitte warten...");
                sender.sendMessage("Es kann zu Laggs führen.");

                // Async-Task für fullrender
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    mapGenerator.generateMap(); // Karte vollständig rendern
                    sender.sendMessage("Vollständiges Rendern abgeschlossen!");
                });
                return true;

            }
            if (action.equals("fullrender")) {
                sender.sendMessage("Starte vollständiges Rendern der Karte...");
                sender.sendMessage("Dieser Vorgang kann einige Zeit dauern, bitte warten...");
                sender.sendMessage("Es kann zu Laggs führen.");

                // Async-Task für fullrender
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    mapGenerator.generateMapFull(); // Karte vollständig rendern
                    sender.sendMessage("Vollständiges Rendern abgeschlossen!");
                });
                return true;

            } else if (action.equals("rerender")) {
                sender.sendMessage("Setze die Datenbank zurück und starte erneutes Rendern...");
                sender.sendMessage("Dieser Vorgang wird alle bisherigen Daten löschen!");

                // Async-Task für rerender
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    mapGenerator.rebuildDatabaseAndRerender(); // Datenbank löschen und neu rendern
                    sender.sendMessage("Erneutes Rendern abgeschlossen!");
                });
                return true;

            } else {
                // Unbekannter Befehl
                sender.sendMessage("Unbekannter Befehl: " + action);
                sender.sendMessage("Verwendung: /dynmap [fullrender|rerender]");
                return true;
            }
        }
        return false;
    }
    public void extractTemplateIfNotExists() {
        // Ziel-Ordner, wo die Templates hinkopiert werden sollen
        File workFolder = new File(this.getDataFolder(), "templates");
        if (!workFolder.exists()) {
            workFolder.mkdirs();
        }

        // Ziel-Datei (index.html im Arbeitsordner)
        File targetFile = new File(workFolder, "index.html");

        // Template-Pfad innerhalb der .jar (aus resources/templates)
        String templatePath = "/templates/index.html";

        if (!targetFile.exists()) {
            try (InputStream input = getClass().getResourceAsStream(templatePath)) {
                if (input == null) {
                    this.getLogger().severe("Template konnte nicht aus der .jar geladen werden: " + templatePath);
                    return;
                }

                // Kopiere Template in das Arbeitsverzeichnis
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                this.getLogger().info("Template wurde erfolgreich in den Arbeitsordner kopiert: " + targetFile.getAbsolutePath());
            } catch (IOException e) {
                this.getLogger().severe("Fehler beim Extrahieren des Templates: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            this.getLogger().info("Template existiert bereits: " + targetFile.getAbsolutePath());
        }
    }



}