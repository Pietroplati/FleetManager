package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.prenotazioni.NuovaPrenotazioneController;
import it.fleetmanager.ui.prenotazioni.PrenotazioniController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller per la dashboard del Driver. Mostra informazioni dell'utente
 * loggato e offre funzioni rapide.
 */
public class DriverDashboardController {

    // Riferimenti al FXML
    @FXML
    private Label lblNome;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblPatente;

    // Utente loggato
    private Utente utente;

    // DAO necessari
    private final PrenotazioneDAO prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());

    /**
     * Richiamato dal LoginController dopo il cambio scena.
     */
    public void setUtente(Utente utente) {
        this.utente = utente;
        aggiornaDatiUtente();
    }

    /**
     * Aggiorna le informazioni del driver nella UI.
     */
    private void aggiornaDatiUtente() {
        if (utente == null) {
            System.err.println("ERRORE: utente nullo in DriverDashboardController");
            return;
        }

        lblNome.setText(utente.getNome() + " " + utente.getCognome());
        lblEmail.setText(utente.getEmail());
        lblPatente.setText("Patente: " + utente.getPatente());
    }

    // ------------------------------------------------------------------------
    // METODI PER I PULSANTI
    // ------------------------------------------------------------------------

    @FXML
    private void onApriPrenotazioni() {

        var ctrl = SceneManager.changeSceneWithController(
            "/ui/views/prenotazioni/PrenotazioniView.fxml"
        );

        ((PrenotazioniController) ctrl).setUtente(utente);
    }


    @FXML
    private void onNuovaPrenotazione() {

        var ctrl = SceneManager.changeSceneWithController(
            "/ui/views/prenotazioni/NuovaPrenotazioneView.fxml"
        );

        ((NuovaPrenotazioneController) ctrl).setUtente(utente);
    }



    @FXML
    private void onLogout() {
        SceneManager.changeScene("/ui/views/LoginView.fxml");
    }
}
