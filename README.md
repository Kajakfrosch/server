## Wiki: Minecraft Server Plugin - Übersicht und Funktionen
### Inhaltsverzeichnis
1. [Einleitung]()
2. [Features]()
    - [Dynamische Karten Generierung]()
    - [Zombie-Invasion Minispiel]()
    - [Server-Performance-Monitor]()
    - [Debugging-Tools]()

3. [Kommandos und Berechtigungen]()
4. [Konfigurationsoptionen]()
5. [Setup und Nutzung]()

### Einleitung
Dieses Minecraft-Server-Plugin bietet eine Vielzahl spannender Features, die die Interaktion mit der Minecraft-Welt verbessern und Spielern neue Herausforderungen bieten. Das Plugin bietet sowohl Administrationswerkzeuge als auch Minispiele und Server-Überwachung.
### Features
#### Dynamische Karten Generierung
Das Plugin erstellt automatisch dynamische Karten der Minecraft-Welt:
- **Kartenformat:** `map.png` im Ordner `/work`.
- **Zusätzliches Feature:** Eine begleitende `blocks.txt`-Datei listet alle sichtbaren Blöcke mit ihren Positionen in folgendem Format auf:
`Blocktyp;x;z`
- **HTML-Erstellung:** Eine eingebettete HTML-Seite wird ebenfalls erzeugt und zeigt die Karte mit zusätzlichen Server-Statistiken an. Die generierte HTML-Datei heißt `index.html`.

##### Nutzung
- Die Karte wird automatisch generiert, wenn Spieler beitreten oder die Welt gespeichert wird.
- Außerdem kann die Karte mit `/dynmap` manuell erstellt werden.

#### Zombie-Invasion Minispiel
Organisiere aufregende Zombie-Invasionen gegen Spieler:
- **Schwierigkeitsgrad und Entfernung:** Bei Start einer Invasion kann der Abstand der Zombies und die Schwierigkeit spezifiziert werden.
- **Regelerklärung:**
    - Zombies spawnen in Wellen.
    - Spieler können gewinnen und Belohnungen erhalten oder verlieren und eine kleine Strafe hinnehmen.

- **Option, alle Invasionen zu stoppen:** `/invasion stop` hält jede aktive Invasion an und setzt den Schwierigkeitsgrad auf "Peaceful".

##### Nutzung
- **Kommando:** `/invasion start <Abstand> <Schwierigkeit>` oder `/invasion stop`. Weitere Details zu Kommandos findest du [hier]().

#### Server-Performance-Monitor
Der Performance-Monitor überwacht verschiedene Aspekte des Servers:
- **Überprüfung der Tickrate (TPS):** Warnung bei niedriger Leistung auf Grundlage eines dynamischen Schwellenwertes.
- **Speicherüberwachung:** Erkennt Überlastung bei hoher Speicherbelegung.
- **Chunk-Überwachung:** Warnt vor zu vielen geladenen Chunks und ermöglicht Optimierungen.

##### Funktionen
- Automatische Überprüfung der Serverauslastung.
- Dynamisch konfigurierbare Schwellenwerte (siehe [Konfigurationsoptionen]()).
- Informationen über den aktuellen Serverstatus können mit dem `/serverstatus`-Kommando angezeigt werden.

#### Debugging-Tools
- **Debugging-Logs:** Debug-Nachrichten basierend auf verschiedenen Log-Stufen (`INFO`, `DEBUG`, `WARN`, `ERROR`).
- **Flexible Konfiguration:** Kann über `config.yml` aktiviert werden.
- **Zweck:** Hilfreich für Entwickler, um Probleme beim Einrichten oder Ausführen des Plugins zu finden.

### Kommandos und Berechtigungen
#### Befehle

| Kommando | Beschreibung | Nutzung | Rechte |
| --- | --- | --- | --- |
| `/dynmap` | Erstellt manuell eine Karte der Welt. | `/dynmap` | `server.dynmap` |
| `/invasion start <dist> <diff>` | Startet eine Zombie-Invasion. | `/invasion start 10 5` | `server.*` |
| `/invasion stop` | Stoppt aktive Invasionen. | `/invasion stop` | `server.*` |
| `/serverstatus` | Zeigt den aktuellen Zustand des Servers an. | `/serverstatus` | `server.serverstatus` |
#### Berechtigungen

| Berechtigung | Beschreibung | Standard |
| --- | --- | --- |
| `server.dynmap` | Erlaubt die Generierung der Weltkarte. | Nur OP |
| `server.serverstatus` | Zeigt den Serverstatus an. | Nur OP |
| `server.*` | Gibt Zugriff auf alle Funktionen des Plugins. | Nur OP |
### Konfigurationsoptionen
Die Konfigurationsdatei `config.yml` erlaubt dir, die Parameter des Plugins anzupassen:
#### Allgemeine Einstellungen:
``` yaml
MOTD: "Willkommen auf dem Minecraft-Server!"
Instanz: "Produktionsserver"
Knoten: "Frankfurt-1"
Webseite: "https://example.com"
```
#### Performance-Überwachung:
``` yaml
performance:
  tps_threshold: 19.0       # Warnung bei TPS unter diesem Wert
  memory_threshold: 0.8     # Warnung bei Speicher über 80%
  chunk_threshold: 2000     # Warnung bei mehr als 2000 geladenen Chunks
```
#### Zombie-Invasion Einstellungen:
``` yaml
invasion:
  default_distance: 10       # Standardabstand für Invasionen
  max_difficulty: 20         # Maximal erlaubte Schwierigkeit
  zombie_spawn_rate: 2       # Spawnrate der Zombies
  rewards:                   # Belohnungen für erfolgreiche Invasionen
    - material: "IRON_INGOT"
      min: 1
      max: 3
      chance: 70
    - material: "GOLD_INGOT"
      min: 1
      max: 2
      chance: 20
    - material: "DIAMOND"
      min: 1
      max: 1
      chance: 10
```
#### Debugging Optionen:
``` yaml
debug:
  enabled: false         # Aktivieren/Deaktivieren des Debugger
  log_level: "INFO"      # Log-Stufen: DEBUG, INFO, WARN, ERROR
```
### Setup und Nutzung
#### 1. Voraussetzungen
- **Java Version:** Java 21
- **Bukkit/Spigot:** Version kompatibel mit Minecraft API 1.21.

