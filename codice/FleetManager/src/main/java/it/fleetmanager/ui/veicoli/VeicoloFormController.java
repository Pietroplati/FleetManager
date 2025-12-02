package it.fleetmanager.ui.veicoli;

import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class VeicoloFormController {

    @FXML private TextField txtTarga;
    @FXML private ComboBox<TipoVeicolo> cmbTipo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModello;
    @FXML private TextField txtAnno;
    @FXML private ComboBox<StatoVeicolo> cmbStato;
    @FXML private TextField txtKm;

    private final VeicoloDAO veicoloDAO =
            new VeicoloDAOImpl(H2DatabaseManager.getInstance());

    private Veicolo veicoloOriginale;

    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable r) {
        this.onSaveCallback = r;
    }

    public void setVeicolo(Veicolo v) {
        this.veicoloOriginale = v;

        cmbTipo.getItems().setAll(TipoVeicolo.values());
        cmbStato.getItems().setAll(StatoVeicolo.values());

        if (v != null) {
            txtTarga.setText(v.getTarga());
            txtTarga.setDisable(true);

            cmbTipo.setValue(v.getTipoVeicolo());
            txtMarca.setText(v.getMarca());
            txtModello.setText(v.getModello());
            txtAnno.setText(String.valueOf(v.getAnnoImmatricolazione()));
            cmbStato.setValue(v.getStatoVeicolo());
            txtKm.setText(String.valueOf(v.getKm()));
        }
    }

    @FXML
    private void onSalva() {

        try {
            String targa = txtTarga.getText();
            TipoVeicolo tipo = cmbTipo.getValue();
            String marca = txtMarca.getText();
            String modello = txtModello.getText();
            int anno = Integer.parseInt(txtAnno.getText());
            int km = Integer.parseInt(txtKm.getText());
            StatoVeicolo stato = cmbStato.getValue();

            Veicolo v = new Veicolo(targa, tipo, marca, modello, anno, stato, km);

            if (veicoloOriginale == null)
                veicoloDAO.save(v);
            else
                veicoloDAO.update(v);

            if (onSaveCallback != null)
                onSaveCallback.run();

            ((Stage) txtKm.getScene().getWindow()).close();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Controlla i campi inseriti.").show();
        }
    }
}
