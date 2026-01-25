package it.fleetmanager.ui.prenotazioni;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;

public class PrenotazioniViewHelper {

    private final Map<Integer, Utente> cacheUtenti;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PrenotazioniViewHelper(Map<Integer, Utente> cacheUtenti) {
        this.cacheUtenti = cacheUtenti;
    }

    public void configuraColonne(
            TableColumn<Prenotazione, String> colId,
            TableColumn<Prenotazione, String> colVeicolo,
            TableColumn<Prenotazione, String> colDriver,
            TableColumn<Prenotazione, String> colInizio,
            TableColumn<Prenotazione, String> colFine,
            TableColumn<Prenotazione, String> colStato,
            TableColumn<Prenotazione, String> colTipo
    ) {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdPrenotazione())));
        colVeicolo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTarga()));

        colDriver.setCellValueFactory(c -> {
            Utente u = cacheUtenti.get(c.getValue().getIdUtente());
            String nome = (u != null) ? (u.getNome() + " " + u.getCognome()) : "N/D";
            return new SimpleStringProperty(nome);
        });

        colInizio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInizio().format(fmt)));
        colFine.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFine().format(fmt)));
        colStato.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStato().name()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPrenotazione().name()));
    }

    public boolean isManager(Utente utenteLoggato) {
        return utenteLoggato != null && utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER;
    }

    public void impostaUIByRuolo(Utente utenteLoggato, Label lblDescrizioneRuolo, Button btnNuova, Button btnConferma, Button btnCompleta) {
        boolean isManager = isManager(utenteLoggato);

        lblDescrizioneRuolo.setText(isManager
                ? "Visualizzazione completa prenotazioni - Manager"
                : "Le mie prenotazioni"
        );

        setVisible(btnNuova, !isManager);
        setVisible(btnConferma, isManager);
        setVisible(btnCompleta, isManager);
    }

    public void aggiornaBottoni(
            Utente utenteLoggato,
            Prenotazione p,
            Button btnConferma,
            Button btnCompleta,
            Button btnAnnulla
    ) {
        // default “sicuro”
        btnAnnulla.setDisable(true);

        if (p == null) {
            aggiornaAzioniManager(utenteLoggato, false, false, btnConferma, btnCompleta);
            return;
        }

        StatoPrenotazione stato = p.getStato();

        if (isManager(utenteLoggato)) {
            boolean confermabile = stato == StatoPrenotazione.RICHIESTA;
            boolean completabile = stato == StatoPrenotazione.ATTIVA;
            boolean annullabile  = (stato == StatoPrenotazione.RICHIESTA || stato == StatoPrenotazione.CONFERMATA);

            aggiornaAzioniManager(utenteLoggato, confermabile, completabile, btnConferma, btnCompleta);
            btnAnnulla.setDisable(!annullabile);
            return;
        }

        // DRIVER
        aggiornaAzioniManager(utenteLoggato, false, false, btnConferma, btnCompleta);

        boolean isOwner = p.getIdUtente() == utenteLoggato.getIdUtente();
        boolean annullabile = isOwner && (stato == StatoPrenotazione.RICHIESTA || stato == StatoPrenotazione.CONFERMATA);
        btnAnnulla.setDisable(!annullabile);
    }

    private void aggiornaAzioniManager(
            Utente utenteLoggato,
            boolean confermabile,
            boolean completabile,
            Button btnConferma,
            Button btnCompleta
    ) {
        boolean isManager = isManager(utenteLoggato);

        setVisible(btnConferma, isManager && confermabile);
        btnConferma.setDisable(!confermabile);

        setVisible(btnCompleta, isManager && completabile);
        btnCompleta.setDisable(!completabile);
    }

    public void mostraErrore(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Errore");
        a.setContentText(msg);
        a.show();
    }

    public void mostraInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Operazione completata");
        a.setContentText(msg);
        a.show();
    }

    public void setVisible(Control c, boolean value) {
        c.setVisible(value);
        c.setManaged(value);
    }
}
