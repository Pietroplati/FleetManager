package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreLoginImpl;
import it.fleetmanager.service.interfaces.GestoreLogin;
import it.fleetmanager.util.RuoloUtente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

	@FXML
	private TextField emailField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label errorLabel;

	private final GestoreLogin gestoreLogin;

	public LoginController() {
		UtenteDAO utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
		this.gestoreLogin = new GestoreLoginImpl(utenteDAO);
	}

	@FXML
	private void onLoginClicked() {
		String email = emailField.getText();
		String password = passwordField.getText();

		errorLabel.setText(""); // reset messaggio errore

		Utente utente = gestoreLogin.login(email, password);

		if (utente == null) {
			errorLabel.setText("Credenziali non valide.");
			return;
		}

		// Login OK → routing per ruolo
		caricaDashboard(utente);

	}

	private void caricaDashboard(Utente utente) {

		if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {

			// Carico la dashboard manager e ottengo il controller
			ManagerDashboardController controller = SceneManager
					.changeSceneWithController("/ui/views/ManagerDashboard.fxml");

			controller.setUtente(utente);

		} else {

			// Carico la dashboard driver e ottengo il controller
			DriverDashboardController controller = SceneManager
					.changeSceneWithController("/ui/views/DriverDashboard.fxml");

			controller.setUtente(utente);
		}
	}
}
