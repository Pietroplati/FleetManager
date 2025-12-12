package it.fleetmanager.ui.scadenze;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;

import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.dashboards.ManagerDashboardController;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

public class ScadenzeController {

	// ================== UI ==================
	@FXML
	private TableView<Scadenza> tableScadenze;

	@FXML
	private TableColumn<Scadenza, String> colId;
	@FXML
	private TableColumn<Scadenza, String> colTarga;
	@FXML
	private TableColumn<Scadenza, String> colTipo;
	@FXML
	private TableColumn<Scadenza, String> colData;
	@FXML
	private TableColumn<Scadenza, String> colNotificata;

	@FXML
	private Label lblInfoUtente;

	// ================== DAO ==================
	private final ScadenzaDAO scadenzaDAO = new ScadenzaDAOImpl(H2DatabaseManager.getInstance());

	// ================== UTENTE ==================
	private Utente utente;

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("it", "IT"));

	// ============================================================
	// SET UTENTE
	// ============================================================
	public void setUtente(Utente u) {
		this.utente = u;
		lblInfoUtente.setText("Elenco scadenze del sistema");
		caricaScadenze();
	}

	// ============================================================
	// INITIALIZE
	// ============================================================
	@FXML
	private void initialize() {
		tableScadenze.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setupColumns();
	}

	// ============================================================
	// COLONNE
	// ============================================================
	private void setupColumns() {

		colId.setCellValueFactory(s -> new SimpleStringProperty(String.valueOf(s.getValue().getIdScadenza())));

		colTarga.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getTarga()));

		colTipo.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getTipoScadenza().name()));

		colData.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getData().format(fmt)));

		colNotificata.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getNotificata() ? "SI" : "NO"));
	}

	// ============================================================
	// CARICA SCADENZE
	// ============================================================
	private void caricaScadenze() {
		List<Scadenza> lista = scadenzaDAO.getTutteScadenze();
		tableScadenze.getItems().setAll(lista);
	}

	// ============================================================
	// SELEZIONE RIGA
	// ============================================================
	private Scadenza getSel() {
		Scadenza s = tableScadenze.getSelectionModel().getSelectedItem();
		if (s == null)
			mostraErrore("Seleziona una scadenza dalla tabella.");
		return s;
	}

	// ============================================================
	// BOTTONI CRUD
	// ============================================================
	@FXML
	private void onAggiungi() {
		mostraInfo("Funzione aggiungi scadenza in preparazione (serve il form).");
	}

	@FXML
	private void onModifica() {
		if (getSel() == null)
			return;

		mostraInfo("Funzione modifica scadenza in preparazione (serve il form).");
	}

	@FXML
	private void onElimina() {
		Scadenza sc = getSel();
		if (sc == null)
			return;

		Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Sei sicuro di voler eliminare questa scadenza?",
				ButtonType.YES, ButtonType.NO);

		if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
			scadenzaDAO.delete(sc.getIdScadenza());
			caricaScadenze();
		}
	}

	// ============================================================
	// BACK
	// ============================================================
	@FXML
	private void onBack() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/dashboards/ManagerDashboard.fxml");

		((ManagerDashboardController) ctrl).setUtente(utente);
	}

	// ============================================================
	// ALERTS
	// ============================================================
	private void mostraErrore(String msg) {
		new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
	}

	private void mostraInfo(String msg) {
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
}
