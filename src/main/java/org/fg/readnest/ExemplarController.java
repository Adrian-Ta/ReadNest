package org.fg.readnest;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ExemplarController {
    @FXML
    private Button ausleihenButton;
    @FXML
    private Label exemplarText;
    private int exemplarID;
    private final WidgetManager widMan = new WidgetManager();

    @FXML
    protected void onAusleihenButtonClick() {
        widMan.createAusleihen(exemplarID);
        Stage stage = (Stage) exemplarText.getScene().getWindow();
        stage.close();
    }


    public Label getExemplarText() {
        return exemplarText;
    }

    public void setExemplarID(int exemplarID) {
        this.exemplarID = exemplarID;
    }
}