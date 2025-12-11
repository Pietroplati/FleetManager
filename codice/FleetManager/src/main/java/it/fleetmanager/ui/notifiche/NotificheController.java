package it.fleetmanager.ui.notifiche;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.dashboards.DriverDashboardController;
import it.fleetmanager.ui.dashboards.ManagerDashboardController;
import it.fleetmanager.util.RuoloUtente;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

public class NotificheController {

	@FXML
	private TableView<Notifica> tableNotifiche;
	@FXML
	private TableColumn<Notifica, String> colId;
	@FXML
	private TableColumn<Notifica, String> colTipo;
	@FXML
	private TableColumn<Notifica, String> colMessaggio;
	@FXML
	private TableColumn<Notifica, String> colData;
	@FXML
	private TableColumn<Notifica, String> colLetta;

	@FXML
	private Label lblDescrizioneUtente;
	@FXML
	private ProgressIndicator loadingIndicator;

	private Utente utente;

	private final NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());

	private final ObservableList<Notifica> notificheList = FXCollections.observableArrayList();

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("it", "IT"));

	// ============================================================
	// INITIALIZE - come VeicoliController
	// ============================================================
	@FXML
	private void initialize() {
		tableNotifiche.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setupColumns();
	}

	// ============================================================
	// SET UTENTE
	// ============================================================
	public void setUtente(Utente u) {
		this.utente = u;
		lblDescrizioneUtente.setText("Notifiche di: " + u.getNome() + " " + u.getCognome());
		caricaNotifiche();
	}

	// ============================================================
	// CONFIGURA COLONNE
	// ============================================================
	private void setupColumns() {

		colId.setCellValueFactory(n -> new SimpleStringProperty(String.valueOf(n.getValue().getIdNotifica())));

		colTipo.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getTipoNotifica().name()));

		colMessaggio.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getMessaggio()));

		colData.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getDataInvio().format(fmt)));

		// 🔥 SI / NO
		colLetta.setCellValueFactory(n -> new SimpleStringProperty(n.getValue().getLetta() ? "SI" : "NO"));
	}

	// ============================================================
	// CARICAMENTO DATI (con Task → come prenotazioni)
	// ============================================================
	private void caricaNotifiche() {

		loadingIndicator.setVisible(true);

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				caricaNotificheInterno();
				return null;
			}
		};

		task.setOnSucceeded(e -> loadingIndicator.setVisible(false));

		task.setOnFailed(e -> {
			loadingIndicator.setVisible(false);
			mostraErrore("Errore durante il caricamento delle notifiche.");
		});

		new Thread(task).start();
	}

	private void caricaNotificheInterno() {
		try {
			List<Notifica> tutte = notificaDAO.findByUtente(utente.getIdUtente());

			List<Notifica> ordinate = tutte.stream().sorted(Comparator.comparing(Notifica::getLetta) // false → true
					.thenComparing(Notifica::getDataInvio, Comparator.reverseOrder())).collect(Collectors.toList());

			Platform.runLater(() -> {
				notificheList.setAll(ordinate);
				tableNotifiche.setItems(notificheList);
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ============================================================
	// REFRESH
	// ============================================================
	@FXML
	private void onRefresh() {
		caricaNotifiche();
	}

	// ============================================================
	// SEGNA TUTTE COME LETTE
	// ============================================================
	@FXML
	private void onSegnaTutteComeLette() {

		try {
			List<Notifica> nonLette = notificaDAO.findNonLette(utente.getIdUtente());

			for (Notifica n : nonLette) {
				n.setLetta(true);
				notificaDAO.update(n);
			}

			caricaNotifiche();

		} catch (Exception e) {
			mostraErrore("Errore durante l'aggiornamento delle notifiche.");
		}
	}

	// ============================================================
	// BACK (identico al PrenotazioniController)
	// ============================================================
	@FXML
	private void onBack() {

		if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {

			ManagerDashboardController ctrl = SceneManager
					.changeSceneWithController("/ui/views/dashboards/ManagerDashboard.fxml");
			ctrl.setUtente(utente);

		} else {

			DriverDashboardController ctrl = SceneManager
					.changeSceneWithController("/ui/views/dashboards/DriverDashboard.fxml");
			ctrl.setUtente(utente);
		}
	}

	// ============================================================
	// ALERT
	// ============================================================
	private void mostraErrore(String msg) {
		Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
		alert.showAndWait();
	}
}
