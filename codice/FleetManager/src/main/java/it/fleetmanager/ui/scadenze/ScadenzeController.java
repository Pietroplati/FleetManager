package it.fleetmanager.ui.scadenze;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScadenzeController implements UserAwareController {

    @FXML private TableView<Scadenza> tableScadenze;

    @FXML private TableColumn<Scadenza, String> colId;
    @FXML private TableColumn<Scadenza, String> colTarga;
    @FXML private TableColumn<Scadenza, String> colTipo;
    @FXML private TableColumn<Scadenza, String> colData;
    @FXML private TableColumn<Scadenza, String> colNotificata;

    @FXML private Label lblInfoUtente;

    //SOLO FACADE
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Utente utente;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("it", "IT"));

    @Override
    public void setUtente(Utente u) {
        this.utente = u;
        lblInfoUtente.setText("Elenco scadenze del sistema");
        caricaScadenze();
    }

    @FXML
    private void initialize() {
        tableScadenze.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
    }

    private void setupColumns() {
        colId.setCellValueFactory(s -> new SimpleStringProperty(String.valueOf(s.getValue().getIdScadenza())));
        colTarga.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getTarga()));
        colTipo.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getTipoScadenza().name()));
        colData.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getData().format(fmt)));
        colNotificata.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getNotificata() ? "SI" : "NO"));
    }

    private void caricaScadenze() {
        List<Scadenza> lista = ui.getTutteScadenze();
        tableScadenze.getItems().setAll(lista);
    }

    private Scadenza getSel() {
        Scadenza s = tableScadenze.getSelectionModel().getSelectedItem();
        if (s == null) mostraErrore("Seleziona una scadenza dalla tabella.");
        return s;
    }

    @FXML
    private void onAggiungi() {
        apriFormScadenza(null);
    }

    @FXML
    private void onModifica() {
        Scadenza s = getSel();
        if (s == null) return;
        apriFormScadenza(s);
    }

    @FXML
    private void onElimina() {
        Scadenza sc = getSel();
        if (sc == null) return;

        Alert a = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Sei sicuro di voler eliminare questa scadenza?",
                ButtonType.YES, ButtonType.NO
        );

        if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            ui.eliminaScadenza(sc.getIdScadenza());
            caricaScadenze();
        }
    }

    @FXML
    private void onBack() {
        SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utente);
    }

    private void mostraErrore(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void mostraInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void apriFormScadenza(Scadenza scadenza) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/scadenze/ScadenzaForm.fxml"));

            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();

            stage.setTitle("Gestione Scadenza");
            stage.setScene(scene);
            stage.initOwner(tableScadenze.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            ScadenzaFormController ctrl = loader.getController();

            if (scadenza == null) {
                ctrl.nuovaScadenza();
            } else {
                ctrl.modificaScadenza(scadenza);
            }

            stage.showAndWait();
            caricaScadenze();

        } catch (Exception e) {
            e.printStackTrace();
            mostraErrore("Errore apertura form scadenza.");
        }
    }
}
