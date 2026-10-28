package org.fg.readnest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WidgetManager {

    public AusleihenController createAusleihen(int exemplarID) {
        try {
            AusleihenController ausleihenController = (AusleihenController) openNewForm("ausleihen", "Ausleihen", false);
            ausleihenController.setExemplarID(exemplarID);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    /** Lädt Exemplar Widget als Node, um als dynamisches Widget verwendet zu werden. **/
    public Node loadExemplarWidget(int exemplarNum) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("exemplarWidget.fxml"));
            Node n = loader.load();
            ExemplarController exemplarController = loader.getController();
            exemplarController.getExemplarText().setText("Exemplar " + exemplarNum);
            exemplarController.setExemplarID(exemplarNum);
            return n;
        } catch (IOException e) {
            return null;
        }
    }

    /** Erstellt den Reader **/
    public ReaderController createReader(Buch b) {
        ReaderController readerCont = (ReaderController) openNewForm("reader", b.getBuchName(), false);
        if (readerCont != null) {
            readerCont.setBuch(b);
        }
        return readerCont;
    }

    /** Erstellt Dialogfenster. Ist immer im Vordergrund **/
    public DialogueController createDialogue(String title, String message) {
        DialogueController dialCont = (DialogueController) openNewForm("dialogue", title, true);
        if (dialCont != null) {
            dialCont.getDialText().setText(message);
        }
        return dialCont;
    }

    /** Erstellt einfaches Formular. Optional immer im Vordergrund → boolean AlwaysOnTop
     * gibt Controller des Formulars für einfachen Zugriff und Speicherung zurück **/
    public Object openNewForm(String fName, String title, boolean AlwaysOnTop) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fName + ".fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle(title);
            Scene s = new Scene(root);
            newStage.setScene(s);
            s.getStylesheets().add(Objects.requireNonNull(getClass().getResource("primer-light.css")).toExternalForm());
            newStage.setResizable(false);
            newStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/fg/readnest/readnestlogo.png"))));
            newStage.setAlwaysOnTop(AlwaysOnTop);
            newStage.show();
            return loader.getController();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /** Lädt die Detailübersicht **/
    public Object loadDetailuebersicht(Buch b) {
        DetailsController detailsController = (DetailsController) openNewForm("details", b.getBuchName() + " Details", false);
        detailsController.getBookTitle().setText(b.getBuchName());
        detailsController.getBookAutor().setText(ReadNest.buchManager.getAutorByID(b.getAutorID()).getName());
        detailsController.getBookVerlag().setText(ReadNest.buchManager.getVerlagByID(b.getVerlagID()).getBezeichnung());
        detailsController.getBookIcon().setImage(b.getCachedVorschaubild());
        detailsController.getBeschreibungText().setText(b.getBeschreibung());
        return detailsController;
    }

    /** Lädt Buch-Widget. Speziell für ausgeliehene Bücher.
     * Gibt Node zurück, um als dynamisches Widget verwendet werden zu können **/
    public Node loadBuchWidget(Image buchIcon, String buchTitle, String buchAutor, Datum bisDatum, boolean inKatalog, ReadNestController rnc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("buchWidget.fxml"));
            Node n = loader.load();
            BuchWidgetController widCont = loader.getController();
            widCont.getBuchTitle().setText(buchTitle);
            widCont.getBuchAutor().setText(buchAutor);
            widCont.setInKatalog(inKatalog);
            widCont.getBisDatText().setText("Bis " + bisDatum);
            widCont.setReadNestController(rnc);
            if(buchIcon != null) {
                widCont.getBuchIcon().setImage(buchIcon);
            }
            return n;
        } catch (IOException e) {
            return null;
        }
    }
}