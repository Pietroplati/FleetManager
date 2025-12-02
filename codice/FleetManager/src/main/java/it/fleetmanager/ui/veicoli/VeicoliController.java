package it.fleetmanager.ui.veicoli;

import java.util.List;

import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.ManagerDashboardController;
import it.fleetmanager.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class VeicoliController {

	@FXML
	private TableView<Veicolo> tableVeicoli;

	@FXML
	private TableColumn<Veicolo, String> colTarga;
	@FXML
	private TableColumn<Veicolo, String> colTipo;
	@FXML
	private TableColumn<Veicolo, String> colMarca;
	@FXML
	private TableColumn<Veicolo, String> colModello;
	@FXML
	private TableColumn<Veicolo, Integer> colAnno;
	@FXML
	private TableColumn<Veicolo, String> colStato;
	@FXML
	private TableColumn<Veicolo, Integer> colKm;
	private Utente utente;

	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());

	private final ObservableList<Veicolo> veicoliList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {

		tableVeicoli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		colTarga.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getTarga()));
		colTipo.setCellValueFactory(
				v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getTipoVeicolo().name()));
		colMarca.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getMarca()));
		colModello.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getModello()));
		colAnno.setCellValueFactory(
				v -> new javafx.beans.property.SimpleIntegerProperty(v.getValue().getAnnoImmatricolazione())
						.asObject());
		colStato.setCellValueFactory(
				v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getStatoVeicolo().name()));
		colKm.setCellValueFactory(
				v -> new javafx.beans.property.SimpleIntegerProperty(v.getValue().getKm()).asObject());

		caricaVeicoli();
	}

	private void caricaVeicoli() {
		List<Veicolo> list = veicoloDAO.getTuttiVeicoli();
		veicoliList.setAll(list);
		tableVeicoli.setItems(veicoliList);
	}

	public void setUtente(Utente u) {
		this.utente = u;
	}

	@FXML
	private void onAggiungi() {
		apriForm(null);
	}

	@FXML
	private void onModifica() {
		Veicolo selezionato = tableVeicoli.getSelectionModel().getSelectedItem();
		if (selezionato == null) {
			mostraAlert("Seleziona un veicolo dalla tabella.");
			return;
		}
		apriForm(selezionato);
	}

	@FXML
	private void onElimina() {
		Veicolo selezionato = tableVeicoli.getSelectionModel().getSelectedItem();
		if (selezionato == null) {
			mostraAlert("Seleziona un veicolo.");
			return;
		}

		veicoloDAO.delete(selezionato.getTarga());
		caricaVeicoli();
	}

	@FXML
	private void onBack() {
		ManagerDashboardController controller = SceneManager
				.changeSceneWithController("/ui/views/ManagerDashboard.fxml");

		controller.setUtente(utente);

	}

	private void apriForm(Veicolo veicoloDaModificare) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/veicoli/VeicoloFormView.fxml"));
			Scene scene = new Scene(loader.load());

			VeicoloFormController controller = loader.getController();
			controller.setVeicolo(veicoloDaModificare);
			controller.setOnSaveCallback(() -> caricaVeicoli());

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle("Dettaglio Veicolo");
			stage.setScene(scene);
			stage.setResizable(false);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mostraAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
		alert.showAndWait();
	}
}
