package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;

public interface GestorePrenotazioni {

    Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

    boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

    void confermaPrenotazione(int idPrenotazione, Utente manager);

    void annullaPrenotazione(int idPrenotazione, Utente utente);

    List<Prenotazione> getPrenotazioniDriver(Utente driver);

    List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo);

    void attivaPrenotazione(int idPrenotazione);

    void completaPrenotazione(int idPrenotazione);

    void aggiornaStatiPrenotazioni();

    // (per togliere DAO dalla UI, logica invariata)
    List<Prenotazione> getTuttePrenotazioni();

    List<Utente> getTuttiUtenti();


    /**
     * Restituisce le prenotazioni visibili all'utente (tutte se manager, solo proprie se driver)
     * già ordinate secondo le stesse regole che erano nel controller.
     */
    List<Prenotazione> getPrenotazioniVisibiliOrdinare(Utente utenteLoggato);

    /**
     * Restituisce una cache {idUtente -> Utente} utile alla UI per stampare i nomi.
     */
    Map<Integer, Utente> getUtentiById();
}
