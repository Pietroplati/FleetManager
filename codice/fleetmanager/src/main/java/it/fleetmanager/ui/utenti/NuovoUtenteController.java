package it.fleetmanager.ui.utenti;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.util.RuoloUtente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class NuovoUtenteController implements UserAwareController {

	@FXML
	private TextField txtNome;
	@FXML
	private TextField txtCognome;
	@FXML
	private TextField txtEmail;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private TextField txtPatente;

	private Utente manager;

	private final UiFacade ui = AppContext.getInstance().getUiFacade();

	@Override
	public void setUtente(Utente u) {
		this.manager = u;
	}

	@FXML
	private void onConferma() {

		if (txtNome.getText().isBlank() || txtCognome.getText().isBlank() || txtEmail.getText().isBlank()
				|| txtPassword.getText().isBlank()) {

			alertErrore("Compila tutti i campi obbligatori.");
			return;
		}

		try {
			Utente nuovo = new Utente(0, txtNome.getText(), txtCognome.getText(), txtEmail.getText(),
					txtPassword.getText(), RuoloUtente.DRIVER,
					txtPatente.getText().isBlank() ? null : txtPatente.getText());

			ui.salvaUtente(nuovo);

			alertInfo("Utente creato con successo.");
			tornaAllaLista();

		} catch (Exception e) {
			alertErrore(e.getMessage());
		}
	}

	@FXML
	private void onBack() {
		tornaAllaLista();
	}

	private void tornaAllaLista() {
		SceneManager.changeScene("/ui/views/utenti/UtentiView.fxml", manager);
	}

	private void alertErrore(String msg) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setHeaderText("Errore");
		a.setContentText(msg);
		a.show();
	}

	private void alertInfo(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText("Operazione completata");
		a.setContentText(msg);
		a.show();
	}
}
