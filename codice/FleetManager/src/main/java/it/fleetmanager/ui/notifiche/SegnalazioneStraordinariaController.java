package it.fleetmanager.ui.notifiche;

import java.util.List;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.UserAwareController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SegnalazioneStraordinariaController implements UserAwareController {

    @FXML private ComboBox<Veicolo> comboVeicoli;
    @FXML private TextArea txtDescrizione;

    //solo Facade
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Utente driver;

    @Override
    public void setUtente(Utente utente) {
        this.driver = utente;
        caricaVeicoli();
    }

    private void caricaVeicoli() {

        List<Veicolo> list = ui.getTuttiVeicoli();
        comboVeicoli.getItems().setAll(list);

        comboVeicoli.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Veicolo v, boolean empty) {
                super.updateItem(v, empty);
                setText(formatVeicolo(v, empty));
            }
        });

        comboVeicoli.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Veicolo v, boolean empty) {
                super.updateItem(v, empty);
                setText(formatVeicolo(v, empty));
            }
        });
    }

    private String formatVeicolo(Veicolo v, boolean empty) {
        if (empty || v == null) return "";
        return v.getTarga() + " - " + v.getMarca() + " " + v.getModello();
    }

    @FXML
    private void onInvia() {

        Veicolo selezionato = comboVeicoli.getValue();
        String descrizione = (txtDescrizione.getText() == null) ? "" : txtDescrizione.getText().trim();

        if (selezionato == null) {
            mostraErrore("Seleziona un veicolo.");
            return;
        }

        if (descrizione.isEmpty()) {
            mostraErrore("Inserisci una descrizione del problema.");
            return;
        }

        try {
            ui.inviaSegnalazioneStraordinaria(driver, selezionato, descrizione);
            mostraInfo("Segnalazione inviata correttamente.");
            chiudiFinestra();
        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onAnnulla() {
        chiudiFinestra();
    }

    private void chiudiFinestra() {
        Stage stage = (Stage) txtDescrizione.getScene().getWindow();
        stage.close();
    }

    private void mostraErrore(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void mostraInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
