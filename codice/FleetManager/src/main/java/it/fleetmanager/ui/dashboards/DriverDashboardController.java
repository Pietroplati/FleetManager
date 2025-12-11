package it.fleetmanager.ui.dashboards;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.prenotazioni.NuovaPrenotazioneController;
import it.fleetmanager.ui.prenotazioni.PrenotazioniController;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class DriverDashboardController {

	// ===== UI =====
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

	@FXML
	private Button btnNotifiche;
	@FXML
	private Circle badgeNotifiche;

	// ===== DAOs =====
	private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());

	private final NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());

	// ===== UTENTE =====
	private Utente utente;

	// ============================================================
	// IMPOSTA L’UTENTE DOPO IL LOGIN
	// ============================================================
	public void setUtente(Utente utente) {
		this.utente = utente;

		aggiornaDatiUtente();
		aggiornaWidgets();
		aggiornaBadgeNotifiche();
	}

	// ============================================================
	// DATI UTENTE
	// ============================================================
	private void aggiornaDatiUtente() {
		lblNome.setText(utente.getNome() + " " + utente.getCognome());
		lblEmail.setText(utente.getEmail());
		lblPatente.setText("Patente: " + utente.getPatente());
	}

	// ============================================================
	// WIDGETS DASHBOARD
	// ============================================================
	private void aggiornaWidgets() {
		aggiornaPrenotazioniAttive();
		aggiornaProssimaPrenotazione();
		aggiornaVeicoloAssegnato();
	}

	private void aggiornaPrenotazioniAttive() {
		long count = prenotazioneDAO.findByDriver(utente.getIdUtente()).stream()
				.filter(p -> p.getStato() == StatoPrenotazione.ATTIVA).count();

		lblPrenotazioniAttive.setText(String.valueOf(count));
	}

	private void aggiornaProssimaPrenotazione() {

		List<Prenotazione> prenList = prenotazioneDAO.findByDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("it", "IT"));

		// -- Se esiste prenotazione ATTIVA che finisce presto
		var attiva = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> p.getDataFine().isAfter(now)).min(Comparator.comparing(Prenotazione::getDataFine));

		if (attiva.isPresent()) {
			lblProssimaPrenotazione.setText(attiva.get().getDataFine().format(fmt));
			return;
		}

		// -- Altrimenti la confermata più vicina a iniziare
		var confermata = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.CONFERMATA)
				.filter(p -> p.getDataInizio().isAfter(now)).min(Comparator.comparing(Prenotazione::getDataInizio));

		if (confermata.isPresent()) {
			lblProssimaPrenotazione.setText(confermata.get().getDataInizio().format(fmt));
			return;
		}

		lblProssimaPrenotazione.setText("Nessuna");
	}

	private void aggiornaVeicoloAssegnato() {

		List<Prenotazione> prenList = prenotazioneDAO.findByDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		var attuale = prenList.stream().filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> !p.getDataInizio().isAfter(now)).filter(p -> p.getDataFine().isAfter(now)).findFirst();

		attuale.ifPresentOrElse(p -> lblVeicoloAssegnato.setText(p.getTarga()),
				() -> lblVeicoloAssegnato.setText("Nessuno"));
	}

	// ============================================================
	// 🔴 BADGE NOTIFICHE (APPARE SOLO SE CI SONO NON LETTE)
	// ============================================================
	private void aggiornaBadgeNotifiche() {

		List<Notifica> nonLette = notificaDAO.findNonLette(utente.getIdUtente());
		badgeNotifiche.setVisible(!nonLette.isEmpty());
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
	private void onSegnalazioneStraordinaria() {
		SceneManager.changeScene("/ui/views/segnalazioni/SegnalazioneStraordinariaView.fxml");
	}

	@FXML
	private void onApriNotifiche() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/notifiche/NotificheView.fxml");
		((it.fleetmanager.ui.notifiche.NotificheController) ctrl).setUtente(utente);
	}

	@FXML
	private void onLogout() {
		SceneManager.changeScene("/ui/views/LoginView.fxml");
	}
}
