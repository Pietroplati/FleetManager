package it.fleetmanager.ui.manutenzioni;

import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.util.SistemaNotifiche;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NuovaStraordinariaController {

    @FXML private ComboBox<String> cbTarga;
    @FXML private TextArea txtDescrizione;

    private Utente utenteLoggato;

    private final H2DatabaseManager db = H2DatabaseManager.getInstance();
    private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);
    private final ManutenzioneDAO manutDAO = new ManutenzioneDAOImpl(db);
    private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);
    private final SistemaNotifiche sistemaNotifiche = new SistemaNotifiche(notificaDAO);

    private final GestoreManutenzioniImpl gestoreManut =
            new GestoreManutenzioniImpl(manutDAO, veicoloDAO, sistemaNotifiche);

    public void setUtente(Utente u) {
        this.utenteLoggato = u;

        for (Veicolo v : veicoloDAO.getTuttiVeicoli()) {
            cbTarga.getItems().add(v.getTarga());
        }
    }

    @FXML
    private void onConferma() {
        try {
            String targa = cbTarga.getValue();
            String descrizione = txtDescrizione.getText();
            Veicolo v = veicoloDAO.getVeicoloByTarga(targa);

            gestoreManut.segnalareInterventoStraordinario(v, descrizione);

            var ctrl = (ManutenzioniController)
                SceneManager.changeSceneWithController("/ui/views/manutenzioni/ManutenzioniView.fxml");
            ctrl.setUtente(utenteLoggato);

        } catch (Exception e) {
            mostraErrore(e.getMessage());
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
