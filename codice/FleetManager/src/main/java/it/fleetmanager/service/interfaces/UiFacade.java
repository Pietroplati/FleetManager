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

/**
 * Interfaccia di facciata (Facade) per l'interazione tra livello UI e logica
 * applicativa del sistema FleetManager.
 * <p>
 * Questa interfaccia espone un insieme di operazioni di alto livello utilizzate
 * dai controller JavaFX per gestire veicoli, prenotazioni, manutenzioni,
 * scadenze, notifiche e utenti. Le implementazioni concrete orchestrano i vari
 * gestori di servizio e delegano la persistenza allo strato repository/DAO.
 * </p>
 */
public interface UiFacade {

	// ===== VEICOLI =====

	/**
	 * Restituisce l'elenco completo dei veicoli presenti nel sistema.
	 *
	 * @return lista dei veicoli (eventualmente vuota, mai {@code null})
	 */
	List<Veicolo> getTuttiVeicoli();

	/**
	 * Restituisce un veicolo a partire dalla targa.
	 *
	 * @param targa targa del veicolo
	 * @return il veicolo trovato; {@code null} se non esiste
	 */
	Veicolo getVeicoloByTarga(String targa);

	/**
	 * Aggiorna i dati di un veicolo esistente.
	 *
	 * @param v veicolo con i dati aggiornati
	 */
	void aggiornaVeicolo(Veicolo v);

	/**
	 * Elimina un veicolo identificato dalla targa.
	 *
	 * @param targa targa del veicolo da eliminare
	 */
	void eliminaVeicolo(String targa);

	// ===== PRENOTAZIONI =====

	/**
	 * Restituisce le prenotazioni associate a un driver.
	 *
	 * @param idDriver identificativo del driver
	 * @return lista delle prenotazioni del driver (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Prenotazione> getPrenotazioniDriver(int idDriver);

	/**
	 * Restituisce le prenotazioni filtrate per stato.
	 *
	 * @param stato stato della prenotazione
	 * @return lista delle prenotazioni con lo stato indicato (eventualmente vuota,
	 *         mai {@code null})
	 */
	List<Prenotazione> getPrenotazioniByStato(StatoPrenotazione stato);

	/**
	 * Crea una nuova prenotazione per un driver e un veicolo in un determinato
	 * intervallo temporale.
	 *
	 * @param driver     utente che effettua la prenotazione (driver)
	 * @param veicolo    veicolo da prenotare
	 * @param dataInizio data e ora di inizio prenotazione
	 * @param dataFine   data e ora di fine prenotazione
	 * @return la prenotazione creata e registrata nel sistema
	 */
	Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

	/**
	 * Conferma una prenotazione esistente tramite un manager.
	 *
	 * @param idPrenotazione identificativo della prenotazione da confermare
	 * @param manager        utente che effettua la conferma (manager)
	 */
	void confermaPrenotazione(int idPrenotazione, Utente manager);

	/**
	 * Annulla una prenotazione esistente.
	 *
	 * @param idPrenotazione identificativo della prenotazione da annullare
	 * @param utente         utente che richiede l'annullamento (driver o manager a
	 *                       seconda delle regole di business)
	 */
	void annullaPrenotazione(int idPrenotazione, Utente utente);

	/**
	 * Completa una prenotazione esistente.
	 *
	 * @param idPrenotazione identificativo della prenotazione da completare
	 */
	void completaPrenotazione(int idPrenotazione);

	/**
	 * Aggiorna gli stati delle prenotazioni in base alle regole temporali e di
	 * business.
	 */
	void aggiornaStatiPrenotazioni();

	// ===== MANUTENZIONI =====

	/**
	 * Restituisce l'elenco completo delle manutenzioni presenti nel sistema.
	 *
	 * @return lista completa delle manutenzioni (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Manutenzione> getTutteManutenzioni();

	/**
	 * Chiude una manutenzione esistente.
	 *
	 * @param idManutenzione identificativo della manutenzione da chiudere
	 */
	void chiudiManutenzione(int idManutenzione);

	/**
	 * Programma una nuova manutenzione ordinaria per un veicolo.
	 *
	 * @param v           veicolo per il quale viene programmata la manutenzione
	 * @param inizio      data e ora di inizio della manutenzione
	 * @param tipo        tipo di manutenzione da programmare
	 * @param descrizione descrizione dell'intervento di manutenzione
	 */
	void programmareManutenzione(Veicolo v, LocalDateTime inizio, TipoManutenzione tipo, String descrizione);

