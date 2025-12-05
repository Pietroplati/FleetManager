package it.fleetmanager.ui.dashboards;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.prenotazioni.NuovaPrenotazioneController;
import it.fleetmanager.ui.prenotazioni.PrenotazioniController;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DriverDashboardController {

	// ===== LABEL UI =====
	@FXML
	private Label lblNome;
	@FXML
	private Label lblEmail;
	@FXML
	private Label lblPatente;

	@FXML
	private Label lblPrenotazioniAttive;
	@FXML
	private Label lblProssimaPrenotazione;
	@FXML
	private Label lblVeicoloAssegnato;

	// ===== UTENTE LOGGATO =====
	private Utente utente;

	// ===== DAO =====
	private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());

	// ============================================================
	// SETTAGGIO UTENTE (chiamato dopo login)
	// ============================================================
	public void setUtente(Utente utente) {
		this.utente = utente;

		aggiornaDatiUtente();
		aggiornaWidgets();
	}

	private void aggiornaDatiUtente() {
		lblNome.setText(utente.getNome() + " " + utente.getCognome());
		lblEmail.setText(utente.getEmail());
		lblPatente.setText("Patente: " + utente.getPatente());
	}

	private void aggiornaWidgets() {
		aggiornaPrenotazioniAttive();
		aggiornaProssimaPrenotazione();
		aggiornaVeicoloAssegnato();
	}

	// ============================================================
	// 1️⃣ PRENOTAZIONI ATTIVE ORA
	// ============================================================
	private void aggiornaPrenotazioniAttive() {
		long count = prenotazioneDAO.findByDriver(utente.getIdUtente()).stream()
				.filter(p -> p.getStato() == StatoPrenotazione.ATTIVA).count();

		lblPrenotazioniAttive.setText(String.valueOf(count));
	}

	// ============================================================
	// 2️⃣ PROSSIMA PRENOTAZIONE (solo data/orario)
	// ============================================================
	private void aggiornaProssimaPrenotazione() {

		List<Prenotazione> prenList = prenotazioneDAO.findByDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("it", "IT"));

		// --- 1) Se esiste PRENOTAZIONE ATTIVA → mostra quando FINISCE ---
		var attivaFiniscePrima = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> p.getDataFine().isAfter(now)).min(Comparator.comparing(Prenotazione::getDataFine));

		if (attivaFiniscePrima.isPresent()) {
			lblProssimaPrenotazione.setText(attivaFiniscePrima.get().getDataFine().format(fmt));
			return;
		}

		// --- 2) Altrimenti → mostra PRENOTAZIONE CONFERMATA più vicina a iniziare ---
		var confermataPiuVicina = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.CONFERMATA)
				.filter(p -> p.getDataInizio().isAfter(now)).min(Comparator.comparing(Prenotazione::getDataInizio));

		if (confermataPiuVicina.isPresent()) {
			lblProssimaPrenotazione.setText(confermataPiuVicina.get().getDataInizio().format(fmt));
			return;
		}

		// --- 3) Nessuna prenotazione futura ---
		lblProssimaPrenotazione.setText("Nessuna");
	}

	// ============================================================
	// 3️⃣ VEICOLO ASSEGNATO (solo se prenotazione ATTIVA ora)
	// ============================================================
	private void aggiornaVeicoloAssegnato() {

		List<Prenotazione> prenList = prenotazioneDAO.findByDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		// 🔥 PRENDI SOLO LE PRENOTAZIONI ATTIVE (stato = ATTIVA)
		var attuale = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> !p.getDataInizio().isAfter(now)) // dataInizio <= now
				.filter(p -> p.getDataFine().isAfter(now)) // dataFine > now
				.findFirst();

		attuale.ifPresentOrElse(p -> lblVeicoloAssegnato.setText(p.getTarga()),
				() -> lblVeicoloAssegnato.setText("Nessuno"));
	}

	// ============================================================
	// NAVIGAZIONE
	// ============================================================
	@FXML
	private void onApriPrenotazioni() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/prenotazioni/PrenotazioniView.fxml");
		((PrenotazioniController) ctrl).setUtente(utente);
	}

	@FXML
	private void onNuovaPrenotazione() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/prenotazioni/NuovaPrenotazioneView.fxml");
		((NuovaPrenotazioneController) ctrl).setUtente(utente);
	}

	@FXML
	private void onLogout() {
		SceneManager.changeScene("/ui/views/LoginView.fxml");
	}
}
