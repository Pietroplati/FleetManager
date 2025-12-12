package it.fleetmanager.ui.dashboards;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;

import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;

import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;

import it.fleetmanager.repository.util.H2DatabaseManager;

import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.manutenzioni.ManutenzioniController;
import it.fleetmanager.ui.notifiche.NotificheController;
import it.fleetmanager.ui.prenotazioni.PrenotazioniController;
import it.fleetmanager.ui.scadenze.ScadenzeController;
import it.fleetmanager.ui.veicoli.VeicoliController;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoNotifica;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class ManagerDashboardController {

	// ===================== UI ELEMENTS =====================
	@FXML
	private Label lblTotVeicoli;
	@FXML
	private Label lblPrenotazioniAttive;
	@FXML
	private Label lblManutenzioni;

	@FXML
	private Button btnNotifiche;
	@FXML
	private Circle badgeNotifiche;

	// ===================== DAO =====================
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());
	private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());
	private final ManutenzioneDAO manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());
	private final NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
	private final ScadenzaDAO scadenzaDAO = new ScadenzaDAOImpl(H2DatabaseManager.getInstance());
	// ===================== SISTEMA NOTIFICHE =====================
	private final SistemaNotifiche sistemaNotifiche =
	        new SistemaNotifiche(notificaDAO, new UtenteDAOImpl(H2DatabaseManager.getInstance()));

	
	// ===================== UTENTE CORRENTE =====================
	private Utente utente;

	// ============================================================
	// SET UTENTE
	// ============================================================
	public void setUtente(Utente utente) {
		this.utente = utente;

		aggiornaWidgets();
		aggiornaBadgeNotifiche();
		controllaScadenze(); // 🔥 NUOVO: controllo automatico scadenze
	}

	// ============================================================
	// AGGIORNA WIDGETS
	// ============================================================
	private void aggiornaWidgets() {
		aggiornaVeicoli();
		aggiornaPrenotazioniAttive();
		aggiornaManutenzioni();
	}

	private void aggiornaVeicoli() {
		try {
			lblTotVeicoli.setText(String.valueOf(veicoloDAO.getTuttiVeicoli().size()));
		} catch (Exception e) {
			lblTotVeicoli.setText("ERR");
		}
	}

	private void aggiornaPrenotazioniAttive() {
		try {
			lblPrenotazioniAttive.setText(String.valueOf(prenotazioneDAO.findByStato(StatoPrenotazione.ATTIVA).size()));
		} catch (Exception e) {
			lblPrenotazioniAttive.setText("ERR");
		}
	}

	private void aggiornaManutenzioni() {
		try {
			lblManutenzioni.setText(String.valueOf(manutenzioneDAO.getTutteManutenzioni().size()));
		} catch (Exception e) {
			lblManutenzioni.setText("ERR");
		}
	}

	// ============================================================
	// BADGE NOTIFICHE
	// ============================================================
	private void aggiornaBadgeNotifiche() {
		List<Notifica> nonLette = notificaDAO.findNonLette(utente.getIdUtente());
		badgeNotifiche.setVisible(!nonLette.isEmpty());
	}

	// ============================================================
	// 🔥 CONTROLLO AUTOMATICO SCADENZE
	// ============================================================
	private void controllaScadenze() {
		try {
			List<Scadenza> scadenze = scadenzaDAO.getTutteScadenze();

			LocalDate oggi = LocalDate.now();
			LocalDate limite = oggi.plusDays(7);

			for (Scadenza s : scadenze) {

				// già notificata → salta
				if (s.getNotificata())
					continue;

				// scade entro 7 giorni?
				if (!s.getData().isAfter(limite)) {

					// 🔥 usa SistemaNotifiche (stile uniforme, messaggio già pronto)
					sistemaNotifiche.inviaNotificaScadenza(s);

					// marca come notificata
					s.setNotificata(true);
					scadenzaDAO.update(s);
				}
			}

			aggiornaBadgeNotifiche();

		} catch (Exception e) {
			System.err.println("ERRORE controllo scadenze: " + e.getMessage());
		}
	}

	// ============================================================
	// NAVIGAZIONE
	// ============================================================
	@FXML
	private void onGestisciVeicoli() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/veicoli/VeicoliView.fxml");
		((VeicoliController) ctrl).setUtente(utente);
	}

	@FXML
	private void onGestisciPrenotazioni() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/prenotazioni/PrenotazioniView.fxml");
		((PrenotazioniController) ctrl).setUtente(utente);
	}

	@FXML
	private void onGestisciManutenzioni() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/manutenzioni/ManutenzioniView.fxml");
		((ManutenzioniController) ctrl).setUtente(utente);
	}

	@FXML
	private void onGestisciScadenze() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/scadenze/ScadenzeView.fxml");
		((ScadenzeController) ctrl).setUtente(utente);
	}

	@FXML
	private void onApriNotifiche() {
		var ctrl = SceneManager.changeSceneWithController("/ui/views/notifiche/NotificheView.fxml");
		((NotificheController) ctrl).setUtente(utente);
	}

	@FXML
	private void onLogout() {
		SceneManager.changeScene("/ui/views/LoginView.fxml");
	}
}
