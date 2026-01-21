package it.fleetmanager.ui.prenotazioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class NuovaPrenotazioneController implements UserAwareController {

    @FXML private ComboBox<Veicolo> comboVeicoli;
    @FXML private DatePicker dateInizio;
    @FXML private DatePicker dateFine;
    @FXML private TextField oraInizio;
    @FXML private TextField oraFine;

    private Utente utente;

    //SOLO FACADE 
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    @Override
    public void setUtente(Utente u) {
        this.utente = u;

        // se la view è già inizializzata, carico subito
        if (comboVeicoli != null) {
            caricaVeicoliDisponibili();
        }
    }

    private void caricaVeicoliDisponibili() {
        comboVeicoli.getItems().setAll(ui.getTuttiVeicoli());
    }

    @FXML
    private void onConferma() {

        if (comboVeicoli.getValue() == null
                || dateInizio.getValue() == null
                || dateFine.getValue() == null
                || oraInizio.getText().isBlank()
                || oraFine.getText().isBlank()) {

            alertErrore("Compila tutti i campi.");
            return;
        }

        try {
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

            //stessa logica, ma passa dalla facade
            ui.creaPrenotazione(utente, v, dtInizio, dtFine);

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
        SceneManager.changeScene("/ui/views/prenotazioni/PrenotazioniView.fxml", utente);
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
        // se l'utente è già stato settato prima dell'initialize, carico subito i veicoli
        if (utente != null) {
            caricaVeicoliDisponibili();
        }
    }
}
