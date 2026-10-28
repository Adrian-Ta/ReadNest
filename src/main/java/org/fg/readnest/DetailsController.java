package org.fg.readnest;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class DetailsController {
    @FXML
    private VBox exemplarBox;
    @FXML
    private ImageView bookIcon;
    @FXML
    private Label bookTitle;
    @FXML
    private Label bookAutor;
    @FXML
    private Label bookVerlag;
    private final WidgetManager widMan = new WidgetManager();
    @FXML
    private Label beschreibungText;

    @FXML
    private void initialize() {
        Platform.runLater(this::fillExemplarBox);
    }

    public void fillExemplarBox() {
        ArrayList<Exemplar> exemplarArrayList = Datenbank.getExemplareByBuchID(ReadNest.buchManager.getBuchByName(bookTitle.getText()).getBuchID());
        for (Exemplar exemplar : exemplarArrayList) {
            if(!Datenbank.IsAusgeliehen(exemplar)) {
                Node n = widMan.loadExemplarWidget(exemplar.getExemplarID());
                if (exemplarBox != null) {
                    exemplarBox.getChildren().add(n);
                }
            }
        }
        if (exemplarBox != null && exemplarBox.getChildren().isEmpty()) {
            Label l = new Label("Keine Exemplare vorhanden.");
            exemplarBox.getChildren().add(l);
        }
    }

    public ImageView getBookIcon() {
        return bookIcon;
    }

    public Label getBookTitle() {
        return bookTitle;
    }

    public Label getBookAutor() {
        return bookAutor;
    }

    public Label getBookVerlag() {
        return bookVerlag;
    }

    public Label getBeschreibungText() {
        return beschreibungText;
    }
}