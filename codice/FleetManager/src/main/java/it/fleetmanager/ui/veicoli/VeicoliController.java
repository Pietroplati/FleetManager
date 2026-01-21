package it.fleetmanager.ui.veicoli;

import java.util.List;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VeicoliController implements UserAwareController {

    @FXML private TableView<Veicolo> tableVeicoli;

    @FXML private TableColumn<Veicolo, String> colTarga;
    @FXML private TableColumn<Veicolo, String> colTipo;
    @FXML private TableColumn<Veicolo, String> colMarca;
    @FXML private TableColumn<Veicolo, String> colModello;
    @FXML private TableColumn<Veicolo, Integer> colAnno;
    @FXML private TableColumn<Veicolo, String> colStato;
    @FXML private TableColumn<Veicolo, Integer> colKm;

    private final ObservableList<Veicolo> veicoliList =
            FXCollections.observableArrayList();

    //SOLO FACADE
    private final UiFacade ui =
            AppContext.getInstance().getUiFacade();

    private Utente utente;

    @FXML
    private void initialize() {

        tableVeicoli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colTarga.setCellValueFactory(v ->
                new SimpleStringProperty(v.getValue().getTarga()));

        colTipo.setCellValueFactory(v ->
                new SimpleStringProperty(
                        v.getValue().getTipoVeicolo().name()));

        colMarca.setCellValueFactory(v ->
                new SimpleStringProperty(v.getValue().getMarca()));

        colModello.setCellValueFactory(v ->
                new SimpleStringProperty(v.getValue().getModello()));

        colAnno.setCellValueFactory(v ->
                new SimpleIntegerProperty(
                        v.getValue().getAnnoImmatricolazione()).asObject());

        colStato.setCellValueFactory(v ->
                new SimpleStringProperty(
                        v.getValue().getStatoVeicolo().name()));

        colKm.setCellValueFactory(v ->
                new SimpleIntegerProperty(
                        v.getValue().getKm()).asObject());

        caricaVeicoli();
    }

    private void caricaVeicoli() {
        List<Veicolo> list = ui.getTuttiVeicoli();
        veicoliList.setAll(list);
        tableVeicoli.setItems(veicoliList);
    }

    @Override
    public void setUtente(Utente u) {
        this.utente = u;
    }

    @FXML
    private void onAggiungi() {
        apriForm(null);
    }

    @FXML
    private void onModifica() {
        Veicolo selezionato = tableVeicoli.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            mostraAlert("Seleziona un veicolo dalla tabella.");
            return;
        }
        apriForm(selezionato);
    }

    @FXML
    private void onElimina() {
        Veicolo selezionato = tableVeicoli.getSelectionModel().getSelectedItem();
        if (selezionato == null) {
            mostraAlert("Seleziona un veicolo.");
            return;
        }

        ui.eliminaVeicolo(selezionato.getTarga());
        caricaVeicoli();
    }

    @FXML
    private void onBack() {
        SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utente);
    }

    private void apriForm(Veicolo veicoloDaModificare) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/views/veicoli/VeicoloFormView.fxml"));

            Scene scene = new Scene(loader.load());

            VeicoloFormController controller = loader.getController();
            controller.setVeicolo(veicoloDaModificare);
            controller.setOnSaveCallback(this::caricaVeicoli);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Dettaglio Veicolo");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostraAlert("Errore apertura form veicolo.");
        }
    }

    private void mostraAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
