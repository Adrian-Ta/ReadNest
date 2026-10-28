package org.fg.readnest;

import java.util.ArrayList;

public class BuchManager {
    private ArrayList<Buch> buchListe;
    private ArrayList<Autor> autorListe;
    private ArrayList<Exemplar> exemplarListe;
    private ArrayList<Verlag> verlagListe;

    /** Speichert bei Konstruktion alle Daten aus der DB **/
    public BuchManager() {
        buchListe = Datenbank.getBuecher();
        autorListe = Datenbank.getAutoren();
        exemplarListe = Datenbank.getAllExemplare();
        verlagListe = Datenbank.getVerlage();
    }

    public ArrayList<Buch> getBuchListe() {
        return buchListe;
    }

    /** Sucht nach Buch nach ExemplarID **/
    public Buch getBuchByExemplarID(int id) {
        for (Exemplar e : exemplarListe) {
            if (e.getExemplarID() == id) {
                return getBuchByID(e.getBuchID());
            }
        }
        return null;
    }

    public ArrayList<Buch> suchleisteBuch(String eingabe) {
        ArrayList<Buch> buchResult = new ArrayList<>();
        String e = eingabe.trim().toLowerCase();
        if(e.isEmpty()) {
            return buchResult;
        }
        for (Buch b : buchListe) {
            if(b.getBuchName().toLowerCase().contains(e)) {
                buchResult.add(b);
            }
        }
        for (Verlag v : verlagListe) {
            if(v.getBezeichnung().toLowerCase().contains(e)) {
                for (Buch b : buchListe) {
                    if(b.getVerlagID() == v.getVerlagID()) {
                        if ( !buchResult.contains(b)) {
                            buchResult.add(b);
                        }
                    }
                }
            }
        }
        for (Autor a : autorListe) {
            if(a.getName().toLowerCase().contains(e)) {
                for (Buch b : buchListe) {
                    if(b.getAutorID() == a.getAutorID()) {
                        if ( !buchResult.contains(b)) {
                            buchResult.add(b);
                        }
                    }
                }
            }
        }
        return buchResult;
    }

    /** Sucht nach Büchern, Autoren und Verlagen mit ungefähr passender Bezeichnung → Verwendungszweck: Suchleiste **/
    public SearchResult searchFor(String eingabe) {
        ArrayList<Buch> buchResult = new ArrayList<>();
        ArrayList<Autor> autorResult = new ArrayList<>();
        ArrayList<Verlag> verlagResult = new ArrayList<>();
        String e = eingabe.trim().toLowerCase();
        if(e.isEmpty()) {
            return new SearchResult(buchResult, autorResult, verlagResult);
        }
        for (Buch b : buchListe) {
            if(b.getBuchName().toLowerCase().contains(e)) {
                buchResult.add(b);
            }
        }
        for (Autor a : autorListe) {
            if(a.getName().toLowerCase().contains(e)) {
                autorResult.add(a);
            }
        }
        for (Verlag v : verlagListe) {
            if(v.getBezeichnung().toLowerCase().contains(e)) {
                verlagResult.add(v);
            }
        }
        return new SearchResult(buchResult, autorResult, verlagResult);
    }

    /** Sucht Autor nach ID **/
    public Autor getAutorByID(int id) {
        for (Autor a : autorListe) {
            if (a.getAutorID() == id) {
                return a;
            }
        }
        return null;
    }

    /** Sucht Buch nach ID **/
    public Buch getBuchByID(int id) {
        for (Buch b : buchListe) {
            if (b.getBuchID() == id) {
                return b;
            }
        }
        return null;
    }

    /** Sucht Buch nach Namen **/
    public Buch getBuchByName(String name) {
        for (Buch b : buchListe) {
            if (b.getBuchName().equals(name)) {
                return b;
            }
        }
        return null;
    }

    /** Sucht Verlag nach ID **/
    public Verlag getVerlagByID(int id) {
        for (Verlag v : verlagListe) {
            if (v.getVerlagID() == id) {
                return v;
            }
        }
        return null;
    }

    public void setBuchListe(ArrayList<Buch> buchListe) {
        this.buchListe = buchListe;
    }

    public ArrayList<Autor> getAutorListe() {
        return autorListe;
    }

    public void setAutorListe(ArrayList<Autor> autorListe) {
        this.autorListe = autorListe;
    }

    public ArrayList<Exemplar> getExemplarListe() {
        return exemplarListe;
    }

    public void setExemplarListe(ArrayList<Exemplar> exemplarListe) {
        this.exemplarListe = exemplarListe;
    }

    public ArrayList<Verlag> getVerlagListe() {
        return verlagListe;
    }

    public void setVerlagListe(ArrayList<Verlag> verlagListe) {
        this.verlagListe = verlagListe;
    }

    public Exemplar getExemplarByID(int exemplarID) {
        for (Exemplar e : exemplarListe) {
            if (e.getExemplarID() == exemplarID) {
                return e;
            }
        }
        return null;
    }
}
