package it.fleetmanager.ui;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.GestoreLogin;
import it.fleetmanager.util.RuoloUtente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    //SOLO INTERFACCIA, ottenuta dal composition root
    private final GestoreLogin gestoreLogin = AppContext.getInstance().getGestoreLogin();

    @FXML
    private void onLoginClicked() {
        String email = emailField.getText();
        String password = passwordField.getText();

        errorLabel.setText("");

        Utente utente = gestoreLogin.login(email, password);

        if (utente == null) {
            errorLabel.setText("Credenziali non valide.");
            return;
        }

        caricaDashboard(utente);
    }

    private void caricaDashboard(Utente utente) {
        if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
            SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utente);
        } else {
            SceneManager.changeScene("/ui/views/dashboards/DriverDashboard.fxml", utente);
        }
    }
}
