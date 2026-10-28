package org.fg.readnest;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Datenbank {
    /** Erstellt Crypto-Objekt und entschlüsselt Login-Daten für DB **/
    private static final Crypto crp = new Crypto();
    private static final String dbURL = crp.getDbURL();
    private static final String user = crp.getDbUser();
    private static final String pw = crp.getDbPass();

    /** Aktueller Zeitpunkt als SQL-Text → statt NOW(), damit MySQL und SQLite funktionieren **/
    private static String jetzt() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /** Datum als SQL-Text yyyy-MM-dd (mit führenden Nullen, damit Textvergleiche in SQLite stimmen) **/
    private static String sqlDatum(Datum d) {
        return String.format("%04d-%02d-%02d", d.getJahr(), d.getMonat(), d.getTag());
    }

    /** Liest Datumsspalte → MySQL liefert DATE/DATETIME, SQLite Text, beide beginnen mit yyyy-MM-dd **/
    private static LocalDate leseDatum(ResultSet rs, String spalte) throws SQLException {
        return LocalDate.parse(rs.getString(spalte).substring(0, 10));
    }

    /** Überprüft Nutzername und Passwort mit Datenbank **/
    public static boolean checkLogin(String nutzername, String passwort) {
        String sql = "SELECT Passwort FROM Nutzer WHERE Nutzername = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nutzername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String pw = rs.getString("Passwort");
                return BCrypt.checkpw(passwort, pw);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    /** Erstellt Nutzerkonto, wenn Nutzername nicht vergeben **/
    public static DBResult createUser(String nutzername, String passwort) {
        String hashedPasswort = BCrypt.hashpw(passwort, BCrypt.gensalt());
        String sql = "INSERT INTO Nutzer (Nutzername, Passwort) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (checkIfUserExists(nutzername)) {
                System.out.println("Benutzername bereits vergeben.");
                return new DBResult(false, "Benutzername bereits vergeben.");
            }
            pstmt.setString(1, nutzername);
            pstmt.setString(2, hashedPasswort);
            int rowsAffected = pstmt.executeUpdate();
            return new DBResult(rowsAffected > 0, "Benutzer erfolgreich angelegt.");
        } catch (SQLException e) {
            System.out.println("Verbindung zur Datenbank fehlgeschlagen.");
        }
        return new DBResult(false, "Verbindung zur Datenbank fehlgeschlagen.");
    }

    public static boolean getHatUserDiesesBuchSchon(int buchID) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM Verleihung v
                    JOIN Exemplar e ON v.ExemplarID = e.ExemplarID
                    WHERE e.BuchID = ? AND v.NutzerID = ? AND v.BisDatum > ?
                ) AS "EXISTS";
                """;
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, buchID);
            pstmt.setInt(2, ReadNest.userManager.getNutzer().getNutzerID());
            pstmt.setString(3, jetzt());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("EXISTS");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // Sicherheitshalber, damit ein User dieses Buch bei einem Error trotzdem nicht ausleihen kann.
        return true;
    }

    /** Gibt Beschreibung nach BuchID **/
    public static String getBeschreibungByBuchID(int buchID) {
        String sql = "SELECT b.Text FROM Beschreibung b JOIN Buch b2 ON b.BuchID = b2.BuchID WHERE b.BuchID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, buchID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Text");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return "Keine Beschreibung vorhanden.";
    }

    /** Überprüft, ob der Nutzer existiert **/
    private static boolean checkIfUserExists(String nutzername) {
        String sql = "SELECT 1 FROM Nutzer WHERE Nutzername = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nutzername);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Verbindung zur Datenbank fehlgeschlagen.");
        }
        return false;
    }

    /** Erstellt einen Verlag in DB → unbedingt auf Admin-Rechte prüfen **/
    public static boolean createVerlag(String bezeichnung) {
        String sql = "INSERT INTO Verlag (bezeichnung) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bezeichnung);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    /** Erstellt neuen Autor in DB → unbedingt auf Admin-Rechte prüfen **/
    public static boolean createAutor(String name) {
        String sql = "INSERT INTO Autor (Name) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, name);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    /** Erstellt neues Buch in DB → unbedingt auf Admin-Rechte prüfen **/
    private static boolean createBuch(String buchname, String isbn, int autorID, int verlagID, Datum verDat, int seitenAnzahl) {
        String sql = "INSERT INTO Buch (Buch, AutorID, VerlagsID, Veroeffentlichungsdatum, Seitenanzahl) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, buchname);
        pstmt.setInt(2, autorID);
        pstmt.setInt(3, verlagID);
        pstmt.setString(4, sqlDatum(verDat));
        pstmt.setInt(5, seitenAnzahl);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    /** Löscht Buch aus DB → unbedingt auf Admin-Rechte prüfen **/
    private static boolean delBuch(int buchID) {
        String sql = "DELETE FROM Buch WHERE BuchID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, buchID);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /** Löscht Autor aus DB → unbedingt auf Admin-Rechte prüfen **/
    private static boolean delAutor(int autorID) {
        String sql = "DELETE FROM Autor WHERE AutorID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, autorID);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /** Löscht Verlag aus DB → unbedingt auf Admin-Rechte prüfen **/
    private static boolean delVerlag(int VerlagsID) {
        String sql = "DELETE FROM Verlag WHERE VerlagsID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, VerlagsID);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /** Storniert ausgeliehenes Buch **/
    private static boolean cancelAusleihen(int LeihID, int nutzerID) {
        String sql = "DELETE FROM Verleihung WHERE LeihID = ? AND NutzerID = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, LeihID);
        pstmt.setInt(2, nutzerID);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /** Prüft auf Admin-Rechte **/
    public static boolean isAdmin(int nutzerID) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM Administrator a
                    WHERE a.NutzerID = ?
                ) AS IsAdmin;
                """;
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nutzerID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("IsAdmin");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    /** Erstellt Nutzer aus Nutzernamen **/
    public static Nutzer getNutzer(String nutzername) {
        String sql = "SELECT NutzerID from Nutzer WHERE Nutzername = ?";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nutzername);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int nutzerID = rs.getInt("NutzerID");
                return new Nutzer(nutzerID, nutzername);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    /** Ruft alle nicht ausgeliehenen Exemplare ab **/
    public static ArrayList<Exemplar> getAllExemplareNichtAusgeliehen() {
        String sql = """
                    SELECT *
                    FROM Exemplar e
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM Verleihung v
                        WHERE v.ExemplarID = e.ExemplarID
                           AND v.BisDatum > ?
                    )
                    """;
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jetzt());
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Exemplar> eList = new ArrayList<>();
            while (rs.next()) {
                int exemplarID = rs.getInt("ExemplarID");
                int buchID = rs.getInt("BuchID");
                Exemplar e = new Exemplar(buchID, exemplarID);
                eList.add(e);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    /** Ruft alle Bücher ab **/
    public static ArrayList<Buch> getBuecher() {
        String sql = "SELECT * FROM Buch";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Buch> buecher = new ArrayList<>();
            while (rs.next()) {
                int buchID = rs.getInt("BuchID");
                String buchName = rs.getString("Buch");
                String isbn = rs.getString("ISBN");
                int AutorID = rs.getInt("AutorID");
                int VerlagID = rs.getInt("VerlagsID");
                LocalDate d = leseDatum(rs, "Veroeffentlichungsdatum");
                Datum verDat = new Datum(d.getDayOfMonth() + "." + d.getMonth() + "." + d.getYear()); //dd.MM.yyyy
                int seitenAnzahl = rs.getInt("Seitenanzahl");
                String buchdatei = rs.getString("Buchdateipfad");
                String vorschaubild = rs.getString("Vorschaubildpfad");
                Buch b = new Buch(buchID, buchName, AutorID, VerlagID, isbn, verDat, seitenAnzahl, buchdatei, vorschaubild);
                buecher.add(b);
            }
            rs.close();
            stmt.close();
            return buecher;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Ruft alle Autoren ab **/
    public static ArrayList<Autor> getAutoren() {
        String sql = "SELECT * FROM Autor";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Autor> aList = new ArrayList<>();
            while(rs.next()) {
                int autorID = rs.getInt("AutorID");
                String name = rs.getString("Name");
                Autor a = new Autor(autorID, name);
                aList.add(a);
            }
            rs.close();
            stmt.close();
            return aList;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /** Prüft, ob das Exemplar ausgeliehen ist **/
    public static boolean IsAusgeliehen(Exemplar e) {
        String sql = """
            SELECT EXISTS (
                 SELECT 1
                 FROM   Verleihung
                 WHERE  ExemplarID = ?
                   AND  Ausleihdatum  <= ?
                   AND  BisDatum  >= ?
             ) AS IstAusgeliehen""";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, e.getExemplarID());
            pstmt.setString(2, jetzt());
            pstmt.setString(3, jetzt());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("IstAusgeliehen");
            }
        } catch (SQLException e1) {
            System.out.println("Error: " + e1.getMessage());
        }
        return false;
    }

    /** Ruft alle Verlage ab **/
    public static ArrayList<Verlag> getVerlage() {
        String sql = "SELECT * FROM Verlag";
        ArrayList<Verlag> vList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int verlagsID = rs.getInt("VerlagsID");
                String bezeichnung = rs.getString("Bezeichnung");
                Verlag v = new Verlag(verlagsID, bezeichnung);
                vList.add(v);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return vList;
    }

    /** Ruft alle Exemplare ab **/
    public static ArrayList<Exemplar> getAllExemplare() {
        String sql = "SELECT * FROM Exemplar";
        ArrayList<Exemplar> eList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int exemplarID = rs.getInt("ExemplarID");
                int buchID = rs.getInt("BuchID");
                Exemplar e = new Exemplar(buchID, exemplarID);
                eList.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return eList;
    }


    /** Sucht Exemplar durch BuchID **/
    public static ArrayList<Exemplar> getExemplareByBuchID(int buchID) {
        String sql = """
        SELECT e.*
        FROM Exemplar e
        JOIN Buch b ON e.BuchID = b.BuchID
        WHERE b.BuchID = ?
    """;
        ArrayList<Exemplar> eList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, buchID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int exemplarID = rs.getInt("ExemplarID");
                Exemplar e = new Exemplar(buchID, exemplarID);
                eList.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return eList;
    }


    /** Sucht Ausleihungen durch Nutzer **/
    public static ArrayList<Ausleihung> getAusleihungenByNutzer(Nutzer n) {
        String sql = "SELECT v.LeihID, v.NutzerID, v.ExemplarID, v.Ausleihdatum, v.BisDatum " +
                "FROM Verleihung v " +
                "JOIN Nutzer nutzer ON v.NutzerID = nutzer.NutzerID " +
                "WHERE nutzer.NutzerID = ?";
        ArrayList<Ausleihung> aList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, n.getNutzerID());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int leihID = rs.getInt("LeihID");
                    int nutzerID = rs.getInt("NutzerID");
                    int exemplarID = rs.getInt("ExemplarID");
                    LocalDate lDausleihDatum = leseDatum(rs, "Ausleihdatum");
                    Datum ausleihDatum = new Datum(
                            lDausleihDatum.getDayOfMonth() + "." +
                                    lDausleihDatum.getMonthValue() + "." +
                                    lDausleihDatum.getYear()
                    );
                    LocalDate lDbisDatum = leseDatum(rs, "BisDatum");
                    Datum bisDatum = new Datum(
                            lDbisDatum.getDayOfMonth() + "." +
                                    lDbisDatum.getMonthValue() + "." +
                                    lDbisDatum.getYear()
                    );
                    if (lDbisDatum.isAfter(LocalDate.now())) {
                        Ausleihung a = new Ausleihung(leihID, nutzerID, exemplarID, ausleihDatum, bisDatum);
                        aList.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return aList;
    }


    /** Leiht Buch aus → unbedingt prüfen, ob das Exemplar schon ausgeliehen ist **/
    public static boolean ausleihen(Nutzer n, int exemplarID, Datum bisDatum) {
        String sql = "INSERT INTO Verleihung (NutzerID, ExemplarID, Ausleihdatum, BisDatum) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbURL, user, pw)) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, n.getNutzerID());
                ps.setInt(2, exemplarID);
                ps.setString(3, jetzt());
                ps.setString(4, sqlDatum(bisDatum));
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
        return true;
    }
}
