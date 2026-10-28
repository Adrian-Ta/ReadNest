<p align="center">
  <img src="src/main/resources/org/fg/readnest/readnestlogo.png" alt="ReadNest Logo" width="120">
</p>

<h1 align="center">ReadNest</h1>
<p align="center">Online-Bibliothek als JavaFX-Desktop-App · Version 1.1</p>

---

Mit ReadNest leiht man Bücher digital aus und liest sie direkt in der App. Jedes Buch hat eine begrenzte Anzahl an Exemplaren. Ist ein Exemplar verliehen, kann es bis zum Rückgabedatum niemand anderes ausleihen, genau wie in einer echten Bibliothek.

## Funktionen

- **Registrieren & Anmelden**: Passwörter werden mit BCrypt gehasht gespeichert.
- **Katalog & Suche**: alle Bücher mit Cover; die Suche findet Titel, Autoren und Verlage.
- **Detailansicht**: Beschreibung, Autor, Verlag und die verfügbaren Exemplare eines Buchs.
- **Ausleihen**: ein freies Exemplar ausleihen; das Rückgabedatum wählt man selbst, höchstens 14 Tage im Voraus. Pro Buch kann man immer nur ein Exemplar gleichzeitig ausleihen.
- **Meine Ausleihungen**: aktuell ausgeliehene Bücher mit Rückgabedatum, durchsuchbar.
- **Reader**: ausgeliehene Bücher als PDF lesen, mit Seitenwechsel und Sprung zu einer Seitenzahl.
- **Adminübersicht**: Tabellen aller Bücher, Autoren und Verlage (nur für Administratoren).

## Voraussetzungen

- **Java 23** (JDK)
- **Maven 3.9+**, z. B. das in IntelliJ IDEA mitgelieferte
- **sqlite3** für die lokale Datenbank (auf macOS vorinstalliert)

> `./mvnw` funktioniert derzeit nicht, weil der Ordner `.mvn/wrapper/` im Repository fehlt. Nutze stattdessen `mvn` oder das Maven aus IntelliJ:
> `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"`

## Schnellstart (lokale Datenbank)

Zum Ausprobieren und Entwickeln nutzt ReadNest eine lokale **SQLite**-Datenbank: eine einzige Datei auf deinem Rechner, ohne Server und ohne Internet.

**1. Datenbank mit Beispieldaten anlegen** (einmalig):

```bash
./localdb/setup.sh
```

Das erzeugt `localdb/readnest.db` mit 6 Beispielbüchern samt Covern und PDFs.

**2. App starten** (aus dem Projektordner):

```bash
export READNEST_DB_URL='jdbc:sqlite:localdb/readnest.db?foreign_keys=true'
mvn clean javafx:run
```

**3. Anmelden** mit einem der Testkonten oder ein eigenes Konto registrieren:

| Benutzer | Passwort | Rolle |
|---|---|---|
| `admin` | `admin` | Administrator |
| `leser` | `leser` | Leser (hat bereits ein Buch ausgeliehen) |

Deine Daten bleiben in `localdb/readnest.db` gespeichert, auch nach einem Neustart. `./localdb/setup.sh` setzt die Datenbank wieder auf die Beispieldaten zurück und **löscht dabei alle Änderungen**.

### In IntelliJ IDEA

1. Projekt öffnen (als Maven-Projekt).
2. *Run → Edit Configurations…* → bei *Environment variables* eintragen:
   `READNEST_DB_URL=jdbc:sqlite:localdb/readnest.db?foreign_keys=true`
3. Maven-Ziel `clean javafx:run` ausführen.

## Datenbanken

ReadNest kann mit zwei Datenbanken arbeiten:

| | Lokal | Team |
|---|---|---|
| Typ | SQLite (`localdb/readnest.db`) | MySQL-Server |
| Aktiv, wenn … | `READNEST_DB_URL` gesetzt ist | `READNEST_DB_URL` **nicht** gesetzt ist |
| Zugangsdaten | keine nötig | verschlüsselt in `config.enc` |
| Gedacht für | Testen & Entwickeln | gemeinsame Daten des Teams |

> **Hinweis:** `config.enc` mit den Zugangsdaten der Team-Datenbank ist in diesem öffentlichen Repository nicht enthalten. Setze daher immer `READNEST_DB_URL` und nutze die lokale Datenbank (siehe [Schnellstart](#schnellstart-lokale-datenbank)).

Das Schema steht in [`localdb/schema.sql`](localdb/schema.sql). Tabellen: `Nutzer`, `Administrator`, `Autor`, `Verlag`, `Buch`, `Beschreibung`, `Exemplar`, `Verleihung`.

Um die lokale Datenbank anzusehen: `sqlite3 localdb/readnest.db`, oder die Datei z. B. mit *DB Browser for SQLite* öffnen.

## Projektstruktur

```
ReadNest/
├── pom.xml                         Maven-Konfiguration & Abhängigkeiten
├── localdb/                        lokale SQLite-Datenbank
│   ├── setup.sh                    Datenbank (neu) aufsetzen
│   ├── schema.sql                  Tabellen
│   ├── seed.sql                    Beispieldaten
│   └── MedienGenerator.java        erzeugt Beispiel-Cover & -PDFs
└── src/main/
    ├── java/org/fg/readnest/
    │   ├── ReadNest.java           Einstiegspunkt (Splashscreen → Login)
    │   ├── Datenbank.java          alle SQL-Abfragen
    │   ├── Crypto.java             wählt die Datenbank / entschlüsselt config.enc
    │   ├── BuchManager.java        Bücher, Autoren, Verlage, Exemplare im Speicher
    │   ├── UserManager.java        angemeldeter Nutzer & seine Ausleihungen
    │   ├── WidgetManager.java      öffnet Fenster und lädt Widgets
    │   ├── *Controller.java        Controller der FXML-Ansichten
    │   └── Buch, Autor, Verlag …   Datenklassen
    └── resources/org/fg/readnest/
        ├── *.fxml                  Oberflächen (Scene Builder)
        ├── primer-light.css        Stylesheet
        └── *.png                   Logo & Bilder
```

## Technologien

- **Java 23** & **JavaFX 23** (FXML, ControlsFX, BootstrapFX, Ikonli)
- **MySQL Connector/J** & **SQLite JDBC** für die Datenbanken
- **Apache PDFBox** zum Anzeigen der PDFs
- **Spring Security Crypto** für BCrypt-Passwort-Hashes
- **Maven** als Build-Tool

## Team

- Marlon Gölzhäuser
- Adrian Tajik
- Gaby Ghmmed
- Nils Humer

---

Weitere Dokumentation: [`AENDERUNGEN.md`](AENDERUNGEN.md) (Umstellung auf die lokale Datenbank) · [`CLAUDE.md`](CLAUDE.md) (technischer Überblick)
