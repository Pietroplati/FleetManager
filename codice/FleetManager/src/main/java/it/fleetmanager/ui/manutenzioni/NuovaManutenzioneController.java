package it.fleetmanager.ui.manutenzioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.*;
import it.fleetmanager.repository.impl.*;
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
    @FXML private TextArea txtDescrizione;

    private Utente utenteLoggato;

    private final H2DatabaseManager db = H2DatabaseManager.getInstance();
    private final ManutenzioneDAO manutDAO = new ManutenzioneDAOImpl(db);
    private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
    private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);
    private final UtenteDAO utenteDAO = new UtenteDAOImpl(db);

    private final SistemaNotifiche sistemaNotifiche =
            new SistemaNotifiche(notificaDAO, utenteDAO);

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
            if (data == null)
                throw new IllegalArgumentException("Inserire una data.");

            LocalTime oraInizio = LocalTime.parse(txtOraInizio.getText());
            LocalDateTime inizio = LocalDateTime.of(data, oraInizio);

            Veicolo v = veicoloDAO.getVeicoloByTarga(cbTarga.getValue());

            gestoreManut.programmareManutenzione(
                    v,
                    inizio,
                    cbTipo.getValue(),
                    txtDescrizione.getText()
            );

            var ctrl = (ManutenzioniController)
                    SceneManager.changeSceneWithController(
                            "/ui/views/manutenzioni/ManutenzioniView.fxml");
            ctrl.setUtente(utenteLoggato);

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Errore");
            a.setContentText(e.getMessage());
            a.show();
        }
    }

    @FXML
    private void onAnnulla() {
        var ctrl = (ManutenzioniController)
                SceneManager.changeSceneWithController(
                        "/ui/views/manutenzioni/ManutenzioniView.fxml");
        ctrl.setUtente(utenteLoggato);
    }
}
