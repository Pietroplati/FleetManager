package it.fleetmanager.ui.prenotazioni;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.*;
import it.fleetmanager.repository.impl.*;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.dashboards.DriverDashboardController;
import it.fleetmanager.ui.dashboards.ManagerDashboardController;
import it.fleetmanager.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrenotazioniController {

	// UI
	@FXML
	private TableView<Prenotazione> tablePrenotazioni;
	@FXML
	private TableColumn<Prenotazione, String> colId, colVeicolo, colDriver, colInizio, colFine, colStato, colTipo;

	@FXML
	private Button btnNuova, btnConferma, btnCompleta, btnAnnulla;
	@FXML
	private Label lblDescrizioneRuolo;
	@FXML
	private ProgressIndicator loadingIndicator;

	// UTENTE LOGGATO
	private Utente utenteLoggato;

	// DAO
	private final H2DatabaseManager db = H2DatabaseManager.getInstance();
	private final PrenotazioneDAO prenDAO = new PrenotazioneDAOImpl(db);
	private final UtenteDAO utenteDAO = new UtenteDAOImpl(db);
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
	private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);

	// SERVIZIO
	private final GestorePrenotazioniImpl gestorePrenotazioni = new GestorePrenotazioniImpl(prenDAO, veicoloDAO,
			new SistemaNotifiche(notificaDAO));

	// CACHE UTENTI (ZERO QUERY durante il rendering)
	private Map<Integer, Utente> cacheUtenti;

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	// ============================================================
	// INIT
	// ============================================================

	public void setUtente(Utente user) {
		this.utenteLoggato = user;

		caricaCacheUtenti(); // 🔥 riduce da 100 query → 0 query
		configuraUI();
		gestorePrenotazioni.aggiornaStatiPrenotazioni();
		tablePrenotazioni.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		caricaDati();
	}

	// ============================================================
	// CACHE UTENTI
	// ============================================================

	private void caricaCacheUtenti() {
		cacheUtenti = new HashMap<>();
		for (Utente u : utenteDAO.getTuttiUtenti()) {
			cacheUtenti.put(u.getIdUtente(), u);
		}
	}

	// ============================================================
	// CONFIGURA UI
	// ============================================================

	private void configuraUI() {

		colId.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().getIdPrenotazione()));
		colVeicolo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTarga()));

		colDriver.setCellValueFactory(c -> {
			Utente u = cacheUtenti.get(c.getValue().getIdUtente());
			return new SimpleStringProperty(u.getNome() + " " + u.getCognome());
		});

		colInizio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().format(fmt)));
		colFine.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().format(fmt)));
		colStato.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStato().name()));
		colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPrenotazione().name()));

		if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
			lblDescrizioneRuolo.setText("Visualizzazione completa prenotazioni - Manager");
			btnConferma.setVisible(true);
			btnCompleta.setVisible(true);
			btnNuova.setVisible(false);
		} else {
			lblDescrizioneRuolo.setText("Le mie prenotazioni");
			btnNuova.setVisible(true);
			btnConferma.setVisible(false);
			btnCompleta.setVisible(false);
		}
	}

	// ============================================================
	// CARICAMENTO DATI (VELOCISSIMO)
	// ============================================================

	private void caricaDati() {
		List<Prenotazione> tutte = prenDAO.findAll(); // UNA SOLA QUERY

		// FILTRO DRIVER
		if (utenteLoggato.getRuoloUtente() == RuoloUtente.DRIVER) {
			tutte = tutte.stream().filter(p -> p.getIdUtente() == utenteLoggato.getIdUtente())
					.collect(Collectors.toList());
		}

		// ORDINAMENTO
		tutte.sort((a, b) -> {
			int cmp = Integer.compare(priorita(a), priorita(b));
			return (cmp != 0) ? cmp : confrontoTemporale(a, b);
		});

		tablePrenotazioni.getItems().setAll(tutte);
	}

	// ============================================================
	// PRIORITÀ ORDINAMENTO (UNIFICATA)
	// ============================================================

	private int priorita(Prenotazione p) {
		StatoPrenotazione s = p.getStato();

		if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
			return switch (s) {
			case RICHIESTA -> 1;
			case ATTIVA -> 2;
			case CONFERMATA -> 3;
			case COMPLETATA -> 4;
			case ANNULLATA -> 5;
			};
		}

		return switch (s) {
		case ATTIVA -> 1;
		case RICHIESTA -> 2;
		case CONFERMATA -> 3;
		case COMPLETATA -> 4;
		case ANNULLATA -> 5;
		};
	}

	private int confrontoTemporale(Prenotazione a, Prenotazione b) {
		LocalDateTime now = LocalDateTime.now();

		LocalDateTime ta = a.getDataInizio().isAfter(now) ? a.getDataInizio() : a.getDataFine();
		LocalDateTime tb = b.getDataInizio().isAfter(now) ? b.getDataInizio() : b.getDataFine();

		return ta.compareTo(tb);
	}

	// ============================================================
	// BOTTONI
	// ============================================================

	@FXML
	private void onNuovaPrenotazione() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/prenotazioni/NuovaPrenotazioneView.fxml");
		((NuovaPrenotazioneController) ctrl).setUtente(utenteLoggato);
	}

	@FXML
	private void onConferma() {
		Prenotazione sel = getSel();
		if (sel == null)
			return;

		gestorePrenotazioni.confermaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
		mostraInfo("Prenotazione confermata.");
		caricaDati();
	}

	@FXML
	private void onCompleta() {
		Prenotazione sel = getSel();
		if (sel == null)
			return;

		gestorePrenotazioni.completaPrenotazione(sel.getIdPrenotazione());
		mostraInfo("Prenotazione completata.");
		caricaDati();
	}

	@FXML
	private void onAnnulla() {
		Prenotazione sel = getSel();
		if (sel == null)
			return;

		gestorePrenotazioni.annullaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
		mostraInfo("Prenotazione annullata.");
		caricaDati();
	}

	private Prenotazione getSel() {
		Prenotazione p = tablePrenotazioni.getSelectionModel().getSelectedItem();
		if (p == null)
			mostraErrore("Seleziona una prenotazione.");
		return p;
	}

	// ============================================================
	// REFRESH
	// ============================================================

	@FXML
	private void onRefreshClick() {

		loadingIndicator.setVisible(true);

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				gestorePrenotazioni.aggiornaStatiPrenotazioni();
				return null;
			}
		};

		task.setOnSucceeded(e -> {
			caricaDati();
			loadingIndicator.setVisible(false);
		});

		task.setOnFailed(e -> {
			loadingIndicator.setVisible(false);
			mostraErrore("Errore durante l'aggiornamento.");
		});

		new Thread(task).start();
	}

	// ============================================================
	// NAVIGAZIONE
	// ============================================================

	@FXML
	private void onBack() {
		if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
			var ctrl = (ManagerDashboardController) SceneManager
					.changeSceneWithController("/ui/views/dashboards/ManagerDashboard.fxml");
			ctrl.setUtente(utenteLoggato);
		} else {
			var ctrl = (DriverDashboardController) SceneManager
					.changeSceneWithController("/ui/views/dashboards/DriverDashboard.fxml");
			ctrl.setUtente(utenteLoggato);
		}
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
