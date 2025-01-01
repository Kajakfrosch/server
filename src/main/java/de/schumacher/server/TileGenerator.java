package de.schumacher.server;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TileGenerator {
    public static void generateTiles(File inputFile, File outputDir) throws Exception {
        BufferedImage image = ImageIO.read(inputFile);
        int tileSize = 256;

        // Erstelle das Ausgabe-Verzeichnis, falls nötig
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        int tilesX = (int) Math.ceil(image.getWidth() / (double) tileSize);
        int tilesY = (int) Math.ceil(image.getHeight() / (double) tileSize);

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                int startX = x * tileSize;
                int startY = y * tileSize;

                int width = Math.min(tileSize, image.getWidth() - startX);
                int height = Math.min(tileSize, image.getHeight() - startY);

                BufferedImage tile = image.getSubimage(startX, startY, width, height);

                File outputFile = new File(outputDir, "tile_" + x + "_" + y + ".png");
                ImageIO.write(tile, "png", outputFile);
            }
        }

        System.out.println("Tiles wurden erfolgreich generiert und in " + outputDir.getAbsolutePath() + " gespeichert!");
    }
}