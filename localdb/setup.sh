#!/usr/bin/env bash
# Setzt die lokale SQLite-Datenbank localdb/readnest.db (neu) auf: Schema, Beispieldaten, Beispiel-PDFs und -Cover.
# Voraussetzung: sqlite3 (auf macOS vorinstalliert), Java, Maven-Abhängigkeiten im ~/.m2-Cache.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
DB="$DIR/readnest.db"
MEDIEN="$DIR/medien"
M2="$HOME/.m2/repository"
CP="$M2/org/apache/pdfbox/pdfbox/3.0.5/pdfbox-3.0.5.jar:$M2/org/apache/pdfbox/pdfbox-io/3.0.5/pdfbox-io-3.0.5.jar:$M2/org/apache/pdfbox/fontbox/3.0.5/fontbox-3.0.5.jar:$M2/commons-logging/commons-logging/1.3.5/commons-logging-1.3.5.jar"

echo "Erzeuge Beispielmedien …"
rm -rf "$MEDIEN"
java -Djava.awt.headless=true -cp "$CP" "$DIR/MedienGenerator.java" "$MEDIEN"

echo "Lege Datenbank an …"
rm -f "$DB"
sqlite3 "$DB" < "$DIR/schema.sql"
sed "s|{{MEDIEN}}|file://$MEDIEN|g" "$DIR/seed.sql" | sqlite3 "$DB"

cat <<EOF
Fertig: $DB
Starten mit lokaler DB:
  export READNEST_DB_URL='jdbc:sqlite:localdb/readnest.db?foreign_keys=true'
  mvn clean javafx:run
Logins: admin/admin (Administrator), leser/leser
EOF
