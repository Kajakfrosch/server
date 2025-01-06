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
        blockColors.put(Material.OAK_LEAVES, new Color(93, 161, 48));
        blockColors.put(Material.SHORT_GRASS, new Color(124, 252, 0));
        blockColors.put(Material.GRASS_BLOCK, new Color(50, 205, 50));
        blockColors.put(Material.BIRCH_LEAVES, new Color(166, 200, 102));
        blockColors.put(Material.SPRUCE_LEAVES, new Color(72, 107, 79));
        blockColors.put(Material.LARGE_FERN, new Color(34, 139, 34));
        blockColors.put(Material.WATER, new Color(70, 130, 180));
        blockColors.put(Material.SAND, new Color(244, 164, 96));
        blockColors.put(Material.TALL_SEAGRASS, new Color(46, 139, 87));
        blockColors.put(Material.SEAGRASS, new Color(0, 100, 0));
        blockColors.put(Material.POPPY, new Color(255, 64, 64));
        blockColors.put(Material.DANDELION, new Color(254, 216, 93));
        blockColors.put(Material.DIORITE, new Color(237, 237, 237));
        blockColors.put(Material.STONE, new Color(169, 169, 169));
        blockColors.put(Material.GRAVEL, new Color(182, 182, 182));
        blockColors.put(Material.FERN, new Color(86, 125, 70));
        blockColors.put(Material.PEONY, new Color(255, 192, 203));
        blockColors.put(Material.SWEET_BERRY_BUSH, new Color(139, 0, 0));
        blockColors.put(Material.ANDESITE, new Color(205, 205, 192));
        blockColors.put(Material.GRANITE, new Color(214, 140, 128));
        blockColors.put(Material.DARK_OAK_LOG, new Color(47, 30, 21));
        blockColors.put(Material.WHITE_WALL_BANNER, new Color(255, 255, 255));
        blockColors.put(Material.DARK_OAK_FENCE, new Color(59, 42, 28));
        blockColors.put(Material.TORCH, new Color(255, 215, 0));
        blockColors.put(Material.DARK_OAK_PLANKS, new Color(75, 58, 41));
        blockColors.put(Material.KELP, new Color(1, 50, 32));
        blockColors.put(Material.DIRT, new Color(139, 69, 19));
        blockColors.put(Material.WHITE_WOOL, new Color(250, 240, 230));
        blockColors.put(Material.DARK_OAK_SLAB, new Color(59, 42, 28));
        blockColors.put(Material.DARK_OAK_STAIRS, new Color(59, 42, 28));
        blockColors.put(Material.CORNFLOWER, new Color(100, 149, 237));
        blockColors.put(Material.AZURE_BLUET, new Color(240, 255, 255));
        blockColors.put(Material.OXEYE_DAISY, new Color(255, 255, 255));
        blockColors.put(Material.SUGAR_CANE, new Color(0, 155, 119));
        blockColors.put(Material.PUMPKIN, new Color(255, 117, 24));
        blockColors.put(Material.DIRT_PATH, new Color(139, 90, 43));
        blockColors.put(Material.SANDSTONE, new Color(243, 229, 171));
        blockColors.put(Material.OAK_STAIRS, new Color(210, 180, 140));
        blockColors.put(Material.OAK_TRAPDOOR, new Color(160, 82, 45));
        blockColors.put(Material.COBBLESTONE_STAIRS, new Color(128, 128, 128));
        blockColors.put(Material.OAK_PLANKS, new Color(222, 184, 135));
        blockColors.put(Material.OAK_FENCE, new Color(193, 154, 107));
        blockColors.put(Material.ICE, new Color(176, 224, 230));
        blockColors.put(Material.SNOW, new Color(255, 255, 255));
        blockColors.put(Material.PACKED_ICE, new Color(173, 216, 230));
        blockColors.put(Material.COPPER_ORE, new Color(184, 115, 51));
        blockColors.put(Material.BUBBLE_COLUMN, new Color(152, 245, 255));
        blockColors.put(Material.COAL_ORE, new Color(54, 69, 79));
        blockColors.put(Material.SNOW_BLOCK, new Color(255, 255, 255));
        blockColors.put(Material.NETHERRACK, new Color(165, 42, 42));
        blockColors.put(Material.RED_MUSHROOM, new Color(155, 17, 30));
        blockColors.put(Material.BROWN_MUSHROOM, new Color(139, 69, 19));
        blockColors.put(Material.IRON_BARS, new Color(138, 138, 138));
    }

    public void generateMap() {
        BufferedImage mapImage = null;
        // Speichern der Karte
        File mapFile = processBlocks();
        // Erstellen der blocks.txt
        // Erstellen des HTML
        generateHTML();

    }
    public void generateMapFull() {
        BufferedImage mapImage = null;
        // Speichern der Karte
        File mapFile = processBlocksFull();
        // Erstellen der blocks.txt
        // Erstellen des HTML
        generateHTML();

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
    private void generateMapFromDatabase() {
        BufferedImage mapImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        int spawnX, spawnZ;

        try {
            World world = Bukkit.getWorld("world");
            if (world == null) {
                plugin.getLogger().severe("Die Welt wurde nicht gefunden!");
                return;
            }
            spawnX = world.getSpawnLocation().getBlockX();
            spawnZ = world.getSpawnLocation().getBlockZ();

            String querySQL = "SELECT block_x, block_z, block_type FROM blocks " +
                    "JOIN rendered_chunks ON blocks.rendered_chunk_id = rendered_chunks.id " +
                    "WHERE rendered_chunks.world_name = ?";
            try (PreparedStatement stmt = getPersistentConnection().prepareStatement(querySQL)) {
                stmt.setString(1, world.getName());
                ResultSet resultSet = stmt.executeQuery();

                while (resultSet.next()) {
                    int blockX = resultSet.getInt("block_x");
                    int blockZ = resultSet.getInt("block_z");
                    String blockType = resultSet.getString("block_type");

                    int pixelX = (1920 / 2) + (blockX - spawnX);
                    int pixelZ = (1080 / 2) + (blockZ - spawnZ);

                    if (pixelX >= 0 && pixelX < mapImage.getWidth() && pixelZ >= 0 && pixelZ < mapImage.getHeight()) {
                        Material material = Material.getMaterial(blockType);
                        if (material != null) {
                            Color color = blockColors.getOrDefault(material, Color.LIGHT_GRAY);
                            mapImage.setRGB(pixelX, pixelZ, color.getRGB());
                        }
                    }
                }

                ImageIO.write(mapImage, "png", new File(plugin.getDataFolder(), "/work/map.png"));
                plugin.getLogger().info("Die Karte wurde erfolgreich aus der Datenbank generiert und gespeichert!");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Generieren der Karte aus der Datenbank: " + e.getMessage());
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


