package org.fg.readnest;

public class Exemplar {
    private int buchID;
    private int exemplarID;

    public Exemplar(int buchID, int exemplarID) {
        this.buchID = buchID;
        this.exemplarID = exemplarID;
    }

    public int getBuchID() {
        return buchID;
    }

    public int getExemplarID() {
        return exemplarID;
    }
}