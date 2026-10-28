package org.fg.readnest;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AusleihenController {
    @FXML
    private DatePicker datePicker;
    private final WidgetManager widMan = new WidgetManager();
    private int exemplarID;

    @FXML
    protected void onAusleihenButtonClick() {
        if(datePicker.getValue() == null) {
            widMan.createDialogue("Ausleihung", "Geben Sie ein Datum ein.");
            return;
        }
        if(Datenbank.getHatUserDiesesBuchSchon(ReadNest.buchManager.getBuchByExemplarID(this.exemplarID).getBuchID())) {
            widMan.createDialogue("Ausleihung", "Sie können nur ein Exemplar eines Buches ausleihen.");
            return;
        }
        LocalDate date = datePicker.getValue();
        String sd = date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
        Datum dAusleihung = new Datum(sd);
        Datum dJetzt = new Datum();
        if (dJetzt.getTimeInMillis() > dAusleihung.getTimeInMillis()) {
            widMan.createDialogue("Ausleihung", "Das Datum darf nicht in der Vergangenheit liegen.");
            return;
        }
        if (dJetzt.berechneTage(dAusleihung) > 14) {
            widMan.createDialogue("Ausleihung", "Die maximale Ausleihzeit ist 14 Tage.");
            return;
        }
        if(Datenbank.IsAusgeliehen(ReadNest.buchManager.getExemplarByID(this.exemplarID))) {
            widMan.createDialogue("Ausleihung", "Buch ist bereits ausgeliehen.");
            return;
        }
        if(Datenbank.ausleihen(ReadNest.userManager.getNutzer(), this.exemplarID, dAusleihung)) {
            widMan.createDialogue("Ausleihung", "Buch wurde ausgeliehen.");
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.close();
            ReadNest.rnc.onRefreshButtonClick();
            return;
        }
        widMan.createDialogue("Ausleihung", "Buch konnte nicht ausgeliehen werden.");
    }

    public void setExemplarID(int exemplarID) {
        this.exemplarID = exemplarID;
    }
}
