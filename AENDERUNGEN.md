# Änderungen: App lauffähig gemacht & lokale SQLite-Datenbank

Stand: 28.10.2026

## Kurzfassung

1. **JavaFX von 17.0.6 auf 23.0.1 aktualisiert**: Die App ist auf aktuellem macOS beim Start abgestürzt.
2. **Lokale SQLite-Datenbank mit Beispieldaten**: Man kann ReadNest jetzt ohne Zugang zur gemeinsamen Datenbank entwickeln und testen. Die ganze Datenbank ist eine Datei: `localdb/readnest.db`.
3. **`Datenbank.java` funktioniert mit MySQL und SQLite**: Ein paar MySQL-spezifische Stellen sind durch neutrale ersetzt. Die Team-Datenbank (MySQL) funktioniert weiterhin.
4. **`Crypto.java` liest optional Umgebungsvariablen**: Darüber wählt man die lokale Datenbank. Ohne diese Variablen verhält sich die App genau wie vorher.

Controller, FXML-Dateien und die übrige Logik habe ich nicht verändert.

---

## 1. JavaFX-Update (`pom.xml`)

**Problem:** Beim Öffnen des ersten Fensters ist die App sofort abgestürzt:

```
NSInternalInconsistencyException: '0x0 is an invalid NSTrackingRectTag'
```

Das ist ein bekannter Fehler in JavaFX 17.0.6 auf neueren macOS-Versionen, behoben ab JavaFX 21.0.2.

**Änderung:** Die vier JavaFX-Abhängigkeiten (`javafx-controls`, `javafx-fxml`, `javafx-web`, `javafx-swing`) stehen jetzt auf `23.0.1` statt `17.0.6`. Die FXML-Dateien waren ohnehin schon mit JavaFX 23.0.1 erstellt worden (Scene Builder), nur `readnestMain.fxml` mit 24.0.1. Die Version passt jetzt also auch zu den FXML-Dateien.

## 2. Lokale SQLite-Datenbank (Ordner `localdb/`)

Bisher lief die App nur mit der gemeinsamen MySQL-Datenbank aus `config.enc`. Ist die nicht erreichbar, bricht die Verbindung ab (`Communications link failure`) und die App zeigt keine Daten.

Lokal nutzen wir jetzt **SQLite**. Dafür muss kein Datenbankserver installiert sein oder laufen, die Datenbank ist einfach die Datei `localdb/readnest.db`. Der Treiber `org.xerial:sqlite-jdbc` ist in `pom.xml` eingetragen, dazu `requires org.xerial.sqlitejdbc;` in `module-info.java`.

Ein vollständiges Schema gab es nicht. Das alte `readnest.sql` aus der Git-Historie enthielt nur die Tabelle `nutzer`. Das neue Schema habe ich deshalb aus allen SQL-Abfragen in `Datenbank.java` abgeleitet.

| Datei | Inhalt |
|---|---|
| `localdb/schema.sql` | Alle 8 Tabellen: `Nutzer`, `Administrator`, `Autor`, `Verlag`, `Buch`, `Beschreibung`, `Exemplar`, `Verleihung`. Mit Fremdschlüsseln; wird ein Buch gelöscht, verschwinden auch seine Exemplare, die Beschreibung und die Ausleihen. |
| `localdb/seed.sql` | Beispieldaten: 2 Nutzer, 5 Autoren, 3 (erfundene) Verlage, 6 Bücher mit Beschreibung, je 2 Exemplare, 2 Ausleihen. ISBNs sind Platzhalter. |
| `localdb/MedienGenerator.java` | Erzeugt pro Buch ein Cover (PNG) und ein 5-seitiges Platzhalter-PDF für den Reader. |
| `localdb/setup.sh` | Baut alles neu auf: Medien erzeugen, `readnest.db` neu anlegen, Schema und Daten laden. |
| `localdb/.gitignore` | `readnest.db` und `medien/` werden nicht eingecheckt, jede:r erzeugt sie lokal mit `setup.sh`. |

**Test-Logins:**

| Benutzer | Passwort | Rolle |
|---|---|---|
| `admin` | `admin` | Administrator |
| `leser` | `leser` | normaler Nutzer, hat „Effi Briest“ gerade ausgeliehen |

Hinweis: Covers und PDFs werden als `file://`-URLs gespeichert, die auf den lokalen `medien`-Ordner zeigen. Die App lädt diese Felder über `URL.openStream()` bzw. `new Image(url)`, deshalb funktioniert das ohne Codeänderung.

