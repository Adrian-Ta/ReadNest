package org.fg.readnest;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class BuchWidgetController {
    @FXML
    private ImageView buchIcon;
    @FXML
    private Label buchTitle;
    @FXML
    private Label buchAutor;
    @FXML
    private Button ausleihenButton;
    @FXML
    private Label bisDatText;
    private boolean inKatalog;
    private final WidgetManager widMan = new WidgetManager();
    private ReadNestController readNestController;

    /** Ausleihen gedrückt **/
    @FXML
    protected void onAusleihenButtonClick() {
        if(inKatalog) {
            widMan.loadDetailuebersicht(ReadNest.buchManager.getBuchByName(buchTitle.getText()));

        } else {
            widMan.createReader(ReadNest.buchManager.getBuchByName(getBuchTitle().getText()));
        }
    }

    public ImageView getBuchIcon() {
        return buchIcon;
    }

    public Label getBuchTitle() {
        return buchTitle;
    }

    public Label getBuchAutor() {
        return buchAutor;
    }

    public Button getAusleihenButton() {
        return ausleihenButton;
    }

    public Label getBisDatText() {
        return bisDatText;
    }

    /** Ob's im Katalog ist, um Button Text und BisDatText zu verändern **/
    public void setInKatalog(boolean inKatalog) {
        this.inKatalog = inKatalog;
        bisDatText.setVisible(!inKatalog);
        if(inKatalog) {
            ausleihenButton.setText("Ansehen");
        } else {
            ausleihenButton.setText("Lesen");
        }
    }

    public boolean isInKatalog() {
        return inKatalog;
    }

    public void setReadNestController(ReadNestController readNestController) {
        this.readNestController = readNestController;
    }
}