	/**
	 * Segnala un intervento di manutenzione straordinaria per un veicolo.
	 *
	 * @param v           veicolo per il quale viene segnalato l'intervento
	 * @param descrizione descrizione del problema riscontrato
	 */
	void segnalareInterventoStraordinario(Veicolo v, String descrizione);

	// ===== SCADENZE =====

	/**
	 * Restituisce l'elenco completo delle scadenze presenti nel sistema.
	 *
	 * @return lista completa delle scadenze (eventualmente vuota, mai {@code null})
	 */
	List<Scadenza> getTutteScadenze();

	/**
	 * Elimina una scadenza identificata dal suo id.
	 *
	 * @param idScadenza identificativo della scadenza da eliminare
	 */
	void eliminaScadenza(int idScadenza);

	/**
	 * Esegue il controllo delle scadenze e invia le notifiche necessarie.
	 * <p>
	 * Metodo esposto per evitare che la UI contenga logica applicativa: in
	 * precedenza questa operazione era gestita nel controller.
	 * </p>
	 */
	void controllaScadenzeENotifica();

	// ===== NOTIFICHE =====

	/**
	 * Restituisce tutte le notifiche presenti nel sistema.
	 *
	 * @return lista completa delle notifiche (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Notifica> getTutteNotifiche();

	/**
	 * Restituisce le notifiche associate a un utente.
	 *
	 * @param idUtente identificativo dell'utente
	 * @return lista delle notifiche dell'utente (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Notifica> getNotificheByUtente(int idUtente);

	/**
	 * Restituisce le notifiche non lette associate a un utente.
	 *
	 * @param idUtente identificativo dell'utente
	 * @return lista delle notifiche non lette (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Notifica> getNotificheNonLette(int idUtente);

	/**
	 * Aggiorna una notifica esistente (ad esempio marcandola come letta).
	 *
	 * @param n notifica con i dati aggiornati
	 */
	void aggiornaNotifica(Notifica n);

	/**
	 * Restituisce le notifiche visibili a un utente: tutte se l'utente è un
	 * manager, solo le proprie se l'utente è un driver.
	 *
	 * @param u utente di riferimento
	 * @return lista delle notifiche visibili (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Notifica> getNotifichePerUtente(Utente u);

	/**
	 * Invia una segnalazione straordinaria relativa a un veicolo, effettuata da un
	 * driver.
	 *
	 * @param driver      utente che effettua la segnalazione (driver)
	 * @param veicolo     veicolo oggetto della segnalazione
	 * @param descrizione descrizione del problema riscontrato
	 */
	void inviaSegnalazioneStraordinaria(Utente driver, Veicolo veicolo, String descrizione);

	// ===== SALVATAGGI SUPPORTO =====

	/**
	 * Salva una scadenza nel sistema.
	 *
	 * @param s scadenza da salvare
	 */
	void salvaScadenza(Scadenza s);

	/**
	 * Salva un veicolo nel sistema.
	 *
	 * @param v veicolo da salvare
	 */
	void salvaVeicolo(Veicolo v);

	/**
	 * Restituisce le prenotazioni visibili all'utente loggato (tutte se manager,
	 * solo proprie se driver), già ordinate secondo le regole applicative.
	 *
	 * @param utenteLoggato utente per il quale calcolare la visibilità delle
	 *                      prenotazioni
	 * @return lista delle prenotazioni visibili, già ordinata (eventualmente vuota,
	 *         mai {@code null})
	 */
	List<Prenotazione> getPrenotazioniVisibiliOrdinare(Utente utenteLoggato);

	/**
	 * Restituisce una cache utile alla UI per risolvere rapidamente gli utenti a
	 * partire dal loro id.
	 *
	 * @return mappa {@code idUtente -> Utente} (eventualmente vuota, mai
	 *         {@code null})
	 */
	Map<Integer, Utente> getUtentiById();

	// ===== UTENTI =====

	/**
	 * Restituisce l'elenco completo dei driver presenti nel sistema.
	 *
	 * @return lista dei driver (eventualmente vuota, mai {@code null})
	 */
	List<Utente> getTuttiDriver();

	/**
	 * Crea un nuovo utente nel sistema.
	 *
	 * @param u utente da creare
	 */
	void creaUtente(Utente u);

	/**
	 * Aggiorna un utente esistente.
	 *
	 * @param u utente con i dati aggiornati
	 */
	void aggiornaUtente(Utente u);

	/**
	 * Elimina un utente identificato dal suo id.
	 *
	 * @param idUtente identificativo dell'utente
	 */
	void eliminaUtente(int idUtente);

	/**
	 * Salva un utente nel sistema.
	 *
	 * @param u utente da salvare
	 */
	void salvaUtente(Utente u);

}
