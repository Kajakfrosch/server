package de.schumacher.server;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class ThymeleafHelper {

    private final TemplateEngine templateEngine;

    public ThymeleafHelper(File templateFolder) {
        // Resolver für Dateien im angegebenen Ordner
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setPrefix(templateFolder.getAbsolutePath() + File.separator);
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // Debug-Funktion für schnelleres Testen.

        // TemplateEngine initialisieren
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    /**
     * Rendert ein Thymeleaf-Template mit den bereitgestellten Daten.
     */
    public void render(String templateName, File outputFile, Context context) {
        try (Writer writer = new FileWriter(outputFile)) {
            // Template in Datei rendern
            this.templateEngine.process(templateName, context, writer);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Fehler beim Rendern des Templates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}