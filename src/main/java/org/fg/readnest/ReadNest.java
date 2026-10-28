package org.fg.readnest;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Objects;

public class ReadNest extends Application {

    /** Erstellt Splashscreen und lädt alle wichtigen Daten herunter **/
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ReadNest.class.getResource("splashscreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/fg/readnest/readnestlogo.png"))));
        stage.setTitle("Login");
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("primer-light.css")).toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        LoginController widCont = fxmlLoader.getController();
        widCont.getLoadingBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        stage.show();
        Task<Void> initTask = programmInitialisierung(stage);
        new Thread(initTask).start();
    }

    private static Task<Void> programmInitialisierung(Stage stage) {
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() {
                userManager = new UserManager();
                buchManager = new BuchManager();
                return null;
            }
        };
        // Bei Erfolg wird das Login-Fenster erstellt, und der Splashscreen geschlossen.
        initTask.setOnSucceeded(_ -> {
            WidgetManager widMan = new WidgetManager();
            stage.close();
            downloading = false;
            System.out.println("Download finished");
            widMan.openNewForm("hello-view", "Login", false);
        });
        // Bei Exception wird das Programm geschlossen.
        initTask.setOnFailed(_ -> {
           System.out.println("Download failed");
           downloading = false;
           System.exit(0);
        });
        return initTask;
    }

    /** Erstellt Verwaltungsklassen für einfache Referenzierung **/
    public static UserManager userManager;
    public static BuchManager buchManager;
    public static boolean downloading = true;

    /** Referenz auf ReadNest-main **/
    public static ReadNestController rnc = null;

    public static void main(String[] args) {

        launch();
    }
}