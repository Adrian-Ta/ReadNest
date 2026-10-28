package org.fg.readnest;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ReadNestController {

    @FXML
    private TilePane buchPane;
    @FXML
    private Label uNtext;
    @FXML
    private Label uIcon;
    @FXML
    private TilePane ausleihBox;
    private final WidgetManager widMan = new WidgetManager();
    @FXML
    private TextField suchfeld;
    @FXML
    private Button adminButton;
    @FXML
    private TextField ausleihSearchBar;

    @FXML
    protected void onAusleihBarEnter() {
        ausleihBox.getChildren().clear();
        if (ausleihBox != null) {
            ausleihBox.setAlignment(Pos.TOP_LEFT);
        }
        String suchbegriff = ausleihSearchBar.getText().trim().toLowerCase();
        Platform.runLater(() -> {
            ReadNest.userManager.refreshAusleihungen();
            for(Ausleihung a : ReadNest.userManager.getMeineAusleihungen()) {
                Buch b = ReadNest.buchManager.getBuchByExemplarID(a.getExemplarID());
                if(b.getBuchName().toLowerCase().contains(suchbegriff)) {
                    Node n = widMan.loadBuchWidget(b.getCachedVorschaubild(), b.getBuchName(),
                            ReadNest.buchManager.getAutorByID(b.getAutorID()).getName(), a.getBisDatum(),
                            false, this);
                    ausleihBox.getChildren().add(n);
                }
            }
            if(ausleihBox != null && ausleihBox.getChildren().isEmpty()) {
                ausleihBox.setAlignment(Pos.TOP_CENTER);
                Label l = new Label("Keine Ergebnisse.");
                l.setFont(new Font("System", 16));
                ausleihBox.getChildren().add(l);
            }
        });
    }

    @FXML
    protected void onAusleihZuruecksetzenClick() {
        ausleihBox.getChildren().clear();
        Platform.runLater(this::fillAusleihBox);
        ausleihSearchBar.clear();
    }

    @FXML
    protected void onAdminButtonClick() {
        if(!ReadNest.userManager.serverValidateIsAdmin()) {
            widMan.createDialogue("ReadNest", "Hierfür müssen Sie ein Administrator sein.");
            return;
        }
        widMan.openNewForm("admin-view", "Admin", false);
    }

    /** Aktualisiert die ausgeliehenen Bücher **/
    @FXML
    protected void onRefreshButtonClick() {
        ReadNest.userManager.refreshAusleihungen();
        ausleihBox.getChildren().clear();
        Platform.runLater(this::fillAusleihBox);
    }

    /** Zurücksetzen Button gedrückt **/
   @FXML
   protected void onZuruecksetzenButtonClicked() {
       buchPane.getChildren().clear();
       suchfeld.clear();
       Platform.runLater(this::buchListeFuellen);
   }

    /** Wird ausgeführt, wenn man in der Suchleiste Enter drückt **/
    @FXML
   protected void onSuchFeldEnter() {
    buchPane.getChildren().clear();
        if (buchPane != null) {
            buchPane.setAlignment(Pos.TOP_LEFT);
        }
    String suchbegriff = suchfeld.getText().trim();
    Platform.runLater(() -> {
        for (Buch b : ReadNest.buchManager.suchleisteBuch(suchbegriff)) {
            Autor ar = ReadNest.buchManager.getAutorByID(b.getAutorID());
            if (ar == null) {
                ar = new Autor(-1, "Autor nicht gefunden");
            }
            Node buchWidget = widMan.loadBuchWidget(
                b.getCachedVorschaubild(),
                b.getBuchName(),
                ar.getName(),
                null,
                true,
                this
            );
            buchPane.getChildren().add(buchWidget);
        }
        if(buchPane != null && buchPane.getChildren().isEmpty()) {
            buchPane.setAlignment(Pos.TOP_CENTER);
            Label l = new Label("Keine Ergebnisse.");
            l.setFont(new Font("System", 16));
            buchPane.getChildren().add(l);
        }
    });
}

    /** Erstellt Profilbild und stellt Nutzername sowie Rechte im GUI ein **/
    @FXML
    private void initialize() {
        ReadNest.rnc = this;
        char fL = ReadNest.userManager.getNutzer().getNutzername().charAt(0);
        uIcon.setText(String.valueOf(fL));
        uNtext.setText("Hallo " + ReadNest.userManager.getNutzer().getNutzername());
        if(ReadNest.userManager.serverValidateIsAdmin()) {
            uNtext.setText(uNtext.getText() + " (Admin)");
        }
        Platform.runLater(this::fillAusleihBox);
        Platform.runLater(this::buchListeFuellen);
        Platform.runLater(() -> {
            boolean admin = ReadNest.userManager.serverValidateIsAdmin();
            adminButton.setDisable(!admin);
            adminButton.setVisible(admin);
        });
    }

    /** Abmelden gedrückt **/
    @FXML
    protected void onLogoutButtonPressed() {
        widMan.openNewForm("hello-view", "Login", false);
        Stage stage = (Stage) buchPane.getScene().getWindow();
        stage.close();
    }

    /** Füllt die ausgeliehenen Bücher **/
    private void fillAusleihBox() {
        for(Ausleihung a : Datenbank.getAusleihungenByNutzer(ReadNest.userManager.getNutzer())) {
            if (ausleihBox != null) {
                ausleihBox.setAlignment(Pos.TOP_LEFT);
            }
            Buch b = ReadNest.buchManager.getBuchByExemplarID(a.getExemplarID());
            Autor ar = ReadNest.buchManager.getAutorByID(b.getAutorID());
            if(ar == null) {
                ar = new Autor(-1, "Autor nicht gefunden");
            }
            Node buchWidget = widMan.loadBuchWidget(b.getCachedVorschaubild(), b.getBuchName(), ar.getName(), a.getBisDatum(), false, this);
            if(this.ausleihBox != null) {
                this.ausleihBox.getChildren().add(buchWidget);
            }
        }
        if(ausleihBox != null && ausleihBox.getChildren().isEmpty()) {
            ausleihBox.setAlignment(Pos.CENTER);
            Label l = new Label("Keine Ausleihungen vorhanden.");
            l.setFont(new Font("System", 16));
            ausleihBox.getChildren().add(l);
        }
    }

    /** Bücher im Katalog werden aufgelistet **/
    private void buchListeFuellen() {
        for (Buch b : ReadNest.buchManager.getBuchListe()) {
            Autor ar = ReadNest.buchManager.getAutorByID(b.getAutorID());
            if(ar == null) {
                ar = new Autor(-1, "Autor nicht gefunden");
            }
            Node buchWidget = widMan.loadBuchWidget(b.getCachedVorschaubild(), b.getBuchName(), ar.getName(), null, true, this);
            if(this.buchPane != null) {
                this.buchPane.getChildren().add(buchWidget);
            }
        }
    }
}
