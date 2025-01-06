package de.schumacher.server;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.thymeleaf.context.Context;


import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The MapGenerator class is responsible for generating a dynamic map and its associated HTML output
 * within a Minecraft plugin. It integrates with a provided Server instance and a DatabaseManager
 * to manage data related to chunk and block rendering, as well as player information and map persistence.
 */
public class MapGenerator {

    private final Server plugin;
    private final Map<Material, Color> blockColors;
    private DatabaseManager databaseManager;
    private boolean isRendering = false;
    public MapGenerator(Server plugin,DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;

        // Blockfarben initialisieren
        blockColors = new HashMap<>();
        initializeBlockColors();
        // Initialisiere die Tabellen
        initializeDatabase();
    }
    public void reloadMapandHTML() {
        plugin.getLogger().info("Map wird erneut generiert...");
        generateHTML();
        generateMapFromDatabase();
    }

    private void initializeBlockColors() {
        blockColors.put(Material.GRASS_BLOCK, new Color(34, 139, 34)); // Dunkles Grasgrün
        blockColors.put(Material.SHORT_GRASS, new Color(124, 252, 0)); // Hellgrün
        blockColors.put(Material.DIRT_PATH, new Color(181, 101, 29)); // Erde-ähnliches Braun
        blockColors.put(Material.OAK_LEAVES, new Color(85, 107, 47)); // Dunkles Olivgrün
        blockColors.put(Material.STRIPPED_OAK_WOOD, new Color(194, 178, 128)); // Hellbraun
        blockColors.put(Material.DANDELION, new Color(255, 223, 0)); // Leuchtendes Gelb
        blockColors.put(Material.AZURE_BLUET, new Color(175, 238, 238)); // Hellblau-cyan
        blockColors.put(Material.OAK_STAIRS, new Color(160, 82, 45)); // Mittleres Braun
        blockColors.put(Material.OXEYE_DAISY, new Color(255, 255, 255)); // Weiß
        blockColors.put(Material.OAK_PLANKS, new Color(222, 184, 135)); // Leicht rötliches Holzbraun
        blockColors.put(Material.COBBLESTONE, new Color(128, 128, 128)); // Grau für Stein
        blockColors.put(Material.MOSSY_COBBLESTONE, new Color(107, 142, 35)); // Moosgrün
        blockColors.put(Material.OAK_FENCE, new Color(184, 134, 11)); // Braun-gelb
        blockColors.put(Material.OAK_FENCE_GATE, new Color(184, 134, 11)); // Selbes Gelbbraun
        blockColors.put(Material.POPPY, new Color(255, 0, 0)); // Knallrot
        blockColors.put(Material.TORCH, new Color(255, 165, 0)); // Feuerorange
        blockColors.put(Material.CRAFTING_TABLE, new Color(139, 69, 19)); // Dunkleres Holz
        blockColors.put(Material.POTATOES, new Color(210, 180, 140)); // Erdfarben
        blockColors.put(Material.WHEAT, new Color(255, 223, 0)); // Gelber Farbton
        blockColors.put(Material.COMPOSTER, new Color(160, 82, 45)); // Dunkelbraun
        blockColors.put(Material.WATER, new Color(70, 130, 180)); // Blau
        blockColors.put(Material.AIR, new Color(0, 0, 0, 0)); // Transparent (schwarz mit 0 Alpha)
        blockColors.put(Material.BEETROOTS, new Color(152, 0, 0)); // Tiefrot
        blockColors.put(Material.ACACIA_LEAVES, new Color(100, 149, 85)); // Gräuliches Grün
        blockColors.put(Material.BASALT, new Color(112, 128, 144)); // Dunkleres Grau-Blau
        blockColors.put(Material.OBSIDIAN, new Color(37, 28, 44)); // Fast Schwarz/Lila
        blockColors.put(Material.TALL_GRASS, new Color(34, 139, 34)); // Dunkelgrün
        blockColors.put(Material.STONE, new Color(112, 128, 144)); // Grau
        blockColors.put(Material.CORNFLOWER, new Color(25, 25, 112)); // Dunkelblau
        blockColors.put(Material.WHITE_BANNER, new Color(255, 255, 255)); // Weiß
        blockColors.put(Material.CHEST, new Color(139, 69, 19)); // Holzbraun
        blockColors.put(Material.FURNACE, new Color(169, 169, 169)); // Hellgrau
        blockColors.put(Material.BOOKSHELF, new Color(139, 69, 19)); // Holzbraun
        blockColors.put(Material.ENCHANTING_TABLE, new Color(72, 61, 139)); // Dunkellila
        blockColors.put(Material.BREWING_STAND, new Color(211, 211, 211)); // Hellgrau
        blockColors.put(Material.ACACIA_PLANKS, new Color(205, 92, 92)); // Warm-rotbraun
        blockColors.put(Material.OAK_TRAPDOOR, new Color(184, 134, 11)); // Braun
        blockColors.put(Material.RED_BED, new Color(178, 34, 34)); // Dunkelrot
        blockColors.put(Material.JUNGLE_LOG, new Color(139, 69, 19)); // Dunkelbraun
        blockColors.put(Material.DAMAGED_ANVIL, new Color(105, 105, 105)); // Dunkelgrau
        blockColors.put(Material.BIRCH_LEAVES, new Color(166, 200, 102)); // Grünlich
        blockColors.put(Material.BAMBOO, new Color(124, 252, 0)); // Knalliges Grün
        blockColors.put(Material.ANDESITE, new Color(205, 205, 192)); // Hellgrau
        blockColors.put(Material.GRANITE, new Color(214, 140, 128)); // Rötlich
        blockColors.put(Material.COPPER_ORE, new Color(184, 115, 51)); // Kupferbraun
        blockColors.put(Material.DIRT, new Color(139, 69, 19)); // Dunkelbraun
        blockColors.put(Material.SUGAR_CANE, new Color(144, 238, 144)); // Hellgrün
        blockColors.put(Material.COAL_ORE, new Color(54, 54, 54)); // Schwarzgrau
        blockColors.put(Material.GRAVEL, new Color(169, 169, 169)); // Dunkleres Hellgrau
        blockColors.put(Material.SEAGRASS, new Color(0, 100, 0)); // Tiefgrün
        blockColors.put(Material.COCOA, new Color(139, 69, 19)); // Brauton
        blockColors.put(Material.CARROTS, new Color(255, 165, 0)); // Orange
        blockColors.put(Material.SAND, new Color(244, 164, 96)); // Sandfarben
        blockColors.put(Material.SANDSTONE, new Color(243, 229, 171)); // Helles Gelb/Braun
        blockColors.put(Material.TALL_SEAGRASS, new Color(34, 139, 34)); // Grün
        blockColors.put(Material.BAMBOO_PLANKS, new Color(154, 205, 50)); // Grüngelb
        blockColors.put(Material.ATTACHED_PUMPKIN_STEM, new Color(46, 139, 87)); // Grün
        blockColors.put(Material.PUMPKIN, new Color(255, 140, 0)); // Orange
        blockColors.put(Material.PUMPKIN_STEM, new Color(34, 139, 34)); // Grün
        blockColors.put(Material.CACTUS, new Color(50, 205, 50)); // Sattes Grün
        blockColors.put(Material.MELON, new Color(154, 205, 50)); // Helles, saftiges Grün
        blockColors.put(Material.ATTACHED_MELON_STEM, new Color(46, 139, 87)); // Grün
        blockColors.put(Material.JUNGLE_LEAVES, new Color(85, 107, 47)); // Dunkler Grünton
        blockColors.put(Material.CLAY, new Color(211, 211, 211)); // Hellgrau
        blockColors.put(Material.DIORITE, new Color(237, 237, 237)); // Quarzfarben
        blockColors.put(Material.IRON_ORE, new Color(184, 134, 11)); // Rostfarben
        blockColors.put(Material.JUNGLE_SAPLING, new Color(85, 107, 47)); // Dunkelgrün
        blockColors.put(Material.MAGMA_BLOCK, new Color(255, 69, 0)); // Feuriges Rot-Orange
        blockColors.put(Material.ACACIA_LOG, new Color(205, 133, 63)); // Mittelbraun
        blockColors.put(Material.OAK_LOG, new Color(139, 69, 19)); // Dunkles Braun
        blockColors.put(Material.ROSE_BUSH, new Color(255, 0, 0)); // Rot
        blockColors.put(Material.ALLIUM, new Color(218, 112, 214)); // Helllila
        blockColors.put(Material.LILAC, new Color(219, 112, 147)); // Rosa-Lila
    }

