package org.fg.readnest;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ReaderController {

    @FXML
    private ImageView bookView;
    @FXML
    private TextField currentPage;
    @FXML
    private Label maxPage;
    @FXML
    private Button pageBackButton;
    @FXML
    private Button pageNextButton;

    private WidgetManager widMan = new WidgetManager();
    private Buch buch;
    private PDDocument document;
    private PDFRenderer renderer;
    private int pageIndex = 0;

    @FXML
    protected void onPageBackButtonClick() {
        previousPage();
    }

    @FXML
    protected void onPageNextButtonClick() {
        nextPage();
    }

    @FXML
    protected void onPageTextFieldEnter() {
        if (currentPage.getText().isEmpty()) {
            widMan.createDialogue("Reader", "Bitte geben Sie eine Seitenzahl ein.");
            return;
        }
        if (Integer.parseInt(currentPage.getText()) < 1 || Integer.parseInt(currentPage.getText()) > document.getNumberOfPages()) {
            widMan.createDialogue("Reader", "Diese Seite existiert nicht.");
            return;
        }
        pageIndex = Integer.parseInt(currentPage.getText()) - 1;
        showPage(pageIndex);
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            try {
                File pdfFile = downloadPDF(buch.getBuchdatei());
                if (pdfFile != null) {
                    document = Loader.loadPDF(pdfFile);
                    renderer = new PDFRenderer(document);

                    maxPage.setText("/ " + document.getNumberOfPages());
                    showPage(pageIndex);
                } else {
                    System.out.println("Datei konnte nicht heruntergeladen werden.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    /** Öffnet Seite mit Index **/
    private void showPage(int index) {
        try {
            BufferedImage bim = renderer.renderImageWithDPI(index, 150);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", os);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            Image fxImage = new Image(is);
            bookView.setImage(fxImage);
            currentPage.setText(String.valueOf(index + 1));
            pageBackButton.setDisable(pageIndex == 0);
            pageNextButton.setDisable(pageIndex == document.getNumberOfPages() - 1);
            os.close();
            is.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** Öffnet nächste Seite **/
    private void nextPage() {
        if (pageIndex < document.getNumberOfPages() - 1) {
            pageIndex++;
            showPage(pageIndex);
        }
    }

    /** Öffnet vorherige Seite **/
    private void previousPage() {
        if (pageIndex > 0) {
            pageIndex--;
            showPage(pageIndex);
        }
    }

    /** Lädt PDF-Datei herunter und speichert sie zwischen **/
    public static File downloadPDF(String fileURL) {
        try {
            URL url = new URL(fileURL);
            InputStream in = url.openStream();
            File tempFile = File.createTempFile("drive_pdf", ".pdf");
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();
            return tempFile;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public void setBuch(Buch buch) {
        this.buch = buch;
    }
}
