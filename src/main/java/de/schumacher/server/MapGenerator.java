package de.schumacher.server;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapGenerator {

    private final Server plugin;
    private final Map<org.bukkit.Material, Color> blockColors;

    public MapGenerator(Server plugin) {
        this.plugin = plugin;

        // Blockfarben initialisieren
        blockColors = new HashMap<>();
        initializeBlockColors();
    }

    private void initializeBlockColors() {
        blockColors.put(org.bukkit.Material.OAK_LEAVES, new Color(93, 161, 48));
        blockColors.put(org.bukkit.Material.SHORT_GRASS, new Color(124, 252, 0));
        blockColors.put(org.bukkit.Material.GRASS_BLOCK, new Color(50, 205, 50));
        blockColors.put(org.bukkit.Material.BIRCH_LEAVES, new Color(166, 200, 102));
        blockColors.put(org.bukkit.Material.SPRUCE_LEAVES, new Color(72, 107, 79));
        blockColors.put(org.bukkit.Material.LARGE_FERN, new Color(34, 139, 34));
        blockColors.put(org.bukkit.Material.WATER, new Color(70, 130, 180));
        blockColors.put(org.bukkit.Material.SAND, new Color(244, 164, 96));
        blockColors.put(org.bukkit.Material.TALL_SEAGRASS, new Color(46, 139, 87));
        blockColors.put(org.bukkit.Material.SEAGRASS, new Color(0, 100, 0));
        blockColors.put(org.bukkit.Material.POPPY, new Color(255, 64, 64));
        blockColors.put(org.bukkit.Material.DANDELION, new Color(254, 216, 93));
        blockColors.put(org.bukkit.Material.DIORITE, new Color(237, 237, 237));
        blockColors.put(org.bukkit.Material.STONE, new Color(169, 169, 169));
        blockColors.put(org.bukkit.Material.GRAVEL, new Color(182, 182, 182));
        blockColors.put(org.bukkit.Material.FERN, new Color(86, 125, 70));
        blockColors.put(org.bukkit.Material.PEONY, new Color(255, 192, 203));
        blockColors.put(org.bukkit.Material.SWEET_BERRY_BUSH, new Color(139, 0, 0));
        blockColors.put(org.bukkit.Material.ANDESITE, new Color(205, 205, 192));
        blockColors.put(org.bukkit.Material.GRANITE, new Color(214, 140, 128));
        blockColors.put(org.bukkit.Material.DARK_OAK_LOG, new Color(47, 30, 21));
        blockColors.put(org.bukkit.Material.WHITE_WALL_BANNER, new Color(255, 255, 255));
        blockColors.put(org.bukkit.Material.DARK_OAK_FENCE, new Color(59, 42, 28));
        blockColors.put(org.bukkit.Material.TORCH, new Color(255, 215, 0));
        blockColors.put(org.bukkit.Material.DARK_OAK_PLANKS, new Color(75, 58, 41));
        blockColors.put(org.bukkit.Material.KELP, new Color(1, 50, 32));
        blockColors.put(org.bukkit.Material.DIRT, new Color(139, 69, 19));
        blockColors.put(org.bukkit.Material.WHITE_WOOL, new Color(250, 240, 230));
        blockColors.put(org.bukkit.Material.DARK_OAK_SLAB, new Color(59, 42, 28));
        blockColors.put(org.bukkit.Material.DARK_OAK_STAIRS, new Color(59, 42, 28));
        blockColors.put(org.bukkit.Material.CORNFLOWER, new Color(100, 149, 237));
        blockColors.put(org.bukkit.Material.AZURE_BLUET, new Color(240, 255, 255));
        blockColors.put(org.bukkit.Material.OXEYE_DAISY, new Color(255, 255, 255));
        blockColors.put(org.bukkit.Material.SUGAR_CANE, new Color(0, 155, 119));
        blockColors.put(org.bukkit.Material.PUMPKIN, new Color(255, 117, 24));
        blockColors.put(org.bukkit.Material.DIRT_PATH, new Color(139, 90, 43));
        blockColors.put(org.bukkit.Material.SANDSTONE, new Color(243, 229, 171));
        blockColors.put(org.bukkit.Material.OAK_STAIRS, new Color(210, 180, 140));
        blockColors.put(org.bukkit.Material.OAK_TRAPDOOR, new Color(160, 82, 45));
        blockColors.put(org.bukkit.Material.COBBLESTONE_STAIRS, new Color(128, 128, 128));
        blockColors.put(org.bukkit.Material.OAK_PLANKS, new Color(222, 184, 135));
        blockColors.put(org.bukkit.Material.OAK_FENCE, new Color(193, 154, 107));
        blockColors.put(org.bukkit.Material.ICE, new Color(176, 224, 230));
        blockColors.put(org.bukkit.Material.SNOW, new Color(255, 255, 255));
        blockColors.put(org.bukkit.Material.PACKED_ICE, new Color(173, 216, 230));
        blockColors.put(org.bukkit.Material.COPPER_ORE, new Color(184, 115, 51));
        blockColors.put(org.bukkit.Material.BUBBLE_COLUMN, new Color(152, 245, 255));
        blockColors.put(org.bukkit.Material.COAL_ORE, new Color(54, 69, 79));
        blockColors.put(org.bukkit.Material.SNOW_BLOCK, new Color(255, 255, 255));
        blockColors.put(org.bukkit.Material.NETHERRACK, new Color(165, 42, 42));
        blockColors.put(org.bukkit.Material.RED_MUSHROOM, new Color(155, 17, 30));
        blockColors.put(org.bukkit.Material.BROWN_MUSHROOM, new Color(139, 69, 19));
        blockColors.put(org.bukkit.Material.IRON_BARS, new Color(138, 138, 138));
    }

    public void generateMap() {
        BufferedImage mapImage = createMapImage();

        if (mapImage != null) {
            // Speichern der Karte
            File mapFile = saveMapImage(mapImage);
            // Erstellen der blocks.txt
            writeBlocksToFile();

            // Erstellen des HTML
            generateHTML(mapFile);
        }
    }

    private BufferedImage createMapImage() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blocksFile))) {
            int chunkSize = 16; // Ein Chunk hat 16x16 Blöcke
            int mapWidth = 1920; // Breite der Karte (kann dynamisch angepasst werden)
            int mapHeight = 1080; // Höhe der Karte (kann dynamisch angepasst werden)

            BufferedImage mapImage = new BufferedImage(mapWidth, mapHeight, BufferedImage.TYPE_INT_RGB);

            // Iteriere über alle Welten
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    int chunkX = chunk.getX() * chunkSize;
                    int chunkZ = chunk.getZ() * chunkSize;

                    for (int x = 0; x < chunkSize; x++) {
                        for (int z = 0; z < chunkSize; z++) {
                            int worldX = chunkX + x;
                            int worldZ = chunkZ + z;

                            // Fallback: Standardfarbe
                            int pixelX = (worldX % mapWidth + mapWidth) % mapWidth;
                            int pixelZ = (worldZ % mapHeight + mapHeight) % mapHeight;

                            mapImage.setRGB(pixelX, pixelZ, Color.LIGHT_GRAY.getRGB());

                            org.bukkit.block.Block surfaceBlock = null;
                            for (int y = world.getMaxHeight() - 1; y >= world.getMinHeight(); y--) {
                                org.bukkit.block.Block block = world.getBlockAt(worldX, y, worldZ);
                                if (block.getType() != org.bukkit.Material.AIR &&
                                        block.getType() != org.bukkit.Material.BEDROCK &&
                                        block.getType() != Material.END_STONE &&
                                        block.getType() != Material.VINE &&
                                        block.getType() != Material.TALL_GRASS &&
                                        block.getType() != Material.FIRE &&
                                        block.getType() != Material.OBSIDIAN&&
                                        block.getType() != Material.NETHERRACK &&
                                        block.getType() != Material.NETHER_BRICK &&
                                        block.getType() != Material.NETHER_PORTAL&&
                                        block.getType() != Material.BASALT) {
                                    surfaceBlock = block;
                                    break;
                                }
                            }

                            if (surfaceBlock == null) {
                                continue;
                            }

                            org.bukkit.Material blockType = surfaceBlock.getType();
                            // Hole Farbe
                            Color blockColor = blockColors.getOrDefault(blockType, Color.LIGHT_GRAY); // Verwende Standardfarbe

                            mapImage.setRGB(pixelX, pixelZ, blockColor.getRGB());
                            writer.write("Block: " + blockType.name() + " Farbe: " + blockColor.toString() + "\n");
                        }
                    }
                }
            }

            plugin.getLogger().info("Blocks.txt erstellt: " + blocksFile.getAbsolutePath());
            return mapImage;

        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der blocks.txt: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler bei der Erstellung der Karte: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private File saveMapImage(BufferedImage mapImage) {
        File mapFile = new File(plugin.getDataFolder()+"/work", "map.png");
        try {
            if (!mapFile.getParentFile().exists()) {
                mapFile.getParentFile().mkdirs();
            }

            ImageIO.write(mapImage, "png", mapFile);
            plugin.getLogger().info("Karte gespeichert: " + mapFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Karte: " + e.getMessage());
            e.printStackTrace();
        }
        return mapFile;
    }
    private void writeBlocksToFile() {
        File blocksFile = new File(plugin.getDataFolder(), "blocks.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(blocksFile))) {
            // Header oder initialer Hinweis (optional, kann auch weggelassen werden)
            writer.write("Blocktyp;x;z");
            writer.newLine();

            int chunkSize = 16; // Ein Chunk hat 16x16 Blöcke

            // Iteriere über alle Welten
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    int chunkX = chunk.getX() * chunkSize; // Chunk-Koordinaten
                    int chunkZ = chunk.getZ() * chunkSize;

                    for (int x = 0; x < chunkSize; x++) {
                        for (int z = 0; z < chunkSize; z++) {
                            int worldX = chunkX + x;
                            int worldZ = chunkZ + z;

                            org.bukkit.block.Block surfaceBlock = null;
                            for (int y = world.getMaxHeight() - 1; y >= world.getMinHeight(); y--) {
                                org.bukkit.block.Block block = world.getBlockAt(worldX, y, worldZ);
                                if (block.getType() != org.bukkit.Material.AIR &&
                                        block.getType() != org.bukkit.Material.BEDROCK &&
                                        block.getType() != Material.END_STONE &&
                                        block.getType() != Material.VINE &&
                                        block.getType() != Material.TALL_GRASS &&
                                        block.getType() != Material.FIRE &&
                                        block.getType() != Material.OBSIDIAN&&
                                        block.getType() != Material.NETHERRACK &&
                                        block.getType() != Material.NETHER_BRICK &&
                                        block.getType() != Material.NETHER_PORTAL&&
                                        block.getType() != Material.BASALT) {
                                    surfaceBlock = block;
                                    break;
                                }
                            }

                            if (surfaceBlock == null) {
                                continue;
                            }

                            org.bukkit.Material blockType = surfaceBlock.getType();

                            // Schreibe blocktyp;x;z in die Datei
                            writer.write(blockType.name() + ";" + worldX + ";" + worldZ);
                            writer.newLine();
                        }
                    }
                }
            }

            plugin.getLogger().info("blocks.txt wurde erstellt: " + blocksFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der blocks.txt: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            plugin.getLogger().severe("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void generateHTML(File mapFile) {
        File htmlFile = new File(plugin.getDataFolder()+"/work", "index.html");
        try (Writer writer = new FileWriter(htmlFile)) {
            // Lade Spielernamen und Statistiken
            String playersList = getPlayersList();
            int totalPlayers = Bukkit.getOnlinePlayers().size();

            // Generiere HTML-Template
            String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Server Map</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        text-align: center;
                        padding: 20px;
                    }
                    img {
                        max-width: 90%%;
                        height: auto;
                        border: 1px solid #ddd;
                        box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.1);
                    }
                    .stats {
                        margin-top: 20px;
                        padding: 10px;
                        background: #fff;
                        border: 1px solid #ddd;
                        box-shadow: 2px 2px 10px rgba(0, 0, 0, 0.1);
                        display: inline-block;
                    }
                    .players {
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <h1>Server Map</h1>
                <img src="map.png" alt="Server World Map">
                <div class="stats">
                    <h2>Server Statistics</h2>
        """;

            // Fügt dynamischen Inhalt hinzu
            htmlContent += "<p><strong>Online Players:</strong> " + totalPlayers + "</p>";
            htmlContent += "<div class=\"players\"><h3>Player Names:</h3><p>" + playersList.replace("%", "%%") + "</p></div>";
            htmlContent += """
                </div>
            </body>
            </html>
        """;

            writer.write(htmlContent);
            plugin.getLogger().info("HTML-Datei gespeichert: " + htmlFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der HTML-Datei: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private String getPlayersList() {
        // Lade alle Spielernamen
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }
}