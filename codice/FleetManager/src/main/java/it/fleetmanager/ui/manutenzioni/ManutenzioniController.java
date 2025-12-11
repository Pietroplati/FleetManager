package it.fleetmanager.ui.manutenzioni;

import java.time.format.DateTimeFormatter;
import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.dashboards.ManagerDashboardController;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ManutenzioniController {

	@FXML
	private TableView<Manutenzione> tableManutenzioni;
	@FXML
	private TableColumn<Manutenzione, String> colId, colTarga, colData, colTipo, colDescrizione;

	@FXML
	private Button btnNuova, btnChiudi;

	@FXML
	private Label lblDescrizioneRuolo;

	private Utente utenteLoggato;

	private final H2DatabaseManager db = H2DatabaseManager.getInstance();
	private final ManutenzioneDAO manutDAO = new ManutenzioneDAOImpl(db);
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
	private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);
	private final UtenteDAO utenteDAO = new UtenteDAOImpl(db);

	private final SistemaNotifiche sistemaNotifiche = new SistemaNotifiche(notificaDAO, utenteDAO);

	private final GestoreManutenzioniImpl gestoreManut = new GestoreManutenzioniImpl(manutDAO, veicoloDAO,
			sistemaNotifiche);

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public void setUtente(Utente user) {
		this.utenteLoggato = user;
		configuraUI();
		caricaDati();
	}

	private void configuraUI() {
		colId.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().getIdManutenzione()));
		colTarga.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTarga()));
		colData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().format(fmt)));
		colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoManutenzione().name()));
		colDescrizione.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));

		if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
			lblDescrizioneRuolo.setText("Gestione Manutenzioni - Manager");
		} else {
			lblDescrizioneRuolo.setText("Le mie manutenzioni");
			btnChiudi.setVisible(false);
		}

		tableManutenzioni.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void caricaDati() {
		List<Manutenzione> tutte = manutDAO.getTutteManutenzioni();
		tableManutenzioni.getItems().setAll(tutte);
	}

	// ============================================================
	// BOTTONI
	// ============================================================

	@FXML
	private void onNuovaManutenzione() {
		var ctrl = (NuovaManutenzioneController) SceneManager
				.changeSceneWithController("/ui/views/manutenzioni/NuovaManutenzioneView.fxml");
		ctrl.setUtente(utenteLoggato);
	}

	@FXML
	private void onChiudi() {
		Manutenzione m = tableManutenzioni.getSelectionModel().getSelectedItem();
		if (m == null) {
			mostraErrore("Seleziona una manutenzione.");
			return;
		}

		gestoreManut.chiudiManutenzione(m.getIdManutenzione());
		mostraInfo("Manutenzione chiusa.");
		caricaDati();
	}

	// ============================================================
	// NAVIGAZIONE
	// ============================================================

	@FXML
	private void onBack() {
		var ctrl = (ManagerDashboardController) SceneManager
				.changeSceneWithController("/ui/views/dashboards/ManagerDashboard.fxml");
		ctrl.setUtente(utenteLoggato);
	}

	// ============================================================
	// ALERT
	// ============================================================

	private void mostraErrore(String msg) {
		Alert a = new Alert(Alert.AlertType.ERROR);
		a.setHeaderText("Errore");
		a.setContentText(msg);
		a.show();
	}

	private void mostraInfo(String msg) {
		Alert a = new Alert(Alert.AlertType.INFORMATION);
		a.setHeaderText("Operazione completata");
		a.setContentText(msg);
		a.show();
	}
}
