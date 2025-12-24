package it.fleetmanager.ui.scadenze;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.TipoScadenza;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ScadenzaFormController {

	// ================== UI ==================
	@FXML private Label lblTitolo;
	@FXML private ComboBox<String> cbTarga;
	@FXML private ComboBox<TipoScadenza> cmbTipo;
	@FXML private DatePicker dpData;
	@FXML private CheckBox chkNotificata;

	// ================== DAO ==================
	private final H2DatabaseManager db = H2DatabaseManager.getInstance();
	private final ScadenzaDAO scadenzaDAO = new ScadenzaDAOImpl(db);
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);

	// ================== MODEL ==================
	private Scadenza scadenza; // null = CREATE

	// ============================================================
	// INIT
	// ============================================================
	@FXML
	private void initialize() {

		cmbTipo.getItems().setAll(TipoScadenza.values());

		// 🔹 carica targhe veicoli esistenti
		for (var v : veicoloDAO.getTuttiVeicoli()) {
			cbTarga.getItems().add(v.getTarga());
		}
	}

	// ============================================================
	// MODALITÀ CREATE
	// ============================================================
	public void nuovaScadenza() {
		scadenza = null;
		lblTitolo.setText("Nuova Scadenza");
		cbTarga.setDisable(false);
	}

	// ============================================================
	// MODALITÀ UPDATE
	// ============================================================
	public void modificaScadenza(Scadenza s) {
		this.scadenza = s;
		lblTitolo.setText("Modifica Scadenza");

		cbTarga.setValue(s.getTarga());
		cmbTipo.setValue(s.getTipoScadenza());
		dpData.setValue(s.getData());
		chkNotificata.setSelected(s.getNotificata());

		cbTarga.setDisable(true); // 🔒 la targa non si cambia
	}

	// ============================================================
	// SALVA
	// ============================================================
	@FXML
	private void onSalva() {

		if (cbTarga.getValue() == null
				|| cmbTipo.getValue() == null
				|| dpData.getValue() == null) {

			new Alert(Alert.AlertType.ERROR,
					"Compila tutti i campi obbligatori").show();
			return;
		}

		if (scadenza == null) {
			scadenza = new Scadenza();
		}

		scadenza.setTarga(cbTarga.getValue());
		scadenza.setTipoScadenza(cmbTipo.getValue());
		scadenza.setData(dpData.getValue());
		scadenza.setNotificata(chkNotificata.isSelected());

		if (scadenza.getIdScadenza() == 0) {
			scadenzaDAO.save(scadenza);
		} else {
			scadenzaDAO.update(scadenza);
		}

		chiudi();
	}

	// ============================================================
	// ANNULLA
	// ============================================================
	@FXML
	private void onAnnulla() {
		chiudi();
	}

	private void chiudi() {
		((Stage) cbTarga.getScene().getWindow()).close();
	}
}
