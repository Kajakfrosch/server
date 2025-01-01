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

#### 3. Nutzung
- **Automatische Karten Generierung:** Die Karte wird von allein aktualisiert, sobald Spieler beitreten oder die Welt gespeichert wird.
- **Manuelle Karten Generierung:** Nutze `/dynmap`, um die Karte manuell zu erzeugen.
- **Spielerstatus:** Prüfe mit `/serverstatus` die aktuelle Leistung des Servers.
- **Zombie-Invasion:** Starte eine Zombie-Invasion mit `/invasion start <Abstand> <Schwierigkeit>`.
