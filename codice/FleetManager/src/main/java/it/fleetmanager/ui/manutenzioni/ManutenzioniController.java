package it.fleetmanager.ui.manutenzioni;

import java.time.format.DateTimeFormatter;
import java.util.List;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.util.RuoloUtente;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ManutenzioniController implements UserAwareController {

	@FXML private TableView<Manutenzione> tableManutenzioni;

	@FXML private TableColumn<Manutenzione, String> colId;
	@FXML private TableColumn<Manutenzione, String> colTarga;
	@FXML private TableColumn<Manutenzione, String> colData;
	@FXML private TableColumn<Manutenzione, String> colOraInizio;
	@FXML private TableColumn<Manutenzione, String> colTipo;
	@FXML private TableColumn<Manutenzione, String> colDescrizione;

	@FXML private Button btnNuova, btnChiudi;
	@FXML private Label lblDescrizioneRuolo;

	private Utente utenteLoggato;

	//SOLO facade
	private final UiFacade ui = AppContext.getInstance().getUiFacade();

	private final DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter fmtOra  = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	public void setUtente(Utente user) {
		this.utenteLoggato = user;
		configuraUI();
		caricaDati();
	}

	private void configuraUI() {

		colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdManutenzione())));
		colTarga.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTarga()));

		colData.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getData().toLocalDate().format(fmtData)
		));

		colOraInizio.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getData().toLocalTime().format(fmtOra)
		));

		colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoManutenzione().name()));
		colDescrizione.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));

		if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
			lblDescrizioneRuolo.setText("Gestione Manutenzioni - Manager");
			btnChiudi.setVisible(true);
			btnChiudi.setManaged(true);
		} else {
			lblDescrizioneRuolo.setText("Le mie manutenzioni");
			btnChiudi.setVisible(false);
			btnChiudi.setManaged(false);
		}

		tableManutenzioni.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	private void caricaDati() {
		List<Manutenzione> tutte = ui.getTutteManutenzioni();
		tableManutenzioni.getItems().setAll(tutte);
	}

	@FXML
	private void onNuovaManutenzione() {
		SceneManager.changeScene("/ui/views/manutenzioni/NuovaManutenzioneView.fxml", utenteLoggato);
	}

	@FXML
	private void onChiudi() {

		Manutenzione m = tableManutenzioni.getSelectionModel().getSelectedItem();

		if (m == null) {
			mostraErrore("Seleziona una manutenzione.");
			return;
		}

		try {
			ui.chiudiManutenzione(m.getIdManutenzione());
			mostraInfo("Manutenzione chiusa.");
			caricaDati();
		} catch (Exception e) {
			mostraErrore(e.getMessage());
		}
	}

	@FXML
	private void onBack() {
		SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utenteLoggato);
	}

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
