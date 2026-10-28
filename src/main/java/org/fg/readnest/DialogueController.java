package org.fg.readnest;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogueController {
    @FXML
    private Button okButton;
    @FXML
    private Label dialText;

    @FXML
    protected void onOkButtonClicked() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public Label getDialText() {
        return dialText;
    }
}
