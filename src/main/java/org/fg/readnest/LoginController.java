package org.fg.readnest;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    public TextField usernameField;
    @FXML
    private PasswordField passwordField1;
    @FXML
    private ProgressBar loadingBar;

    private final WidgetManager widMan = new WidgetManager();

    /** Wenn bei den Nutzername- und Passwortfeldern jeweils Enter gedrückt wird **/
    @FXML
    protected void onUsernameFieldEnter() {
        tryLogin();
    }
    @FXML
    protected void onPasswortFieldEnter() {
        tryLogin();
    }

    /** Registrieren gedrückt **/
    @FXML
    protected void onRegistrierenButtonClick() {
        if(usernameField.getText().isEmpty() || passwordField1.getText().isEmpty()) {
            widMan.createDialogue("Login", "Bitte geben Sie ein Passwort und einen Nutzernamen ein.");
            return;
        }
        DBResult dbr = Datenbank.createUser(usernameField.getText().trim(), passwordField1.getText().trim());
        widMan.createDialogue("Login", dbr.message());
    }

    /** Startet einen Anmeldeversuch **/
    public void tryLogin() {
        if(Datenbank.checkLogin(usernameField.getText().trim(), passwordField1.getText().trim())) {
            // Daten richtig
            ReadNest.userManager.setNutzer(Datenbank.getNutzer(usernameField.getText().trim()));
            loginSuccess();
        } else {
            // Daten falsch oder keine Verbindung zur DB
            loginFailed();
        }
    }

    /** Anmelden gedrückt **/
    @FXML
    protected void onHelloButtonClick() {
        tryLogin();
    }

    /** Anmeldeversuch erfolgreich → Hauptformular wird geöffnet **/
    private void loginSuccess() {
        Object controller = widMan.openNewForm("readnestMain", "ReadNest", false);
        if (controller instanceof ReadNestController) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();

        } else {
            System.out.println("Error: Controller konnte nicht geladen werden.");
        }
    }

    /** Anmeldeversuch fehlgeschlagen → Dialogbox wird geöffnet **/
    private void loginFailed() {
        System.out.println("Error: Nutzername oder Passwort falsch.");
        widMan.createDialogue("Login", "Nutzername oder Passwort falsch.");
    }

    /** Für Throbber im Lade-Widget verwendet **/
    public ProgressBar getLoadingBar() {
        return loadingBar;
    }
}
