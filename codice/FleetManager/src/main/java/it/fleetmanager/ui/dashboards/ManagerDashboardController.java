package it.fleetmanager.ui.dashboards;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class ManagerDashboardController implements UserAwareController {

    @FXML private Label lblTotVeicoli;
    @FXML private Label lblPrenotazioniAttive;
    @FXML private Label lblManutenzioni;

    @FXML private Button btnNotifiche;
    @FXML private Circle badgeNotifiche;

    //Solo facade (niente DAO in UI)
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Utente utente;

    @Override
    public void setUtente(Utente utente) {
        this.utente = utente;

        aggiornaWidgets();
        // prima facevi sia badge che controllo scadenze qui:
        ui.controllaScadenzeENotifica();
        aggiornaBadgeNotifiche();
    }

    private void aggiornaWidgets() {
        aggiornaVeicoli();
        aggiornaPrenotazioniAttive();
        aggiornaManutenzioni();
    }

    private void aggiornaVeicoli() {
        try {
            lblTotVeicoli.setText(String.valueOf(ui.getTuttiVeicoli().size()));
        } catch (Exception e) {
            lblTotVeicoli.setText("ERR");
        }
    }

    private void aggiornaPrenotazioniAttive() {
        try {
            lblPrenotazioniAttive.setText(
                    String.valueOf(ui.getPrenotazioniByStato(StatoPrenotazione.ATTIVA).size())
            );
        } catch (Exception e) {
            lblPrenotazioniAttive.setText("ERR");
        }
    }

    private void aggiornaManutenzioni() {
        try {
            lblManutenzioni.setText(String.valueOf(ui.getTutteManutenzioni().size()));
        } catch (Exception e) {
            lblManutenzioni.setText("ERR");
        }
    }

    private void aggiornaBadgeNotifiche() {
        try {
            boolean mostraBadge = ui.getNotifichePerUtente(utente)
                    .stream()
                    .anyMatch(n -> !n.getLetta());

            badgeNotifiche.setVisible(mostraBadge);
        } catch (Exception e) {
            badgeNotifiche.setVisible(false);
        }
    }

 
    @FXML
    private void onGestisciVeicoli() {
        SceneManager.changeScene("/ui/views/veicoli/VeicoliView.fxml", utente);
    }

    @FXML
    private void onGestisciPrenotazioni() {
        SceneManager.changeScene("/ui/views/prenotazioni/PrenotazioniView.fxml", utente);
    }

    @FXML
    private void onGestisciManutenzioni() {
        SceneManager.changeScene("/ui/views/manutenzioni/ManutenzioniView.fxml", utente);
    }

    @FXML
    private void onGestisciScadenze() {
        SceneManager.changeScene("/ui/views/scadenze/ScadenzeView.fxml", utente);
    }

    @FXML
    private void onApriNotifiche() {
        SceneManager.changeScene("/ui/views/notifiche/NotificheView.fxml", utente);
    }

    @FXML
    private void onLogout() {
        SceneManager.changeScene("/ui/views/LoginView.fxml");
    }
    
    @FXML
    private void onGestisciUtenti() {
        SceneManager.changeScene("/ui/views/utenti/UtentiView.fxml", utente);
    }

}
