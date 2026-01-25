package it.fleetmanager.ui.manutenzioni;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.util.TipoManutenzione;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NuovaManutenzioneController implements UserAwareController {

    @FXML private ComboBox<String> cbTarga;
    @FXML private ComboBox<TipoManutenzione> cbTipo;
    @FXML private DatePicker dpData;
    @FXML private TextField txtOraInizio;
    @FXML private TextArea txtDescrizione;

    private Utente utenteLoggato;

    //SOLO facade
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    @Override
    public void setUtente(Utente u) {
        this.utenteLoggato = u;

        cbTipo.getItems().setAll(TipoManutenzione.values());
        cbTarga.getItems().clear();

        for (Veicolo v : ui.getTuttiVeicoli()) {
            cbTarga.getItems().add(v.getTarga());
        }
    }

    @FXML
    private void onConferma() {
        try {
            LocalDate data = dpData.getValue();
            if (data == null) {
                throw new IllegalArgumentException("Inserire una data.");
            }

            LocalTime oraInizio = LocalTime.parse(txtOraInizio.getText());
            LocalDateTime inizio = LocalDateTime.of(data, oraInizio);

            String targa = cbTarga.getValue();
            if (targa == null || targa.isBlank()) {
                throw new IllegalArgumentException("Seleziona una targa.");
            }

            TipoManutenzione tipo = cbTipo.getValue();
            if (tipo == null) {
                throw new IllegalArgumentException("Seleziona un tipo manutenzione.");
            }

            Veicolo v = ui.getVeicoloByTarga(targa);

            ui.programmareManutenzione(
                    v,
                    inizio,
                    tipo,
                    txtDescrizione.getText()
            );

            SceneManager.changeScene("/ui/views/manutenzioni/ManutenzioniView.fxml", utenteLoggato);

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Errore");
            a.setContentText(e.getMessage());
            a.show();
        }
    }

    @FXML
    private void onAnnulla() {
        SceneManager.changeScene("/ui/views/manutenzioni/ManutenzioniView.fxml", utenteLoggato);
    }
}
