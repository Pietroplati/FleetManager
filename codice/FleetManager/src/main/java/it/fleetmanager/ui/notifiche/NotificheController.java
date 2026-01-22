package it.fleetmanager.ui.notifiche;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoNotifica;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class NotificheController implements UserAwareController {

    @FXML private TableView<Notifica> tableNotifiche;
    @FXML private TableColumn<Notifica, String> colId;
    @FXML private TableColumn<Notifica, String> colTipo;
    @FXML private TableColumn<Notifica, String> colMessaggio;
    @FXML private TableColumn<Notifica, String> colData;
    @FXML private TableColumn<Notifica, String> colLetta;

    @FXML private Label lblDescrizioneUtente;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button btnVeicoloNonDisponibile;

    // SOLO facade
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private Utente utente;

    private final ObservableList<Notifica> notificheList = FXCollections.observableArrayList();

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("it", "IT"));

    @FXML
    private void initialize() {
        tableNotifiche.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        setupColumns();
        setupSelectionListener();
        btnVeicoloNonDisponibile.setVisible(false);
        btnVeicoloNonDisponibile.setManaged(false);
    }

    @Override
    public void setUtente(Utente u) {
        this.utente = u;
        lblDescrizioneUtente.setText("Notifiche di: " + u.getNome() + " " + u.getCognome());
        caricaNotifiche();
    }

    private void setupColumns() {

        colId.setCellValueFactory(n ->
                new SimpleStringProperty(String.valueOf(n.getValue().getIdNotifica()))
        );
        colTipo.setCellValueFactory(n ->
                new SimpleStringProperty(n.getValue().getTipoNotifica().name())
        );
        colMessaggio.setCellValueFactory(n ->
                new SimpleStringProperty(n.getValue().getMessaggio())
        );
        colData.setCellValueFactory(n ->
                new SimpleStringProperty(n.getValue().getDataInvio().format(fmt))
        );
        colLetta.setCellValueFactory(n ->
                new SimpleStringProperty(n.getValue().getLetta() ? "SI" : "NO")
        );
    }

    private void setupSelectionListener() {

        tableNotifiche.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, sel) -> {

                    if (sel == null || sel.getTipoNotifica() != TipoNotifica.SEGNALAZIONE) {
                        hide(btnVeicoloNonDisponibile);
                        return;
                    }

                    String targa = estraiTarga(sel.getMessaggio());
                    if (targa != null) show(btnVeicoloNonDisponibile);
                    else hide(btnVeicoloNonDisponibile);
                });
    }

    private String estraiTarga(String msg) {
        String regex = "\\b[A-Z]{2}\\d{3}[A-Z]{2}\\b";
        var matcher = java.util.regex.Pattern.compile(regex).matcher(msg);
        return matcher.find() ? matcher.group() : null;
    }

    private void caricaNotifiche() {

        loadingIndicator.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                caricaNotificheInterno();
                return null;
            }
        };

        task.setOnSucceeded(e -> loadingIndicator.setVisible(false));
        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            mostraErrore("Errore durante il caricamento delle notifiche.");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void caricaNotificheInterno() {
        try {
            List<Notifica> tutte;

            // logica identica: manager vede tutte, driver solo le sue
            if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
                tutte = ui.getTutteNotifiche();
            } else {
                tutte = ui.getNotificheByUtente(utente.getIdUtente());
            }

            List<Notifica> ordinate = tutte.stream()
                    .sorted(
                            Comparator.comparing(Notifica::getLetta)
                                    .thenComparing(Notifica::getDataInvio, Comparator.reverseOrder())
                    )
                    .toList(); // <-- FIX SonarLint

            Platform.runLater(() -> {
                notificheList.setAll(ordinate);
                tableNotifiche.setItems(notificheList);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notifica getSel() {
        Notifica n = tableNotifiche.getSelectionModel().getSelectedItem();
        if (n == null) mostraErrore("Seleziona una notifica.");
        return n;
    }

    @FXML
    private void onRefresh() {
        caricaNotifiche();
    }

    @FXML
    private void onSegnaTutteComeLette() {

        try {
            List<Notifica> target;

            if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
                target = ui.getTutteNotifiche();
            } else {
                target = ui.getNotificheNonLette(utente.getIdUtente());
            }

            for (Notifica n : target) {
                if (!n.getLetta()) {
                    n.setLetta(true);
                    ui.aggiornaNotifica(n);
                }
            }

            caricaNotifiche();

        } catch (Exception e) {
            mostraErrore("Errore durante l'aggiornamento delle notifiche.");
        }
    }

    @FXML
    private void onSegnaComeLetta() {

        Notifica n = getSel();
        if (n == null) return;

        if (n.getLetta()) {
            mostraInfo("La notifica è già segnata come letta.");
            return;
        }

        n.setLetta(true);
        ui.aggiornaNotifica(n);

        caricaNotifiche();
    }

    @FXML
    private void onSegnaVeicoloNonDisponibile() {

        Notifica n = getSel();
        if (n == null) return;

        String targa = estraiTarga(n.getMessaggio());
        if (targa == null) {
            mostraErrore("Targa non trovata nella segnalazione.");
            return;
        }

        Veicolo v = ui.getVeicoloByTarga(targa);
        if (v == null || "N/A".equals(v.getTarga())) {
            mostraErrore("Veicolo non trovato nel database.");
            return;
        }

        v.setStatoVeicolo(StatoVeicolo.NON_DISPONIBILE);
        ui.aggiornaVeicolo(v);

        mostraInfo("Il veicolo " + targa + " è stato impostato come NON DISPONIBILE.");
        caricaNotifiche();
    }

    @FXML
    private void onBack() {
        if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
            SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utente);
        } else {
            SceneManager.changeScene("/ui/views/dashboards/DriverDashboard.fxml", utente);
        }
    }

    private void mostraErrore(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void mostraInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void hide(Control c) {
        c.setVisible(false);
        c.setManaged(false);
    }

    private void show(Control c) {
        c.setVisible(true);
        c.setManaged(true);
    }
}
