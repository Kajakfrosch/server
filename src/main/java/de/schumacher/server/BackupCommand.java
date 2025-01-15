package de.schumacher.server;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * The BackupCommand class implements the functionality for creating a backup
 * of the Minecraft server's world and relevant files. This command is triggered
 * by server operators and facilitates generating a compressed archive of the
 * server data.
 *
 * The backup includes copying the server's main directory, creating a temporary
 * copy of files, compressing them into a .tar.gz archive, and handling temporary
 * resources cleanup. It also ensures proper server save states during the
 * backup process.
 *
 * Constructor:
 * - Takes in an instance of {@link JavaPlugin} for accessing server configurations
 *   and world data.
 *
 * The `onCommand` method:
 * - Verifies the sender's permissions (OP status).
 * - Retrieves the backup directory path from the plugin configuration.
 * - Creates a timestamped .tar.gz backup file.
 * - Copies the server data to a temporary directory.
 * - Archives the data to the target file.
 * - Deletes temporary files after completion.
 * - Sends appropriate feedback to the command sender about success or failure.
 *
 * Private Utility Methods:
 * - `copyDirectory`: Recursively copies files and directories from the server
 *   directory to a temporary directory.
 * - `createTarGz`: Compresses the temporary backup data into a .tar.gz file and
 *   manages temporary server save commands.
 * - `addFilesToTarGz`: Populates the tar.gz archive with files and directories
 *   recursively.
 *
 * The backup path is loaded from the plugin's configuration under the key
 * `backup.path`. If the directory does not exist, it will attempt to create
 * the required folders.
 *
 * Errors during the backup process are caught and reported to the sender.
 */
public class BackupCommand implements CommandExecutor {

    private final JavaPlugin plugin; // Referenz zum Plugin für Zugriff auf Serverdaten und Config

    public BackupCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) { // Berechtigungsprüfung
            sender.sendMessage("§cDu hast keine Berechtigung, diesen Befehl auszuführen!");
            return true;
        }

        sender.sendMessage("§aBackup wird erstellt, bitte warten...");

        // Konfiguration laden: Backup-Pfad
        String backupPath = plugin.getConfig().getString("backup.path", "/opt/sicherungen");

        // Zeitstempel für den Dateinamen
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File backupFile = new File(backupPath, timestamp + "-Backup-Minecraft.tar.gz");

        try {
            // Sicherungsordner erstellen, falls nicht vorhanden
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    sender.sendMessage("§cFehler: Konnte Sicherungsordner nicht erstellen!");
                    return true;
                }
            }

            // Serververzeichnis abrufen (Minecraft-Root)
            File serverDir = new File(plugin.getServer().getWorldContainer().getAbsolutePath());
            File tempDir = new File("temp_backup");

            // Erstellt eine Kopie des Ordners
            copyDirectory(serverDir, tempDir);

            // Erstelle Backup von der Kopie
            createTarGz(tempDir, backupFile);

            // Temporäre Dateien löschen
            deleteDirectory(tempDir);

            sender.sendMessage("§aBackup erfolgreich erstellt: " + backupFile.getAbsolutePath());
        } catch (Exception e) {
            sender.sendMessage("§cEin Fehler ist beim Erstellen des Backups aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }

        for (File file : source.listFiles()) {
            File targetFile = new File(target, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, targetFile); // Rekursives Kopieren für Unterordner
            } else {
                try (InputStream in = new FileInputStream(file);
                     OutputStream out = new FileOutputStream(targetFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }
            }
        }
    }
    private void createTarGz(File sourceDir, File tarGzFile) throws IOException {
        // Speichern der Daten auf die Festplatte
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-all");
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-off");
        try (FileOutputStream fos = new FileOutputStream(tarGzFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GZIPOutputStream gos = new GZIPOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gos)) {

            // Normale Datei-Blockgröße setzen, um korrekte Tar-Kompatibilität sicherzustellen
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            // Dateien und Ordner in das tar-Archiv schreiben
            addFilesToTarGz(sourceDir, "", tos);
        }finally {
            // Automatisches Speichern aktivieren
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "save-on");
        }
    }

    private void addFilesToTarGz(File file, String parent, TarArchiveOutputStream tos) throws IOException {
        String entryName = parent + file.getName();
        TarArchiveEntry entry = new TarArchiveEntry(file, entryName);

        tos.putArchiveEntry(entry);

        if (file.isFile()) {
            // Datei-Content schreiben
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    tos.write(buffer, 0, length);
                }
            }
            tos.closeArchiveEntry();
        } else if (file.isDirectory()) {
            tos.closeArchiveEntry();
            // Rekursiv alle Dateien im Verzeichnis hinzufügen
            for (File child : file.listFiles()) {
                addFilesToTarGz(child, entryName + "/", tos);
            }
        }
    }
}