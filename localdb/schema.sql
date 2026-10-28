-- ReadNest – Schema für die lokale SQLite-Datenbank (localdb/readnest.db)
-- Abgeleitet aus den SQL-Abfragen in Datenbank.java
-- Datumswerte werden als Text gespeichert: 'yyyy-MM-dd' bzw. 'yyyy-MM-dd HH:mm:ss'

PRAGMA foreign_keys = ON;

CREATE TABLE Nutzer (
    NutzerID            INTEGER PRIMARY KEY AUTOINCREMENT,
    Nutzername          TEXT NOT NULL UNIQUE,
    Passwort            TEXT NOT NULL,
    Registrierungsdatum TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE Administrator (
    NutzerID INTEGER PRIMARY KEY REFERENCES Nutzer (NutzerID) ON DELETE CASCADE
);

CREATE TABLE Autor (
    AutorID INTEGER PRIMARY KEY AUTOINCREMENT,
    Name    TEXT NOT NULL
);

CREATE TABLE Verlag (
    VerlagsID   INTEGER PRIMARY KEY AUTOINCREMENT,
    Bezeichnung TEXT NOT NULL
);

CREATE TABLE Buch (
    BuchID                  INTEGER PRIMARY KEY AUTOINCREMENT,
    Buch                    TEXT    NOT NULL,
    ISBN                    TEXT,
    AutorID                 INTEGER NOT NULL REFERENCES Autor (AutorID),
    VerlagsID               INTEGER NOT NULL REFERENCES Verlag (VerlagsID),
    Veroeffentlichungsdatum TEXT    NOT NULL,
    Seitenanzahl            INTEGER,
    -- URLs (http:// oder file://), werden per URL.openStream bzw. new Image(url) geladen
    Buchdateipfad           TEXT    NOT NULL,
    Vorschaubildpfad        TEXT    NOT NULL
);

CREATE TABLE Beschreibung (
    BuchID INTEGER PRIMARY KEY REFERENCES Buch (BuchID) ON DELETE CASCADE,
    Text   TEXT NOT NULL
);

CREATE TABLE Exemplar (
    ExemplarID INTEGER PRIMARY KEY AUTOINCREMENT,
    BuchID     INTEGER NOT NULL REFERENCES Buch (BuchID) ON DELETE CASCADE
);

CREATE TABLE Verleihung (
    LeihID       INTEGER PRIMARY KEY AUTOINCREMENT,
    NutzerID     INTEGER NOT NULL REFERENCES Nutzer (NutzerID) ON DELETE CASCADE,
    ExemplarID   INTEGER NOT NULL REFERENCES Exemplar (ExemplarID) ON DELETE CASCADE,
    Ausleihdatum TEXT    NOT NULL,
    BisDatum     TEXT    NOT NULL
);