#### 2. Installation
1. **JAR-Datei hochladen:** Lade die Datei `server-0.8-Snapshot.jar` in den Plugin-Ordner deines Servers hoch.
2. **Konfigurationsdateien:** Starte den Server, um automatisch die `config.yml` und andere notwendige Dateien zu erstellen.
## **Apache Webserver für die Weltkarte einrichten**
Um die Weltkarte auf deinem Server sicher zugänglich zu machen, kannst du Apache mit SSL-Unterstützung konfigurieren. Nachfolgend findest du eine allgemeine Anleitung ohne spezifische Domain.
### **1. Apache-Installation und Grundkonfiguration**
1. Installiere Apache:
``` bash
   sudo apt update
   sudo apt install apache2
```
1. Starte den Dienst und aktiviere ihn:
``` bash
   sudo systemctl start apache2
   sudo systemctl enable apache2
```
1. Installiere Certbot, um ein kostenloses SSL-Zertifikat (Let's Encrypt) zu beantragen:
``` bash
   sudo apt install python3-certbot-apache
```
1. Generiere das SSL-Zertifikat:
``` bash
   sudo certbot --apache
```
1. Stelle sicher, dass deine Firewall Port 80 (HTTP) und Port 443 (HTTPS) für Apache zulässt:
``` bash
   sudo ufw allow 'Apache Full'
   sudo ufw reload
```
### **2. Beispiel für eine Apache-SSL-Konfigurationsdatei**
Speichere folgenden Inhalt in der Datei **`/etc/apache2/sites-available/ssl.conf`**:
``` apache
<VirtualHost *:80>
    ServerName your-domain.com
    Redirect / https://your-domain.com/

    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined

    RewriteEngine on
    RewriteCond %{SERVER_NAME} =your-domain.com
    RewriteRule ^ https://%{SERVER_NAME}%{REQUEST_URI} [END,NE,R=permanent]
</VirtualHost>

<VirtualHost *:443>
    ServerName your-domain.com
    DocumentRoot "/var/www/map/"

    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined

    SSLCertificateFile /etc/letsencrypt/live/your-domain.com/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/your-domain.com/privkey.pem
    Include /etc/letsencrypt/options-ssl-apache.conf
</VirtualHost>
```
**Achtung:** Ersetze **`your-domain.com`** mit der Domain deines Servers oder deiner öffentlichen IP-Adresse, falls du keine Domain hast.
### **3. Schritte zur Einrichtung der Karte**
1. Erstelle das Verzeichnis für die Karte:
``` bash
   sudo mkdir /var/www/map
```
1. Verlinke die generierten Plugin-Dateien in das Webserver-Verzeichnis:
``` bash
   sudo ln -s /opt/minecraft/plugins/server/work/index.html /var/www/map/index.html
   sudo ln -s /opt/minecraft/plugins/server/work/map.png /var/www/map/map.png
```
### **4. Apache-Konfiguration aktivieren**
1. Aktiviere die neue SSL-Konfiguration:
``` bash
   sudo a2ensite ssl.conf
```
1. Lade Apache neu, um alle Änderungen zu übernehmen:
``` bash
   sudo systemctl reload apache2
```
1. Teste die Konfiguration und den Status von Apache:
``` bash
   sudo apache2ctl configtest
   sudo systemctl status apache2
```
### **5. Zugriff testen**
1. Im Browser:
   Rufe die HTTPS-Adresse deines Servers (z. B. `https://your-domain.com`) auf.
   Falls keine Domain eingestellt ist, kannst du auf die Server-IP-Adresse zugreifen, indem du den Port 443 (HTTPS) verwendest.

### **Hinweise**
- Wenn du eine öffentliche Domain verwendest, stelle sicher, dass die DNS-Einträge korrekt gesetzt sind.
- Zertifikate von Let's Encrypt haben eine Laufzeit von 90 Tagen. Nutze einen Cronjob, um diese automatisiert zu erneuern:
``` bash
   sudo certbot renew --quiet
```
Mit diesen Schritten kannst du die Weltkarte sicher über einen Browser zugänglich machen! 🚀

#### 3. Nutzung
- **Automatische Karten Generierung:** Die Karte wird von allein aktualisiert, sobald Spieler beitreten oder die Welt gespeichert wird.
- **Manuelle Karten Generierung:** Nutze `/dynmap`, um die Karte manuell zu erzeugen.
- **Spielerstatus:** Prüfe mit `/serverstatus` die aktuelle Leistung des Servers.
- **Zombie-Invasion:** Starte eine Zombie-Invasion mit `/invasion start <Abstand> <Schwierigkeit>`.