    public void generateMap() {
        if(isRendering != true) {
            isRendering = true;
            BufferedImage mapImage = null;
            // Speichern der Karte
            File mapFile = processBlocks();
            // Erstellen der blocks.txt
            // Erstellen des HTML
            generateHTML();
            isRendering = false;
        }else {plugin.getLogger().info("Server is rendering ");};

    }
    public void generateMapFull() {
        if(isRendering != true) {
            isRendering = true;
            BufferedImage mapImage = null;
            // Speichern der Karte
            File mapFile = processBlocksFull();
            // Erstellen der blocks.txt
            // Erstellen des HTML
            generateHTML();
            isRendering = false;
        }
        else {plugin.getLogger().info("Server is rendering ");};
    }
    public void dropAllTables() {
        try (PreparedStatement dropBlocksStmt = getPersistentConnection().prepareStatement("DROP TABLE IF EXISTS blocks");
             PreparedStatement dropChunksStmt = getPersistentConnection().prepareStatement("DROP TABLE IF EXISTS rendered_chunks")) {

            dropBlocksStmt.executeUpdate();
            dropChunksStmt.executeUpdate();
            plugin.getLogger().info("Alle Tabellen erfolgreich aus der Datenbank gelöscht.");

        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Löschen der Tabellen aus der Datenbank: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void rebuildDatabaseAndRerender() {
        if(isRendering != true) {
            isRendering = true;
            // 1. Lösche alle Tabellen
            dropAllTables();

            // 2. Initialisiere die Tabellen neu
            initializeDatabase();

            // 3. Render alle Chunks neu
            generateMapFull();

            plugin.getLogger().info("Die Datenbank wurde neu erstellt und alle Chunks wurden neu gerendert.");
            isRendering = false;
        }
        else {plugin.getLogger().info("Server is rendering ");};

    }
    private File processBlocksFull() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blocksFile))) {
            World world = Bukkit.getWorld("world");
            if (world == null) {
                plugin.getLogger().severe("Die Welt wurde nicht gefunden!");
                return null;
            }

            List<Block> blockList = new ArrayList<>();



            for (Chunk chunk : world.getLoadedChunks()) {


                if (isChunkRendered(chunk)) {
                    plugin.getLogger().info("Überspringe bereits gerenderten Chunk: X=" + chunk.getX() + ", Z=" + chunk.getZ());
                    continue; // Überspringe diesen Chunk
                }
                try {
                    // Warte 1 Sekunde (1 Sekunde = 1000 Millisekunden)
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // Fehlerbehandlung, falls der Thread unterbrochen wird
                    e.printStackTrace();
                }
                plugin.getLogger().info("Rendere Chunk: X=" + chunk.getX() + ", Z=" + chunk.getZ());

                int chunkX = chunk.getX() * 16;
                int chunkZ = chunk.getZ() * 16;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {

                        int worldX = chunkX + x;
                        int worldZ = chunkZ + z;

                        // Finde den obersten sichtbaren Block
                        Block surfaceBlock = Objects.requireNonNull(world.rayTraceBlocks(
                                new Location(world, worldX + 0.5, world.getMaxHeight(), worldZ + 0.5), // Startpunkt
                                new Vector(0, -1, 0), // Richtung (von oben nach unten)
                                world.getMaxHeight(), // Reichweite
                                FluidCollisionMode.ALWAYS // Beinhaltet Flüssigkeiten (Wasser, Lava)
                        )).getHitBlock();

                        if (surfaceBlock != null) {
                            blockList.add(surfaceBlock);
                        }
                    }
                }

                saveChunkAndBlocksToDatabase(chunk, blockList);

            }

            generateMapFromDatabase();
            return blocksFile;

        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Verarbeiten von Blöcken: " + e.getMessage());
            return null;
        }
    }

    public void generateHTML() {
        // Arbeitsordner und Template-Datei
        File workFolder = new File(plugin.getDataFolder(), "work");
        if (!workFolder.exists()) {
            workFolder.mkdirs();
        }
        File templateFolder = new File(plugin.getDataFolder(), "templates");
        ThymeleafHelper htmlHelper = new ThymeleafHelper(templateFolder);

        // Thymeleaf-Kontext erstellen
        Context context = new Context();
        context.setVariable("totalPlayers", Bukkit.getOnlinePlayers().size());
        context.setVariable("playersList", getPlayersList());

        // Index.html rendern
        File outputFile = new File(workFolder, "index.html");
        htmlHelper.render("index", outputFile, context);

        plugin.getLogger().info("HTML-Datei erfolgreich erstellt: " + outputFile.getAbsolutePath());
    }

    private synchronized File processBlocks() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blocksFile))) {
            World world = Bukkit.getWorld("world");
            if (world == null) {
                plugin.getLogger().severe("Die Welt wurde nicht gefunden!");
                return null;
            }

            List<Block> blockList = new ArrayList<>();
            int chunkCounter = 0; // Zähler für die Anzahl der verarbeiteten Chunks
            final int MAX_CHUNKS_PER_RUN = 10; // Maximale Anzahl Chunks pro Lauf

            for (Chunk chunk : world.getLoadedChunks()) {
                if (chunkCounter >= MAX_CHUNKS_PER_RUN) {
                    plugin.getLogger().info("Die maximale Anzahl von "+ MAX_CHUNKS_PER_RUN+ "Chunks wurde in diesem Durchlauf erreicht.");
                    break; // Breche die Schleife, wenn das Limit erreicht ist
                }

                if (isChunkRendered(chunk)) {
                    plugin.getLogger().info("Überspringe bereits gerenderten Chunk: X=" + chunk.getX() + ", Z=" + chunk.getZ());
                    continue; // Überspringe diesen Chunk
                }
                plugin.getLogger().info("Rendere Chunk: X=" + chunk.getX() + ", Z=" + chunk.getZ());

                int chunkX = chunk.getX() * 16;
                int chunkZ = chunk.getZ() * 16;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int worldX = chunkX + x;
                        int worldZ = chunkZ + z;

                        // Finde den obersten sichtbaren Block
                        Block surfaceBlock = Objects.requireNonNull(world.rayTraceBlocks(
                                new Location(world, worldX + 0.5, world.getMaxHeight(), worldZ + 0.5), // Startpunkt
                                new Vector(0, -1, 0), // Richtung (von oben nach unten)
                                world.getMaxHeight(), // Reichweite
                                FluidCollisionMode.NEVER // Ignoriert Flüssigkeiten (Wasser, Lava)
                        )).getHitBlock();

                        if (surfaceBlock != null) {
                            blockList.add(surfaceBlock);
                        }
                    }
                }

                saveChunkAndBlocksToDatabase(chunk, blockList);
                chunkCounter++; // Erhöhe den Zähler nach der Verarbeitung des Chunks
            }

            generateMapFromDatabase();
            return blocksFile;

        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Verarbeiten von Blöcken: " + e.getMessage());
            return null;
        }
    }

    private String getPlayersList() {
        // Lade alle Spielernamen
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }
    private void saveChunkAndBlocksToDatabase(Chunk chunk, List<Block> blockList) {
        try {
            // Chunk speichern
            String insertChunkSQL = "INSERT INTO rendered_chunks (world_name, chunk_x, chunk_z) " +
                    "VALUES (?, ?, ?) ON CONFLICT(world_name, chunk_x, chunk_z) DO NOTHING";
            try (PreparedStatement chunkStmt = getPersistentConnection().prepareStatement(insertChunkSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                chunkStmt.setString(1, chunk.getWorld().getName());
                chunkStmt.setInt(2, chunk.getX());
                chunkStmt.setInt(3, chunk.getZ());
                chunkStmt.executeUpdate();

                // ID des Chunks abrufen
                ResultSet generatedKeys = chunkStmt.getGeneratedKeys();
                int chunkId = -1;
                if (generatedKeys.next()) {
                    chunkId = generatedKeys.getInt(1); // Neuer Chunk eingefügt
                } else {
                    // Wenn kein Eintrag erstellt wurde, hole die ID des bestehenden Chunks
                    String selectChunkIdSQL = "SELECT id FROM rendered_chunks WHERE world_name = ? AND chunk_x = ? AND chunk_z = ?";
                    try (PreparedStatement selectStmt = getPersistentConnection().prepareStatement(selectChunkIdSQL)) {
                        selectStmt.setString(1, chunk.getWorld().getName());
                        selectStmt.setInt(2, chunk.getX());
                        selectStmt.setInt(3, chunk.getZ());
                        ResultSet resultSet = selectStmt.executeQuery();
                        if (resultSet.next()) {
                            chunkId = resultSet.getInt("id");
                        }
                    }
                }

                if (chunkId >= 0) {
                    // Blockdaten speichern
                    String insertBlockSQL = "INSERT INTO blocks (rendered_chunk_id, block_x, block_y, block_z, block_type) " +
                            "VALUES (?, ?, ?, ?, ?) ON CONFLICT (rendered_chunk_id, block_x, block_y, block_z) DO NOTHING";
                    try (PreparedStatement blockStmt = getPersistentConnection().prepareStatement(insertBlockSQL)) {
                        for (Block blockData : blockList) {
                            blockStmt.setInt(1, chunkId);
                            blockStmt.setInt(2, blockData.getX());
                            blockStmt.setInt(3, blockData.getY());
                            blockStmt.setInt(4, blockData.getZ());
                            blockStmt.setString(5, blockData.getType().name());
                            blockStmt.addBatch();
                        }
                        blockStmt.executeBatch();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isChunkRendered(Chunk chunk) {
        try (PreparedStatement stmt = getPersistentConnection().prepareStatement(
                "SELECT id FROM rendered_chunks WHERE world_name = ? AND chunk_x = ? AND chunk_z = ?")) {
            stmt.setString(1, chunk.getWorld().getName());
            stmt.setInt(2, chunk.getX());
            stmt.setInt(3, chunk.getZ());
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Connection getPersistentConnection() {
        try {
            Connection connection = databaseManager.getConnection();
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().warning("Die Verbindung zur Datenbank ist nicht aktiv. Stelle Verbindung wieder her...");
                connection = databaseManager.getConnection(); // Erneuter Verbindungsaufbau
                if (connection == null || connection.isClosed()) {
                    plugin.getLogger().severe("Die Wiederherstellung der Verbindung zur Datenbank ist fehlgeschlagen.");
                    databaseManager = new DatabaseManager(plugin.getDataFolder());
                     databaseManager.connect();
                    connection = databaseManager.getConnection();
                }
            }
            return connection;
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Herstellen der Verbindung: " + e.getMessage());
            return null;
        }
    }
//    private void generateMapFromDatabase() {
//        BufferedImage mapImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
//        int spawnX, spawnZ;
//
//        try {
//            World world = Bukkit.getWorld("world");
//            if (world == null) {
//                plugin.getLogger().severe("Die Welt wurde nicht gefunden!");
//                return;
//            }
//            spawnX = world.getSpawnLocation().getBlockX();
//            spawnZ = world.getSpawnLocation().getBlockZ();
//
//            String querySQL = "SELECT block_x, block_z, block_type FROM blocks " +
//                    "JOIN rendered_chunks ON blocks.rendered_chunk_id = rendered_chunks.id " +
//                    "WHERE rendered_chunks.world_name = ?";
//            try (PreparedStatement stmt = getPersistentConnection().prepareStatement(querySQL)) {
//                stmt.setString(1, world.getName());
//                ResultSet resultSet = stmt.executeQuery();
//
//                while (resultSet.next()) {
//                    int blockX = resultSet.getInt("block_x");
//                    int blockZ = resultSet.getInt("block_z");
//                    String blockType = resultSet.getString("block_type");
//
//                    int pixelX = (1920 / 2) + (blockX - spawnX);
//                    int pixelZ = (1080 / 2) + (blockZ - spawnZ);
//
//                    if (pixelX >= 0 && pixelX < mapImage.getWidth() && pixelZ >= 0 && pixelZ < mapImage.getHeight()) {
//                        Material material = Material.getMaterial(blockType);
//                        if (material != null) {
//                            Color color = blockColors.getOrDefault(material, Color.LIGHT_GRAY);
//                            mapImage.setRGB(pixelX, pixelZ, color.getRGB());
//                        }
//                    }
//                }
//
//                ImageIO.write(mapImage, "png", new File(plugin.getDataFolder(), "/work/map.png"));
//                plugin.getLogger().info("Die Karte wurde erfolgreich aus der Datenbank generiert und gespeichert!");
//            }
//        } catch (Exception e) {
//            plugin.getLogger().severe("Fehler beim Generieren der Karte aus der Datenbank: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
private void generateMapFromDatabase() {
    int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
    int chunkSize = 16; // Ein Chunk ist 16x16 Blöcke

    try {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().severe("Die Welt wurde nicht gefunden!");
            return;
        }

        String queryBoundsSQL = "SELECT MIN(block_x) AS min_x, MIN(block_z) AS min_z, MAX(block_x) AS max_x, MAX(block_z) AS max_z " +
                "FROM blocks " +
                "JOIN rendered_chunks ON blocks.rendered_chunk_id = rendered_chunks.id " +
                "WHERE rendered_chunks.world_name = ?";
        try (PreparedStatement stmt = getPersistentConnection().prepareStatement(queryBoundsSQL)) {
            stmt.setString(1, world.getName());
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                minX = resultSet.getInt("min_x");
                minZ = resultSet.getInt("min_z");
                maxX = resultSet.getInt("max_x");
                maxZ = resultSet.getInt("max_z");
            } else {
                plugin.getLogger().warning("Keine Daten gefunden, Karte nicht generiert.");
                return;
            }
        }

        // Berechnung der PNG-Größe
        int width = (maxX - minX) + 1; // Breite in Blöcken
        int height = (maxZ - minZ) + 1; // Höhe in Blöcken

        // Optional: Vergrößerung der Karte für bessere Sichtbarkeit (z. B. 1 Block = 2 Pixel)
        int scaleFactor = 2;
        BufferedImage mapImage = new BufferedImage(width * scaleFactor, height * scaleFactor, BufferedImage.TYPE_INT_RGB);

        // Zeichengesperre auf den Spawn-Punkt ausrichten
        int spawnX = world.getSpawnLocation().getBlockX();
        int spawnZ = world.getSpawnLocation().getBlockZ();

        String queryBlocksSQL = "SELECT block_x, block_z, block_type FROM blocks " +
                "JOIN rendered_chunks ON blocks.rendered_chunk_id = rendered_chunks.id " +
                "WHERE rendered_chunks.world_name = ?";
        try (PreparedStatement stmt = getPersistentConnection().prepareStatement(queryBlocksSQL)) {
            stmt.setString(1, world.getName());
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                int blockX = resultSet.getInt("block_x");
                int blockZ = resultSet.getInt("block_z");
                String blockType = resultSet.getString("block_type");

                // Pixelkoordinaten bestimmen (verschoben für den minimierten Bereich)
                int pixelX = (blockX - minX) * scaleFactor;
                int pixelZ = (blockZ - minZ) * scaleFactor;

                Material material = Material.valueOf(blockType);
                Color color = blockColors.getOrDefault(material, Color.LIGHT_GRAY);

                // Bereiche ausfüllen (pro Block mehrere Pixel, falls scaleFactor > 1)
                for (int dx = 0; dx < scaleFactor; dx++) {
                    for (int dz = 0; dz < scaleFactor; dz++) {
                        if (pixelX + dx < mapImage.getWidth() && pixelZ + dz < mapImage.getHeight()) {
                            mapImage.setRGB(pixelX + dx, pixelZ + dz, color.getRGB());
                        }
                    }
                }
            }
        }

        // Karte als PNG speichern
        File outputFile = new File(plugin.getDataFolder(), "/work/map.png");
        ImageIO.write(mapImage, "png", outputFile);
        plugin.getLogger().info("Die Karte wurde dynamisch basierend auf geladenen Chunks erstellt: " + outputFile.getAbsolutePath());

    } catch (Exception e) {
        plugin.getLogger().severe("Fehler beim Generieren der Karte: " + e.getMessage());
        e.printStackTrace();
    }
}
    private void initializeDatabase() {
        String createRenderedChunksTable = "CREATE TABLE IF NOT EXISTS rendered_chunks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "world_name TEXT NOT NULL, " +
                "chunk_x INTEGER NOT NULL, " +
                "chunk_z INTEGER NOT NULL, " +
                "UNIQUE(world_name, chunk_x, chunk_z)" +
                ");";

        String createBlocksTable = "CREATE TABLE IF NOT EXISTS blocks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rendered_chunk_id INTEGER NOT NULL, " +
                "block_x INTEGER NOT NULL, " +
                "block_y INTEGER NOT NULL, " +
                "block_z INTEGER NOT NULL, " +
                "block_type TEXT NOT NULL, " +
                "FOREIGN KEY (rendered_chunk_id) REFERENCES rendered_chunks(id) ON DELETE CASCADE, " +
                "UNIQUE(rendered_chunk_id, block_x, block_y, block_z)" +
                ");";

        try (Connection connection = getPersistentConnection(); // Verwenden der persistierenden Verbindung
             PreparedStatement createChunksStmt = connection.prepareStatement(createRenderedChunksTable);
             PreparedStatement createBlocksStmt = connection.prepareStatement(createBlocksTable)) {

            // Tabellen erstellen
            createChunksStmt.execute();
            createBlocksStmt.execute();
            plugin.getLogger().info("Datenbank-Tabellen erfolgreich initialisiert.");

        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Initialisieren der Tabellen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


