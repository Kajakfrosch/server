

package de.schumacher.server;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;


public class EmbeddedTomcatServer {

    private final int port;
    private final String webRoot;
    private Tomcat tomcat;
    private Thread awaitThread;
    public EmbeddedTomcatServer(int port, String webRoot) {
        this.port = port;
        this.webRoot = webRoot;
    }
    public void start() throws Exception {
        // Tomcat-Instanz erstellen
        tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(port);

        System.out.println("Starting Embedded Tomcat Server on Port " + port + "!");

        // Optional: HTTPS hinzufügen (wenn benötigt)
        Connector sslConnector = new Connector();
        sslConnector.setPort(8443); // HTTPS-Port
        sslConnector.setSecure(true);
        sslConnector.setScheme("https");
        tomcat.getService().addConnector(sslConnector);

        // WebRoot-Verzeichnis überprüfen
        File root = new File(webRoot).getAbsoluteFile();
        if (!root.exists()) {
            throw new IllegalArgumentException("WebRoot-Verzeichnis existiert nicht: " + root.getAbsolutePath());
        }

        // Context hinzufügen
        Context context = tomcat.addContext("", webRoot);

        // Default-Servlet für statische Dateien
        context.addWelcomeFile("index.html");
        tomcat.addServlet("", "default", new DefaultServlet());
        context.addServletMappingDecoded("/", "default");

        // Tomcat starten
        tomcat.start();
        System.out.println("Tomcat gestartet auf Port " + port);

        // Thread für Tomcat await
        awaitThread = new Thread(() -> tomcat.getServer().await());
        awaitThread.setDaemon(true);
        awaitThread.start();
    }

    public void stop() {
        if (tomcat != null) {
            try {
                // Beende Tomcat blockierend
                tomcat.stop();
                tomcat.destroy();
                System.out.println("Tomcat wurde erfolgreich gestoppt.");
            } catch (LifecycleException e) {
                System.err.println("Fehler beim Stoppen des Tomcat-Servers:");
                e.printStackTrace();
            }
        }

        if (awaitThread != null && awaitThread.isAlive()) {
            // Unterbreche den blockierenden Thread
            awaitThread.interrupt();
        }
    }

}




