package org.fg.readnest;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminController {

    @FXML private TableView<Buch> buchTable;
    @FXML private TableColumn<Buch, String> titelColumn;
    @FXML private TableColumn<Buch, String> isbnColumn;
    @FXML private TableColumn<Buch, String> autorColumn;
    @FXML private TableColumn<Buch, String> verlagColumn;

    @FXML private TableView<Autor> autorTable;
    @FXML private TableColumn<Autor, String> autorNameColumn;

    @FXML private TableView<Verlag> verlagTable;
    @FXML private TableColumn<Verlag, String> verlagNameColumn;

    private final BuchManager buchManager = ReadNest.buchManager;
    private final UserManager userManager = ReadNest.userManager;

    @FXML
    private void initialize() {
        if (!userManager.serverValidateIsAdmin()) {
            throw new SecurityException("Nur Administratoren haben Zugriff auf diese Ansicht");
        }

        initColumns();
        loadData();
    }

    private void initColumns() {
        titelColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBuchName()));
        isbnColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getISBN()));
        autorColumn.setCellValueFactory(cell -> {
            Autor autor = buchManager.getAutorByID(cell.getValue().getAutorID());
            return new SimpleStringProperty(autor != null ? autor.getName() : "");
        });
        verlagColumn.setCellValueFactory(cell -> {
            Verlag verlag = buchManager.getVerlagByID(cell.getValue().getVerlagID());
            return new SimpleStringProperty(verlag != null ? verlag.getBezeichnung() : "");
        });

        autorNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        verlagNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBezeichnung()));
    }

    private void loadData() {
        buchTable.setItems(FXCollections.observableArrayList(buchManager.getBuchListe()));
        autorTable.setItems(FXCollections.observableArrayList(buchManager.getAutorListe()));
        verlagTable.setItems(FXCollections.observableArrayList(buchManager.getVerlagListe()));
    }
}
