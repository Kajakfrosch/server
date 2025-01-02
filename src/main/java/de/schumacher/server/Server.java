package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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