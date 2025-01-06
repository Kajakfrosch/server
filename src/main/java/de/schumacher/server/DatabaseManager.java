/*
 * Copyright (c) 2025. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package de.schumacher.server;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DatabaseManager class is responsible for managing the connection to
 * an SQLite database for a Minecraft plugin. It provides methods to establish
 * and terminate a database connection, as well as access the active connection.
 */
public class DatabaseManager {

    private Connection connection;
    private final String databasePath;

    public DatabaseManager(File dataFolder) {
        // Der Pfad zur SQLite-Datenbank
        this.databasePath = new File(dataFolder, "database.db").getAbsolutePath();
    }

    // Verbindung zur Datenbank herstellen
    public void connect() {
        try {
            // SQLite-Verbindung
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            Bukkit.getLogger().info("SQLite-Datenbank erfolgreich verbunden!");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Fehler beim Verbinden mit der SQLite-Datenbank: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Bukkit.getLogger().info("SQLite-Datenbank erfolgreich geschlossen!");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Fehler beim Schließen der SQLite-Datenbank: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }
}