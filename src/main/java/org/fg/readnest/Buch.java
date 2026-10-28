package org.fg.readnest;

import javafx.scene.image.Image;

public class Buch {
    private int buchID;
    private String buchName;
    private int AutorID;
    private int VerlagID;
    private String isbn;
    private Datum verDat;
    private int seitenAnzahl;
    private String buchdatei;
    private String vorschaubild;
    private Image cachedVorschaubild;
    private String beschreibung;

    public Buch(int buchID, String buchName, int AutorID, int VerlagID, String isbn, Datum verDat, int seitenAnzahl, String buchdatei, String vorschaubild) {
        this.buchID = buchID;
        this.buchName = buchName;
        this.AutorID = AutorID;
        this.VerlagID = VerlagID;
        this.isbn = isbn;
        this.verDat = verDat;
        this.seitenAnzahl = seitenAnzahl;
        this.buchdatei = buchdatei;
        this.vorschaubild = vorschaubild;
        this.cachedVorschaubild = new Image(vorschaubild);
        this.beschreibung = Datenbank.getBeschreibungByBuchID(this.buchID);
    }

    public int getBuchID() {
        return buchID;
    }

    public String getBuchName() {
        return buchName;
    }

    public String getISBN() {
        return isbn;
    }

    public Datum getVerDatum() {
        return verDat;
    }

    public int getSeitenAnzahl() {
        return seitenAnzahl;
    }

    public String getBuchdatei() {
        return buchdatei;
    }

    public String getVorschaubild() {
        return vorschaubild;
    }

    public int getAutorID() {
        return AutorID;
    }

    public int getVerlagID() {
        return VerlagID;
    }

    public Image getCachedVorschaubild() {
        return cachedVorschaubild;
    }

    public String getBeschreibung() {
        return beschreibung;
    }
}