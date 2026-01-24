package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoManutenzione;

public interface UiFacade {



    // ===== VEICOLI =====
    List<Veicolo> getTuttiVeicoli();
    Veicolo getVeicoloByTarga(String targa);
    void aggiornaVeicolo(Veicolo v);
    void eliminaVeicolo(String targa);


    List<Prenotazione> getPrenotazioniDriver(int idDriver);
    List<Prenotazione> getPrenotazioniByStato(StatoPrenotazione stato);

    Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

    void confermaPrenotazione(int idPrenotazione, Utente manager);
    void annullaPrenotazione(int idPrenotazione, Utente utente);
    void completaPrenotazione(int idPrenotazione);
    void aggiornaStatiPrenotazioni();

    // ===== MANUTENZIONI =====
    List<Manutenzione> getTutteManutenzioni();
    void chiudiManutenzione(int idManutenzione);
    void programmareManutenzione(Veicolo v, LocalDateTime inizio, TipoManutenzione tipo, String descrizione);
    void segnalareInterventoStraordinario(Veicolo v, String descrizione);

    // ===== SCADENZE =====
    List<Scadenza> getTutteScadenze();
    void eliminaScadenza(int idScadenza);
    void controllaScadenzeENotifica(); // prima stava nel ManagerDashboardController

    // ===== NOTIFICHE =====

    // (A) Metodi granulari (comodi per i controller)
    List<Notifica> getTutteNotifiche();
    List<Notifica> getNotificheByUtente(int idUtente);
    List<Notifica> getNotificheNonLette(int idUtente);
    void aggiornaNotifica(Notifica n);

    // (B) Metodi "alto livello" (li teniamo per compatibilità)
    List<Notifica> getNotifichePerUtente(Utente u);   // manager = tutte, driver = sue
    void inviaSegnalazioneStraordinaria(Utente driver, Veicolo veicolo, String descrizione);


    
    void salvaScadenza(Scadenza s);
    void salvaVeicolo(Veicolo v);


	List<Prenotazione> getPrenotazioniVisibiliOrdinare(Utente utenteLoggato);
	Map<Integer, Utente> getUtentiById();
	
	// ===== UTENTI =====

	List<Utente> getTuttiDriver();

	void creaUtente(Utente u);

	void aggiornaUtente(Utente u);

	void eliminaUtente(int idUtente);

	void salvaUtente(Utente u);

    
}
