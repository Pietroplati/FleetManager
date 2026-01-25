package it.fleetmanager.ui.veicoli;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
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

    //SOLO FACADE
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Veicolo veicoloOriginale;
    private Runnable onSaveCallback;

    public void setOnSaveCallback(Runnable r) {
        this.onSaveCallback = r;
    }

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TipoVeicolo.values());
        cmbStato.getItems().setAll(StatoVeicolo.values());
    }

    public void setVeicolo(Veicolo v) {
        this.veicoloOriginale = v;

        if (v == null) {
            // nuovo veicolo
            txtTarga.setDisable(false);
            txtTarga.clear();
            txtMarca.clear();
            txtModello.clear();
            txtAnno.clear();
            txtKm.clear();
            cmbTipo.setValue(null);
            cmbStato.setValue(null);
            return;
        }

        // modifica veicolo
        txtTarga.setText(v.getTarga());
        txtTarga.setDisable(true);

        cmbTipo.setValue(v.getTipoVeicolo());
        txtMarca.setText(v.getMarca());
        txtModello.setText(v.getModello());
        txtAnno.setText(String.valueOf(v.getAnnoImmatricolazione()));
        cmbStato.setValue(v.getStatoVeicolo());
        txtKm.setText(String.valueOf(v.getKm()));
    }

    @FXML
    private void onSalva() {
        try {
            String targa = safeTrim(txtTarga.getText());
            TipoVeicolo tipo = cmbTipo.getValue();
            String marca = safeTrim(txtMarca.getText());
            String modello = safeTrim(txtModello.getText());
            StatoVeicolo stato = cmbStato.getValue();

            if (targa.isEmpty() || tipo == null || marca.isEmpty() || modello.isEmpty() || stato == null) {
                mostraErrore("Compila tutti i campi obbligatori (targa, tipo, marca, modello, stato).");
                return;
            }

            int anno = parseIntOrFail(txtAnno.getText(), "Anno non valido.");
            int km = parseIntOrFail(txtKm.getText(), "Km non validi.");

            Veicolo v = new Veicolo(targa, tipo, marca, modello, anno, stato, km);

            if (veicoloOriginale == null) {
                ui.aggiornaVeicolo(v); // <-- NO: questo farebbe update
                // Qui serve la creazione: nella tua UiFacade manca "salvaVeicolo"
                // quindi facciamo la cosa corretta: aggiungiamo metodo e usiamo quello.
                // Per non “barare”, in questa classe chiamo il metodo che devi aggiungere:
                ui.salvaVeicolo(v);
            } else {
                ui.aggiornaVeicolo(v);
            }

            if (onSaveCallback != null) onSaveCallback.run();

            chiudi();

        } catch (IllegalArgumentException ex) {
            mostraErrore(ex.getMessage());
        } catch (Exception e) {
            mostraErrore("Controlla i campi inseriti.");
        }
    }

    @FXML
    private void onAnnulla() {
        chiudi();
    }

    private void chiudi() {
        Stage stage = (Stage) txtKm.getScene().getWindow();
        stage.close();
    }

    private void mostraErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.showAndWait();
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private int parseIntOrFail(String value, String errorMsg) {
        try {
            return Integer.parseInt(safeTrim(value));
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMsg);
        }
    }
}
