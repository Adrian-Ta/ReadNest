package org.fg.readnest;

public class Ausleihung {

    private int leihID;
    private int nutzerID;
    private int exemplarID;
    private Datum ausleihDatum;
    private Datum bisDatum;

    public Ausleihung(int leihID, int nutzerID, int exemplarID, Datum ausleihDatum, Datum bisDatum) {
        this.leihID = leihID;
        this.nutzerID = nutzerID;
        this.exemplarID = exemplarID;
        this.ausleihDatum = ausleihDatum;
        this.bisDatum = bisDatum;
    }

    public int getLeihID() {
        return leihID;
    }

    public int getNutzerID() {
        return nutzerID;
    }

    public int getExemplarID() {
        return exemplarID;
    }

    public Datum getAusleihDatum() {
        return ausleihDatum;
    }

    public Datum getBisDatum() {
        return bisDatum;
    }
}