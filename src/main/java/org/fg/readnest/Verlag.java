package org.fg.readnest;

public class Verlag {

    private int verlagID;
    private String bezeichnung;

    public Verlag(int verlagID, String bezeichnung) {
        this.verlagID = verlagID;
        this.bezeichnung = bezeichnung;
    }

    public int getVerlagID() {
        return verlagID;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}