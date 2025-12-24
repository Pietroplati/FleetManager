package it.fleetmanager.ui.notifiche;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;

import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;

import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.ui.SceneManager;

import it.fleetmanager.ui.dashboards.DriverDashboardController;
import it.fleetmanager.ui.dashboards.ManagerDashboardController;

import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoNotifica;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

public class NotificheController {

    // ============================================================
    // UI
    // ============================================================
    @FXML
    private TableView<Notifica> tableNotifiche;

    @FXML
    private TableColumn<Notifica, String> colId;
    @FXML
    private TableColumn<Notifica, String> colTipo;
    @FXML
    private TableColumn<Notifica, String> colMessaggio;
    @FXML
    private TableColumn<Notifica, String> colData;
    @FXML
    private TableColumn<Notifica, String> colLetta;

    @FXML
    private Label lblDescrizioneUtente;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Button btnVeicoloNonDisponibile;

    // ============================================================
    // DAO
    // ============================================================
    private final NotificaDAO notificaDAO =
            new NotificaDAOImpl(H2DatabaseManager.getInstance());

    private final VeicoloDAO veicoloDAO =
            new VeicoloDAOImpl(H2DatabaseManager.getInstance());

    private Utente utente;
    private final ObservableList<Notifica> notificheList =
            FXCollections.observableArrayList();

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("it", "IT"));

    // ============================================================
    // INITIALIZE
    // ============================================================
    @FXML
    private void initialize() {
        tableNotifiche.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        setupSelectionListener();
        btnVeicoloNonDisponibile.setVisible(false);
    }

    // ============================================================
    // SET UTENTE
    // ============================================================
    public void setUtente(Utente u) {
        this.utente = u;
        lblDescrizioneUtente.setText(
                "Notifiche di: " + u.getNome() + " " + u.getCognome()
        );
        caricaNotifiche();
    }

    // ============================================================
    // CONFIGURA COLONNE
    // ============================================================
    private void setupColumns() {

        colId.setCellValueFactory(
                n -> new SimpleStringProperty(String.valueOf(n.getValue().getIdNotifica()))
        );

        colTipo.setCellValueFactory(
                n -> new SimpleStringProperty(n.getValue().getTipoNotifica().name())
        );

        colMessaggio.setCellValueFactory(
                n -> new SimpleStringProperty(n.getValue().getMessaggio())
        );

        colData.setCellValueFactory(
                n -> new SimpleStringProperty(n.getValue().getDataInvio().format(fmt))
        );

        colLetta.setCellValueFactory(
                n -> new SimpleStringProperty(n.getValue().getLetta() ? "SI" : "NO")
        );
    }

    // ============================================================
    // LISTENER SELEZIONE
    // ============================================================
    private void setupSelectionListener() {

        tableNotifiche.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, sel) -> {

                    if (sel == null || sel.getTipoNotifica() != TipoNotifica.SEGNALAZIONE) {
                        btnVeicoloNonDisponibile.setVisible(false);
                        return;
                    }

                    String targa = estraiTarga(sel.getMessaggio());
                    btnVeicoloNonDisponibile.setVisible(targa != null);
                });
    }

    // ============================================================
    // ESTRARRE TARGA
    // ============================================================
    private String estraiTarga(String msg) {
        String regex = "\\b[A-Z]{2}\\d{3}[A-Z]{2}\\b";
        var matcher = java.util.regex.Pattern.compile(regex).matcher(msg);
        return matcher.find() ? matcher.group() : null;
    }

    // ============================================================
    // CARICAMENTO NOTIFICHE
    // ============================================================
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

        new Thread(task).start();
    }

    /**
     * 🔥 LOGICA DEFINITIVA:
     * - MANAGER → tutte le notifiche
     * - DRIVER  → solo le proprie
     */
    private void caricaNotificheInterno() {
        try {
            List<Notifica> tutte;

            if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
                tutte = notificaDAO.findAll();
            } else {
                tutte = notificaDAO.findByUtente(utente.getIdUtente());
            }

            List<Notifica> ordinate = tutte.stream()
                    .sorted(
                            Comparator.comparing(Notifica::getLetta)
                                    .thenComparing(Notifica::getDataInvio, Comparator.reverseOrder())
                    )
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                notificheList.setAll(ordinate);
                tableNotifiche.setItems(notificheList);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // SUPPORTO
    // ============================================================
    private Notifica getSel() {
        Notifica n = tableNotifiche.getSelectionModel().getSelectedItem();
        if (n == null)
            mostraErrore("Seleziona una notifica.");
        return n;
    }

    // ============================================================
    // REFRESH
    // ============================================================
    @FXML
    private void onRefresh() {
        caricaNotifiche();
    }

    // ============================================================
    // SEGNA TUTTE COME LETTE
    // ============================================================
    @FXML
    private void onSegnaTutteComeLette() {

        try {
            List<Notifica> target;

            if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
                target = notificaDAO.findAll();
            } else {
                target = notificaDAO.findNonLette(utente.getIdUtente());
            }

            for (Notifica n : target) {
                if (!n.getLetta()) {
                    n.setLetta(true);
                    notificaDAO.update(n);
                }
            }

            caricaNotifiche();

        } catch (Exception e) {
            mostraErrore("Errore durante l'aggiornamento delle notifiche.");
        }
    }

    // ============================================================
    // SEGNA COME LETTA
    // ============================================================
    @FXML
    private void onSegnaComeLetta() {

        Notifica n = getSel();
        if (n == null)
            return;

        if (n.getLetta()) {
            mostraInfo("La notifica è già segnata come letta.");
            return;
        }

        n.setLetta(true);
        notificaDAO.update(n);

        caricaNotifiche();
    }

    // ============================================================
    // IMPOSTA VEICOLO NON DISPONIBILE
    // ============================================================
    @FXML
    private void onSegnaVeicoloNonDisponibile() {

        Notifica n = getSel();
        if (n == null)
            return;

        String targa = estraiTarga(n.getMessaggio());
        if (targa == null) {
            mostraErrore("Targa non trovata nella segnalazione.");
            return;
        }

        Veicolo v = veicoloDAO.getVeicoloByTarga(targa);
        if (v == null || "N/A".equals(v.getTarga())) {
            mostraErrore("Veicolo non trovato nel database.");
            return;
        }

        v.setStatoVeicolo(StatoVeicolo.NON_DISPONIBILE);
        veicoloDAO.update(v);

        mostraInfo("Il veicolo " + targa + " è stato impostato come NON DISPONIBILE.");
        caricaNotifiche();
    }

    // ============================================================
    // BACK
    // ============================================================
    @FXML
    private void onBack() {

        if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {

            ManagerDashboardController ctrl =
                    SceneManager.changeSceneWithController(
                            "/ui/views/dashboards/ManagerDashboard.fxml");

            ctrl.setUtente(utente);

        } else {

            DriverDashboardController ctrl =
                    SceneManager.changeSceneWithController(
                            "/ui/views/dashboards/DriverDashboard.fxml");

            ctrl.setUtente(utente);
        }
    }

    // ============================================================
    // ALERT
    // ============================================================
    private void mostraErrore(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void mostraInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
