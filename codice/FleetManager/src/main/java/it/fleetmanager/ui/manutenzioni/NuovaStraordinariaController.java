package it.fleetmanager.ui.manutenzioni;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NuovaStraordinariaController implements UserAwareController {

    @FXML private ComboBox<String> cbTarga;
    @FXML private TextArea txtDescrizione;

    private Utente utenteLoggato;

    //SOLO facade
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    @Override
    public void setUtente(Utente u) {
        this.utenteLoggato = u;

        cbTarga.getItems().clear();
        for (Veicolo v : ui.getTuttiVeicoli()) {
            cbTarga.getItems().add(v.getTarga());
        }
    }

    @FXML
    private void onConferma() {
        try {
            String targa = cbTarga.getValue();
            if (targa == null || targa.isBlank()) {
                throw new IllegalArgumentException("Seleziona una targa.");
            }

            String descrizione = txtDescrizione.getText();
            Veicolo v = ui.getVeicoloByTarga(targa);

            ui.segnalareInterventoStraordinario(v, descrizione);

            SceneManager.changeScene("/ui/views/manutenzioni/ManutenzioniView.fxml", utenteLoggato);

        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onAnnulla() {
        SceneManager.changeScene("/ui/views/manutenzioni/ManutenzioniView.fxml", utenteLoggato);
    }

    private void mostraErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.show();
    }
}
