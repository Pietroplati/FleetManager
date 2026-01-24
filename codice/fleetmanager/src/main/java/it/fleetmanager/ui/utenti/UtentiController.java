package it.fleetmanager.ui.utenti;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class UtentiController implements UserAwareController {

	// ================== TABLE ==================
	@FXML
	private TableView<Utente> tableUtenti;
	@FXML
	private TableColumn<Utente, String> colNome;
	@FXML
	private TableColumn<Utente, String> colCognome;
	@FXML
	private TableColumn<Utente, String> colEmail;
	@FXML
	private TableColumn<Utente, String> colPatente;

	private final UiFacade ui = AppContext.getInstance().getUiFacade();
	private final ObservableList<Utente> utenti = FXCollections.observableArrayList();

	private Utente manager;

	private boolean tabellaInizializzata = false;

	// ================== INIT ==================

	@Override
	public void setUtente(Utente utente) {
		this.manager = utente;

		if (!tabellaInizializzata) {
			inizializzaTabella();
			tabellaInizializzata = true;
		}

		caricaUtenti();
	}

	private void inizializzaTabella() {

		// Binding proprietà → colonne
		colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
		colCognome.setCellValueFactory(new PropertyValueFactory<>("cognome"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colPatente.setCellValueFactory(new PropertyValueFactory<>("patente"));

		// Larghezze ESPLICITE (somma = larghezza TableView)
		colNome.setPrefWidth(180);
		colCognome.setPrefWidth(180);
		colEmail.setPrefWidth(320);
		colPatente.setPrefWidth(140);

		// 🔥 niente colonna vuota a destra
		tableUtenti.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		tableUtenti.setItems(utenti);
	}

	private void caricaUtenti() {
		utenti.setAll(ui.getTuttiDriver());
	}

	// ================== AZIONI ==================

	@FXML
	private void onAggiungi() {
		SceneManager.changeScene("/ui/views/utenti/NuovoUtenteView.fxml", manager);
	}

	@FXML
	private void onModifica() {
		Utente selezionato = tableUtenti.getSelectionModel().getSelectedItem();
		if (selezionato == null) {
			mostraAvviso("Seleziona un utente da modificare.");
			return;
		}

		UtenteDialog dialog = new UtenteDialog(selezionato);
		Optional<Utente> risultato = dialog.mostra();

		risultato.ifPresent(u -> {
			ui.aggiornaUtente(u);
			caricaUtenti();
		});
	}

	@FXML
	private void onElimina() {
		Utente selezionato = tableUtenti.getSelectionModel().getSelectedItem();
		if (selezionato == null) {
			mostraAvviso("Seleziona un utente da eliminare.");
			return;
		}

		Alert conferma = new Alert(Alert.AlertType.CONFIRMATION);
		conferma.setTitle("Conferma eliminazione");
		conferma.setHeaderText("Eliminare l'utente selezionato?");
		conferma.setContentText(selezionato.getNome() + " " + selezionato.getCognome());

		Optional<ButtonType> res = conferma.showAndWait();
		if (res.isPresent() && res.get() == ButtonType.OK) {
			ui.eliminaUtente(selezionato.getIdUtente());
			caricaUtenti();
		}
	}

	@FXML
	private void onIndietro() {
		SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", manager);
	}

	// ================== UTIL ==================

	private void mostraAvviso(String msg) {
		Alert a = new Alert(Alert.AlertType.WARNING);
		a.setTitle("Attenzione");
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}
