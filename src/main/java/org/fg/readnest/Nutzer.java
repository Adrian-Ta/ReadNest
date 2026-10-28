package org.fg.readnest;

public class Nutzer {
    private int NutzerID;
    private String nutzername;

    public Nutzer(int NutzerID, String nutzername) {
        this.NutzerID = NutzerID;
        this.nutzername = nutzername;
    }

    public int getNutzerID() {
        return NutzerID;
    }

    public String getNutzername() {
        return nutzername;
    }
}
