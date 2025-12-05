package it.fleetmanager.ui.prenotazioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.util.SistemaNotifiche;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NuovaPrenotazioneController {

    @FXML private ComboBox<Veicolo> comboVeicoli;
    @FXML private DatePicker dateInizio;
    @FXML private DatePicker dateFine;
    @FXML private TextField oraInizio;
    @FXML private TextField oraFine;

    private Utente utente;

 // DAO
    private final H2DatabaseManager db = H2DatabaseManager.getInstance();
    private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
    private final PrenotazioneDAO prenDAO = new PrenotazioneDAOImpl(db);
    private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);

    // Sistema notifiche
    private final SistemaNotifiche sistemaNotifiche = new SistemaNotifiche(notificaDAO);

    // Gestore prenotazioni
    private final GestorePrenotazioniImpl gestorePrenotazioni =
            new GestorePrenotazioniImpl(prenDAO, veicoloDAO, sistemaNotifiche);


  
    public void setUtente(Utente u) {
        this.utente = u;

        // se la view è già inizializzata
        if (comboVeicoli != null) {
            caricaVeicoliDisponibili();
        }
    }



    private void caricaVeicoliDisponibili() {
        comboVeicoli.getItems().setAll(veicoloDAO.getTuttiVeicoli());
    }

 
    @FXML
    private void onConferma() {

        if (comboVeicoli.getValue() == null ||
            dateInizio.getValue() == null ||
            dateFine.getValue() == null ||
            oraInizio.getText().isBlank() ||
            oraFine.getText().isBlank()) {

            alertErrore("Compila tutti i campi.");
            return;
        }

        try {
            // Parsing orari
            LocalTime tInizio = LocalTime.parse(oraInizio.getText());
            LocalTime tFine = LocalTime.parse(oraFine.getText());

            LocalDate dInizio = dateInizio.getValue();
            LocalDate dFine = dateFine.getValue();

            LocalDateTime dtInizio = LocalDateTime.of(dInizio, tInizio);
            LocalDateTime dtFine = LocalDateTime.of(dFine, tFine);

            if (dtFine.isBefore(dtInizio)) {
                alertErrore("La data/ora di fine non può essere prima dell’inizio.");
                return;
            }

            Veicolo v = comboVeicoli.getValue();

            gestorePrenotazioni.creaPrenotazione(utente, v, dtInizio, dtFine);

            alertInfo("Prenotazione creata con successo.");

            tornaAllePrenotazioni();

        } catch (Exception e) {
            alertErrore(e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        tornaAllePrenotazioni();
    }

    private void tornaAllePrenotazioni() {

        var ctrl = SceneManager.changeSceneWithController(
            "/ui/views/prenotazioni/PrenotazioniView.fxml"
        );

        ((PrenotazioniController) ctrl).setUtente(utente);
    }


 
    private void alertErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.show();
    }

    private void alertInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Operazione completata");
        a.setContentText(msg);
        a.show();
    }
    
    @FXML
    private void initialize() {
        // Se utente è già stato passato, carichiamo i veicoli
        if (utente != null) {
            caricaVeicoliDisponibili();
        }
    }


}
