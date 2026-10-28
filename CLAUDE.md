# CLAUDE.md

ReadNest ist eine JavaFX-Desktop-App für eine digitale Bibliothek: Nutzer registrieren sich, leihen Exemplare von Büchern aus und lesen sie als PDF im eingebauten Reader. Administratoren pflegen Autoren, Verlage und Bücher. Code, Kommentare und UI sind auf Deutsch – neue Bezeichner und Kommentare ebenfalls auf Deutsch schreiben.

## Bauen & Starten

- Java 23 (Compiler-Level 23 in `pom.xml`; der Code nutzt unbenannte Lambda-Parameter `_ ->`).
- Maven: `./mvnw` funktioniert **nicht**, weil `.mvn/wrapper/` im Repo fehlt. Stattdessen das Maven aus IntelliJ verwenden:
  ```bash
  "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" clean javafx:run
  ```
- Einstiegspunkt: `org.fg.readnest/org.fg.readnest.ReadNest` (`Launcher` ruft nur `ReadNest.main` auf).
- JavaFX muss ≥ 21.0.2 sein. Mit 17.0.6 stürzt die App auf aktuellem macOS beim Öffnen des Fensters ab (`invalid NSTrackingRectTag`). Aktuell: 23.0.1.
- Es gibt keine Tests (`src/test` existiert nicht).

## Datenbank

Zugriff über JDBC, ausschließlich in `Datenbank.java` (statische Methoden, pro Aufruf eine neue `Connection`, `PreparedStatement`s). Zwei Datenbanken werden unterstützt:
- **MySQL**: die gemeinsame Team-Datenbank (Treiber `mysql-connector-j`)
- **SQLite**: lokale Entwicklung, Datei `localdb/readnest.db` (Treiber `sqlite-jdbc`)

Zugangsdaten kommen aus `Crypto`:
1. Ist `READNEST_DB_URL` gesetzt, wird diese URL verwendet (optional `READNEST_DB_USER`/`READNEST_DB_PASS`; SQLite braucht keine).
2. Sonst wird `src/main/resources/org/fg/readnest/config.enc` gelesen (AES-verschlüsselt, Format `url;user;pass`) – das ist die Team-Datenbank (MySQL). Diese Datei nicht verändern.

Lokale SQLite-Datenbank:
```bash
./localdb/setup.sh   # baut localdb/readnest.db komplett neu auf, inkl. Beispieldaten
export READNEST_DB_URL='jdbc:sqlite:localdb/readnest.db?foreign_keys=true'
sqlite3 localdb/readnest.db   # direkt reinschauen
```
Der Pfad ist relativ zum Arbeitsverzeichnis, also die App aus dem Projektordner starten. `foreign_keys=true` ist nötig, sonst ignoriert SQLite die Fremdschlüssel (z. B. das kaskadierende Löschen).
Test-Logins: `admin`/`admin` (Administrator), `leser`/`leser`.

**SQL muss auf MySQL und SQLite laufen:**
- Kein `NOW()`, sondern den Zeitpunkt als Parameter übergeben: `jetzt()` liefert `yyyy-MM-dd HH:mm:ss`.
- Datumswerte mit `sqlDatum(Datum)` schreiben (`yyyy-MM-dd` mit führenden Nullen, weil SQLite Datumswerte als Text vergleicht).
- Datumswerte mit `leseDatum(rs, spalte)` lesen, nicht mit `rs.getDate()`.
- Keine anderen MySQL- oder SQLite-spezifischen Funktionen (`CONCAT`, `INTERVAL`, `datetime()` …) in `Datenbank.java`.

Schema in `localdb/schema.sql` (SQLite-Syntax). Tabellen: `Nutzer`, `Administrator`, `Autor`, `Verlag`, `Buch`, `Beschreibung`, `Exemplar`, `Verleihung`. Spaltennamen genau so verwenden, wie sie in `Datenbank.java` stehen (z. B. `VerlagsID`, `Veroeffentlichungsdatum`, `BisDatum`). Wenn sich das Schema ändert, `localdb/schema.sql` und `localdb/seed.sql` mitpflegen.

`Buch.Buchdateipfad` und `Buch.Vorschaubildpfad` sind **URLs** (http(s) oder `file://`), keine Dateipfade – sie werden per `URL.openStream()` bzw. `new Image(url)` geladen.

## Architektur

- `ReadNest` (Application): zeigt den Splashscreen und lädt in einem Hintergrund-`Task` die globalen Manager. Danach öffnet sich das Login (`hello-view.fxml`).
- Globale Zustände als statische Felder in `ReadNest`: `userManager`, `buchManager`, `rnc` (Hauptfenster-Controller).
- `BuchManager`: lädt beim Start **alle** Bücher, Autoren, Verlage und Exemplare in Listen und bietet Suche/Lookup darauf. Neue Daten in der DB erscheinen erst nach einem Neuladen.
- `UserManager`: eingeloggter `Nutzer`, seine aktiven Ausleihungen, Admin-Flag. `isAdmin()` ist nur für die UI – vor Admin-Aktionen immer `serverValidateIsAdmin()` aufrufen.
- `WidgetManager`: zentrale Stelle zum Laden von FXML-Fenstern und Widgets (`openNewForm`, `createDialogue`, `loadBuchWidget`, `createReader` …).
- Modelle: `Buch`, `Autor`, `Verlag`, `Exemplar`, `Ausleihung`, `Nutzer`, `Datum` (Wrapper um `Calendar`, Format `dd.MM.yyyy`), `DBResult`, `SearchResult` (Records).
- Passwörter werden mit BCrypt (`spring-security-crypto`) gehasht.

FXML → Controller (alle in `src/main/resources/org/fg/readnest/`):

| FXML | Controller |
|---|---|
| `splashscreen.fxml`, `hello-view.fxml` | `LoginController` |
| `readnestMain.fxml` | `ReadNestController` (Hauptfenster: Katalog, Suche, meine Ausleihen) |
| `details.fxml` | `DetailsController` |
| `exemplarWidget.fxml` | `ExemplarController` |
| `buchWidget.fxml` | `BuchWidgetController` |
| `ausleihen.fxml` | `AusleihenController` |
| `reader.fxml` | `ReaderController` (PDF-Anzeige mit PDFBox) |
| `admin-view.fxml` | `AdminController` |
| `dialogue.fxml` | `DialogueController` |

Neue FXML-Controller müssen im Paket `org.fg.readnest` liegen (das Paket ist in `module-info.java` für `javafx.fxml` geöffnet). Neue Bibliotheken brauchen ein `requires` in `module-info.java`.

## Konventionen

- Methodenkommentare im Stil `/** Beschreibung **/` direkt über der Methode.
- DB-Fehler werden mit `System.out.println("Error: " + e.getMessage())` geloggt und mit einem Default-Wert beantwortet, statt Exceptions weiterzuwerfen.
- Styling über `primer-light.css`.

## Bekannte Probleme

- `Datenbank.getBuecher()` baut das Datum mit `d.getMonth()` (Monatsname statt Zahl). Das Parsen schlägt dann stillschweigend fehl, und alle Veröffentlichungsdaten zeigen das heutige Datum. Korrekt wäre `getMonthValue()`.
- `Datenbank.getAllExemplareNichtAusgeliehen()` gibt immer `null` zurück.
- Der AES-Schlüssel steht im Klartext in `Crypto.java`, deshalb schützt die Verschlüsselung von `config.enc` die DB-Zugangsdaten nicht wirklich.
- `target/` ist im Git-Repo eingecheckt.
