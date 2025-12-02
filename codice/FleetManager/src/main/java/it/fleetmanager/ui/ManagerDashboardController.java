package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.veicoli.VeicoliController;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller per la dashboard del Manager. Riceve l'utente dal LoginController
 * e mostra le statistiche principali.
 */
public class ManagerDashboardController {

	// Riferimenti ai label del FXML
	@FXML
	private Label lblTotVeicoli;

	@FXML
	private Label lblPrenotazioniAttive;

	@FXML
	private Label lblManutenzioni;

	// DAOs
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());

	private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());

	private final ManutenzioneDAO manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());

	// Utente loggato
	private Utente utente;

	/**
	 * Metodo chiamato dal LoginController dopo il cambio di scena.
	 */
	public void setUtente(Utente utente) {
		this.utente = utente;
		aggiornaStatistiche();
	}

	/**
	 * Aggiorna le statistiche della dashboard manager.
	 */
	private void aggiornaStatistiche() {
		try {
			int totVeicoli = veicoloDAO.getTuttiVeicoli().size();
			int prenAttive = prenotazioneDAO.findByStato(StatoPrenotazione.ATTIVA).size();
			int manutenzioni = manutenzioneDAO.getTutteManutenzioni().size();

			lblTotVeicoli.setText(String.valueOf(totVeicoli));
			lblPrenotazioniAttive.setText(String.valueOf(prenAttive));
			lblManutenzioni.setText(String.valueOf(manutenzioni));

		} catch (Exception e) {
			System.err.println("[ERROR] Errore caricamento statistiche: " + e.getMessage());
		}
	}

	// ------------------------------------------------------------------------
	// METODI PER I PULSANTI
	// ------------------------------------------------------------------------

	@FXML
	private void onGestisciVeicoli() {
		VeicoliController controller =
			    SceneManager.changeSceneWithController("/ui/views/veicoli/VeicoliView.fxml");

			controller.setUtente(utente);

	}


	@FXML
	private void onGestisciPrenotazioni() {
		System.out.println("TODO: apri gestione prenotazioni");
		// SceneManager.changeScene("/it/fleetmanager/ui/PrenotazioniView.fxml");
	}

	@FXML
	private void onGestisciManutenzioni() {
		System.out.println("TODO: apri gestione manutenzioni");
		// SceneManager.changeScene("/it/fleetmanager/ui/ManutenzioniView.fxml");
	}

	@FXML
	private void onLogout() {
		SceneManager.changeScene("/it/fleetmanager/ui/LoginView.fxml");
	}
}
