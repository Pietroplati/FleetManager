package it.fleetmanager.ui.prenotazioni;

import java.time.format.DateTimeFormatter;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.ui.DriverDashboardController;
import it.fleetmanager.ui.ManagerDashboardController;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrenotazioniController {

    // UI ELEMENTS
    @FXML private TableView<Prenotazione> tablePrenotazioni;

    @FXML private TableColumn<Prenotazione, String> colId;
    @FXML private TableColumn<Prenotazione, String> colVeicolo;
    @FXML private TableColumn<Prenotazione, String> colDriver;
    @FXML private TableColumn<Prenotazione, String> colInizio;
    @FXML private TableColumn<Prenotazione, String> colFine;
    @FXML private TableColumn<Prenotazione, String> colStato;
    @FXML private TableColumn<Prenotazione, String> colTipo;

    @FXML private Button btnNuova;
    @FXML private Button btnConferma;
    @FXML private Button btnCompleta;
    @FXML private Button btnAnnulla;
    @FXML private Label lblDescrizioneRuolo;

    // VARIABILI DI CONTROLLER
    private Utente utenteLoggato;

    // DAO
    private final H2DatabaseManager db = H2DatabaseManager.getInstance();
    private final PrenotazioneDAO prenDAO = new PrenotazioneDAOImpl(db);
    private final UtenteDAO utenteDAO = new UtenteDAOImpl(db);
    private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(db);

    // ➤ AGGIUNGERE QUESTA RIGA (NotificaDAO)
    private final NotificaDAO notificaDAO = new NotificaDAOImpl(db);

    // SERVIZIO PRENOTAZIONI (con sistema notifiche corretto)
    private final GestorePrenotazioniImpl gestorePrenotazioni =
            new GestorePrenotazioniImpl(prenDAO, veicoloDAO, new SistemaNotifiche(notificaDAO));

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    // METODO CHIAMATO DALLA DASHBOARD
    public void setUtente(Utente user) {
        this.utenteLoggato = user;
        configuraUI();
        caricaDati();
    }


    // CONFIGURAZIONE UI
    private void configuraUI() {

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdPrenotazione())));
        colVeicolo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTarga()));

        colDriver.setCellValueFactory(c -> {
            Utente u = utenteDAO.getUtenteById(c.getValue().getIdUtente());
            return new SimpleStringProperty(u.getNome() + " " + u.getCognome());
        });

        colInizio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().format(fmt)));
        colFine.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().format(fmt)));
        colStato.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStato().name()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPrenotazione().name()));

        if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
            lblDescrizioneRuolo.setText("Visualizzazione completa prenotazioni - Manager");
            btnConferma.setVisible(true);
            btnCompleta.setVisible(true);
            btnNuova.setVisible(false);
        } else {
            lblDescrizioneRuolo.setText("Le mie prenotazioni");
            btnNuova.setVisible(true);
            btnConferma.setVisible(false);
            btnCompleta.setVisible(false);
        }
    }


    // CARICA DATI IN TABELLA
    private void caricaDati() {

        if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {
            tablePrenotazioni.getItems().setAll(prenDAO.findByStato(StatoPrenotazione.ATTIVA));
            tablePrenotazioni.getItems().addAll(prenDAO.findByStato(StatoPrenotazione.CONFERMATA));
            tablePrenotazioni.getItems().addAll(prenDAO.findByStato(StatoPrenotazione.RICHIESTA));
            tablePrenotazioni.getItems().addAll(prenDAO.findByStato(StatoPrenotazione.COMPLETATA));
            tablePrenotazioni.getItems().addAll(prenDAO.findByStato(StatoPrenotazione.ANNULLATA));
        } else {
            tablePrenotazioni.getItems().setAll(prenDAO.findByDriver(utenteLoggato.getIdUtente()));
        }
    }


    // BOTTONI UI
    @FXML
    private void onNuovaPrenotazione() {

        var ctrl = SceneManager.changeSceneWithController(
            "/ui/views/prenotazioni/NuovaPrenotazioneView.fxml"
        );

        ((NuovaPrenotazioneController) ctrl).setUtente(utenteLoggato);
    }


    @FXML
    private void onConferma() {
        Prenotazione sel = tablePrenotazioni.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostraErrore("Seleziona una prenotazione.");
            return;
        }

        try {
            gestorePrenotazioni.confermaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
            mostraInfo("Prenotazione confermata.");
            caricaDati();
        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onCompleta() {
        Prenotazione sel = tablePrenotazioni.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostraErrore("Seleziona una prenotazione.");
            return;
        }

        try {
            gestorePrenotazioni.completaPrenotazione(sel.getIdPrenotazione());
            mostraInfo("Prenotazione completata.");
            caricaDati();
        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onAnnulla() {
        Prenotazione sel = tablePrenotazioni.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostraErrore("Seleziona una prenotazione.");
            return;
        }

        try {
            gestorePrenotazioni.annullaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
            mostraInfo("Prenotazione annullata.");
            caricaDati();
        } catch (Exception e) {
            mostraErrore(e.getMessage());
        }
    }

    @FXML
    private void onBack() {

        if (utenteLoggato == null) {
            System.err.println("ERRORE: utenteLoggato è null in onBack()");
            return;
        }

        if (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER) {

            ManagerDashboardController ctrl =
                (ManagerDashboardController) SceneManager.changeSceneWithController(
                    "/ui/views/ManagerDashboard.fxml"
                );

            ctrl.setUtente(utenteLoggato);

        } else {

            DriverDashboardController ctrl =
                (DriverDashboardController) SceneManager.changeSceneWithController(
                    "/ui/views/DriverDashboard.fxml"
                );

            ctrl.setUtente(utenteLoggato);
        }
    }






    // ALERT
    private void mostraErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.show();
    }

    private void mostraInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Operazione completata");
        a.setContentText(msg);
        a.show();
    }
}