## 3. `Datenbank.java`: MySQL- und SQLite-kompatibel

SQLite kennt einige MySQL-Funktionen nicht und speichert Datumswerte als Text. Diese Stellen habe ich angepasst:

| Vorher | Nachher | Warum |
|---|---|---|
| `NOW()` im SQL (4 Abfragen) | Parameter `?`, gefüllt mit `jetzt()` | SQLite hat kein `NOW()` |
| Datum als `"2026-11-7"` per `setObject` | `sqlDatum(datum)` → `"2026-11-07"` | SQLite vergleicht Datumswerte als Text, dafür braucht es führende Nullen |
| `rs.getDate("…").toLocalDate()` | `leseDatum(rs, "…")` | Der SQLite-Treiber kann reinen Datumstext nicht mit `getDate()` lesen |

Betroffen sind `getHatUserDiesesBuchSchon`, `createBuch`, `getAllExemplareNichtAusgeliehen`, `getBuecher`, `IsAusgeliehen`, `getAusleihungenByNutzer` und `ausleihen`. Die drei kleinen Hilfsmethoden stehen oben in `Datenbank.java`. Alle Änderungen funktionieren auch mit MySQL.

**Getestet mit SQLite:** Login (richtig/falsch), Admin-Prüfung, Autoren/Verlage/Exemplare/Beschreibung laden, aktive Ausleihen, Ausleihstatus, neues Buch ausleihen, Registrierung inkl. doppeltem Benutzernamen. Die App startet und lädt die Bücher.

## 4. Umgebungsvariablen in `Crypto.java`

Am Anfang des Konstruktors steht jetzt:

```java
/** Lokale DB: Umgebungsvariablen haben Vorrang vor config.enc (siehe localdb/setup.sh) **/
if (System.getenv("READNEST_DB_URL") != null) {
    dbURL = System.getenv("READNEST_DB_URL");
    dbUser = System.getenv("READNEST_DB_USER");
    dbPass = System.getenv("READNEST_DB_PASS");
    return;
}
```

- **`READNEST_DB_URL` gesetzt:** Die App verbindet sich mit dieser Datenbank. Für SQLite reicht die URL, Benutzer und Passwort braucht es nicht.
- **Nicht gesetzt:** wie bisher, `config.enc` wird gelesen (Team-Datenbank). Die Datei selbst ist unverändert.

## So benutzt du die lokale Datenbank

Datenbank aufsetzen (geht beliebig oft, setzt alles auf den Ausgangszustand zurück):

```bash
./localdb/setup.sh
```

App mit lokaler Datenbank starten (aus dem Projektordner):

```bash
export READNEST_DB_URL='jdbc:sqlite:localdb/readnest.db?foreign_keys=true'
"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" clean javafx:run
```

**In IntelliJ:** Run-Konfiguration öffnen → *Environment variables* → `READNEST_DB_URL=jdbc:sqlite:localdb/readnest.db?foreign_keys=true` eintragen. Das Arbeitsverzeichnis muss der Projektordner sein (Standard).

**Reinschauen:** `sqlite3 localdb/readnest.db`, oder die Datei in DB Browser for SQLite, DBeaver oder dem Database-Tool von IntelliJ öffnen.

## Außerdem neu

- **`CLAUDE.md`**: Projektüberblick für Claude Code (Bauen, Datenbank, Architektur, Konventionen, bekannte Probleme).

## Gefunden, aber nicht geändert

- **`./mvnw` funktioniert nicht**, weil der Ordner `.mvn/wrapper/` im Repo fehlt. Workaround: das Maven aus IntelliJ verwenden (siehe oben).
- **Veröffentlichungsdatum falsch:** `Datenbank.getBuecher()` nutzt `d.getMonth()` (z. B. „MARCH“) statt `d.getMonthValue()`. Das Datum kann so nicht gelesen werden, und alle Bücher zeigen das heutige Datum.
- **`getAllExemplareNichtAusgeliehen()`** gibt immer `null` zurück, obwohl die Liste gefüllt wird.
- **Schlüssel im Klartext:** Der AES-Schlüssel für `config.enc` steht direkt in `Crypto.java`. Wer das Repo hat, kann die Zugangsdaten der gemeinsamen Datenbank also entschlüsseln.
