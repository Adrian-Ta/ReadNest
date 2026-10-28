package org.fg.readnest;

import java.util.ArrayList;

public class UserManager {
    private Nutzer nutzer;
    private ArrayList<Ausleihung> meineAusleihungen;
    private boolean isAdmin;

    public Nutzer getNutzer() {
        return nutzer;
    }

    /** Sollte vor jeder Aktion überprüft werden, die Adminrechten bedarf **/
    public boolean serverValidateIsAdmin() {
        setAdmin(Datenbank.isAdmin(getNutzer().getNutzerID()));
        return isAdmin;
    }

    public ArrayList<Ausleihung> getMeineAusleihungen() {
        return meineAusleihungen;
    }

    /** Nutzer wird gesetzt und alle Ausleihungen werden abgerufen **/
    public void setNutzer(Nutzer nutzer) {
        this.nutzer = nutzer;
        meineAusleihungen = Datenbank.getAusleihungenByNutzer(nutzer);
    }

    /** Aktualisiert Ausleihungen → unbedingt nach jeder neuen Ausleihung oder Stornierung ausführen **/
    public void refreshAusleihungen() {
        meineAusleihungen = Datenbank.getAusleihungenByNutzer(nutzer);
    }


    /** Nur zum Nutzen für UI. Adminaktionen erfordern erneute Validation **/
    public boolean isAdmin() {
        return isAdmin;
    }
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
