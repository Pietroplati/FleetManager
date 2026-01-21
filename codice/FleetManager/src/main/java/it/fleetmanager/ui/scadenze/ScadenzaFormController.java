package it.fleetmanager.ui.scadenze;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.util.TipoScadenza;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ScadenzaFormController {

    @FXML private Label lblTitolo;
    @FXML private ComboBox<String> cbTarga;
    @FXML private ComboBox<TipoScadenza> cmbTipo;
    @FXML private DatePicker dpData;
    @FXML private CheckBox chkNotificata;

    //SOLO FACADE
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Scadenza scadenza;

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TipoScadenza.values());

        //targhe via facade
        ui.getTuttiVeicoli().forEach(v -> cbTarga.getItems().add(v.getTarga()));
    }

    public void nuovaScadenza() {
        scadenza = null;
        lblTitolo.setText("Nuova Scadenza");
        cbTarga.setDisable(false);
        cbTarga.setValue(null);
        cmbTipo.setValue(null);
        dpData.setValue(null);
        chkNotificata.setSelected(false);
    }

    public void modificaScadenza(Scadenza s) {
        this.scadenza = s;
        lblTitolo.setText("Modifica Scadenza");

        cbTarga.setValue(s.getTarga());
        cmbTipo.setValue(s.getTipoScadenza());
        dpData.setValue(s.getData());
        chkNotificata.setSelected(s.getNotificata());

        cbTarga.setDisable(true);
    }

    @FXML
    private void onSalva() {

        if (cbTarga.getValue() == null || cmbTipo.getValue() == null || dpData.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Compila tutti i campi obbligatori").show();
            return;
        }

        if (scadenza == null) {
            scadenza = new Scadenza();
        }

        scadenza.setTarga(cbTarga.getValue());
        scadenza.setTipoScadenza(cmbTipo.getValue());
        scadenza.setData(dpData.getValue());
        scadenza.setNotificata(chkNotificata.isSelected());

        //save/update spostato nel facade
        ui.salvaScadenza(scadenza);

        chiudi();
    }

    @FXML
    private void onAnnulla() {
        chiudi();
    }

    private void chiudi() {
        ((Stage) cbTarga.getScene().getWindow()).close();
    }
}
