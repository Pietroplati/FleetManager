package it.fleetmanager.ui.prenotazioni;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PrenotazioniController implements UserAwareController {

    @FXML private TableView<Prenotazione> tablePrenotazioni;
    @FXML private TableColumn<Prenotazione, String> colId, colVeicolo, colDriver, colInizio, colFine, colStato, colTipo;

    @FXML private Button btnNuova, btnConferma, btnCompleta, btnAnnulla;
    @FXML private Label lblDescrizioneRuolo;
    @FXML private ProgressIndicator loadingIndicator;

    private Utente utenteLoggato;

    //SOLO FACADE
    private final UiFacade ui = AppContext.getInstance().getUiFacade();

    private final Map<Integer, Utente> cacheUtenti = new HashMap<>();
    private PrenotazioniViewHelper view;

    @Override
    public void setUtente(Utente user) {
        this.utenteLoggato = user;

        // helper UI
        this.view = new PrenotazioniViewHelper(cacheUtenti);

        ui.aggiornaStatiPrenotazioni();

        caricaCacheUtenti();

        view.configuraColonne(colId, colVeicolo, colDriver, colInizio, colFine, colStato, colTipo);
        view.impostaUIByRuolo(utenteLoggato, lblDescrizioneRuolo, btnNuova, btnConferma, btnCompleta);
        view.aggiornaBottoni(utenteLoggato, null, btnConferma, btnCompleta, btnAnnulla);

        tablePrenotazioni.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        caricaDati();

        tablePrenotazioni.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> view.aggiornaBottoni(utenteLoggato, newSel, btnConferma, btnCompleta, btnAnnulla));
    }

    private void caricaCacheUtenti() {
        cacheUtenti.clear();
        cacheUtenti.putAll(ui.getUtentiById());
    }

    private void caricaDati() {
        List<Prenotazione> lista = ui.getPrenotazioniVisibiliOrdinare(utenteLoggato);
        tablePrenotazioni.getItems().setAll(lista);
    }

    @FXML
    private void onNuovaPrenotazione() {
        SceneManager.changeScene("/ui/views/prenotazioni/NuovaPrenotazioneView.fxml", utenteLoggato);
    }

    @FXML
    private void onConferma() {
        Prenotazione sel = getSelOrError();
        if (sel == null) return;

        ui.confermaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
        view.mostraInfo("Prenotazione confermata.");
        caricaDati();
    }

    @FXML
    private void onCompleta() {
        Prenotazione sel = getSelOrError();
        if (sel == null) return;

        ui.completaPrenotazione(sel.getIdPrenotazione());
        view.mostraInfo("Prenotazione completata.");
        caricaDati();
    }

    @FXML
    private void onAnnulla() {
        Prenotazione sel = getSelOrError();
        if (sel == null) return;

        ui.annullaPrenotazione(sel.getIdPrenotazione(), utenteLoggato);
        view.mostraInfo("Prenotazione annullata.");
        caricaDati();
    }

    private Prenotazione getSelOrError() {
        Prenotazione p = tablePrenotazioni.getSelectionModel().getSelectedItem();
        if (p == null) view.mostraErrore("Seleziona una prenotazione.");
        return p;
    }

    @FXML
    private void onRefreshClick() {
        loadingIndicator.setVisible(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                ui.aggiornaStatiPrenotazioni();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            caricaCacheUtenti();
            caricaDati();
            loadingIndicator.setVisible(false);
        });

        task.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            view.mostraErrore("Errore durante l'aggiornamento.");
        });

        new Thread(task).start();
    }

    @FXML
    private void onBack() {
        if (view.isManager(utenteLoggato)) {
            SceneManager.changeScene("/ui/views/dashboards/ManagerDashboard.fxml", utenteLoggato);
        } else {
            SceneManager.changeScene("/ui/views/dashboards/DriverDashboard.fxml", utenteLoggato);
        }
    }
}
