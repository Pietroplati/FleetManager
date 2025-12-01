package it.fleetmanager.ui;

import java.io.IOException;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.service.impl.GestoreLoginImpl;
import it.fleetmanager.service.interfaces.GestoreLogin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

	@FXML
	private TextField emailField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private Label errorLabel;

	private final GestoreLogin gestoreLogin;

	public LoginController() {
		// Iniezione manuale dei DAO → per ora va benissimo
		UtenteDAO utenteDAO = new UtenteDAOImpl();
		this.gestoreLogin = new GestoreLoginImpl(utenteDAO);
	}

	@FXML
	private void onLoginClicked() {
		String email = emailField.getText();
		String password = passwordField.getText();

		errorLabel.setText(""); // reset

		Utente u = gestoreLogin.login(email, password);

		if (u == null) {
			errorLabel.setText("Credenziali non valide.");
			return;
		}

		// Login OK
		caricaDashboard(u);
	}

	private void caricaDashboard(Utente utente) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/fleetmanager/ui/DashboardView.fxml"));
			Scene scene = new Scene(loader.load());

			Stage stage = (Stage) emailField.getScene().getWindow();
			stage.setScene(scene);
			stage.setTitle("Dashboard - FleetManager");

		} catch (IOException e) {
			e.printStackTrace();
			errorLabel.setText("Errore nel caricamento della Dashboard.");
		}
	}
}
