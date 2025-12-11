package it.fleetmanager.ui.manutenzioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.TipoManutenzione;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NuovaManutenzioneController {

    @FXML private ComboBox<String> cbTarga;
    @FXML private ComboBox<TipoManutenzione> cbTipo;
    @FXML private DatePicker dpData;
    @FXML private TextField txtOraInizio;
    @FXML private TextField txtOraFine;
    @FXML private TextArea txtDescrizione;

    private Utente utenteLoggato;

    private final H2DatabaseManager db = H2DatabaseManager.getInstance();
    private final ManutenzioneDAO manutDAO = new ManutenzioneDAOImpl(db);
    private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
    private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);
    private final UtenteDAO utenteDAO = new UtenteDAOImpl(db);

    private final SistemaNotifiche sistemaNotifiche = new SistemaNotifiche(notificaDAO, utenteDAO);

    private final GestoreManutenzioniImpl gestoreManut =
            new GestoreManutenzioniImpl(manutDAO, veicoloDAO, sistemaNotifiche);

    public void setUtente(Utente u) {
        this.utenteLoggato = u;
        cbTipo.getItems().setAll(TipoManutenzione.values());

        for (Veicolo v : veicoloDAO.getTuttiVeicoli()) {
            cbTarga.getItems().add(v.getTarga());
        }
    }

    @FXML
    private void onConferma() {
        try {
            LocalDate data = dpData.getValue();
            if (data == null) throw new IllegalArgumentException("Inserire una data.");

            LocalTime oraInizio = parseOra(txtOraInizio.getText());
            LocalTime oraFine = parseOra(txtOraFine.getText());

            if (oraFine.isBefore(oraInizio))
                throw new IllegalArgumentException("L'orario di fine è precedente all'inizio.");

            LocalDateTime inizio = LocalDateTime.of(data, oraInizio);
            LocalDateTime fine = LocalDateTime.of(data, oraFine);

            String targa = cbTarga.getValue();
            String descrizione = txtDescrizione.getText();
            TipoManutenzione tipo = cbTipo.getValue();

            Veicolo v = veicoloDAO.getVeicoloByTarga(targa);

            // Salvataggio
            gestoreManut.programmareManutenzione(v, data, tipo, descrizione);

            // Torna alla tabella manutenzioni
            var ctrl = (ManutenzioniController)
                SceneManager.changeSceneWithController("/ui/views/manutenzioni/ManutenzioniView.fxml");
            ctrl.setUtente(utenteLoggato);

        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    private LocalTime parseOra(String s) {
        try {
            return LocalTime.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Orario non valido (usa HH:mm)");
        }
    }

    @FXML
    private void onAnnulla() {
        var ctrl = (ManutenzioniController)
            SceneManager.changeSceneWithController("/ui/views/manutenzioni/ManutenzioniView.fxml");
        ctrl.setUtente(utenteLoggato);
    }

    private void mostraErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.show();
    }
}
