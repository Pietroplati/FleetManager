package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;

/**
 * Interfaccia di servizio per la gestione delle {@link Prenotazione} nel
 * sistema FleetManager.
 * <p>
 * Questa interfaccia definisce il contratto delle operazioni di business
 * relative alle prenotazioni dei veicoli: creazione, validazione disponibilità,
 * conferma/annullamento, attivazione e completamento, oltre alle operazioni di
 * consultazione per driver e manager. Le implementazioni concrete si occupano
 * della logica applicativa e delegano la persistenza allo strato
 * repository/DAO.
 * </p>
 */
public interface GestorePrenotazioni {

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
	 * Verifica la disponibilità di un veicolo per un intervallo temporale.
	 *
	 * @param veicolo    veicolo da verificare
	 * @param dataInizio data e ora di inizio intervallo
	 * @param dataFine   data e ora di fine intervallo
	 * @return {@code true} se il veicolo risulta disponibile nell'intervallo
	 *         richiesto; {@code false} altrimenti
	 */
	boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

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
	 * Restituisce le prenotazioni associate a un determinato driver.
	 *
	 * @param driver utente driver
	 * @return lista delle prenotazioni del driver (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Prenotazione> getPrenotazioniDriver(Utente driver);

	/**
	 * Restituisce le prenotazioni associate a un determinato veicolo.
	 *
	 * @param veicolo veicolo di cui recuperare le prenotazioni
	 * @return lista delle prenotazioni del veicolo (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo);

	/**
	 * Attiva una prenotazione esistente.
	 * <p>
	 * L'operazione comporta l'aggiornamento dello stato della prenotazione e può
	 * influire sullo stato del veicolo associato.
	 * </p>
	 *
	 * @param idPrenotazione identificativo della prenotazione da attivare
	 */
	void attivaPrenotazione(int idPrenotazione);

	/**
	 * Completa una prenotazione esistente.
	 * <p>
	 * L'operazione comporta l'aggiornamento dello stato della prenotazione e può
	 * influire sullo stato del veicolo associato.
	 * </p>
	 *
	 * @param idPrenotazione identificativo della prenotazione da completare
	 */
	void completaPrenotazione(int idPrenotazione);

	/**
	 * Aggiorna gli stati delle prenotazioni in base alle regole temporali e di
	 * business.
	 * <p>
	 * Tipicamente utilizzato per sincronizzare lo stato delle prenotazioni con il
	 * passare del tempo (ad esempio prenotazioni che diventano attive o
	 * completate).
	 * </p>
	 */
	void aggiornaStatiPrenotazioni();

	/**
	 * Restituisce tutte le prenotazioni presenti nel sistema.
	 * <p>
	 * Metodo esposto per evitare l'accesso diretto ai DAO dalla UI.
	 * </p>
	 *
	 * @return lista completa delle prenotazioni (eventualmente vuota, mai
	 *         {@code null})
	 */
	List<Prenotazione> getTuttePrenotazioni();

	/**
	 * Restituisce le prenotazioni visibili all'utente loggato: tutte se l'utente è
	 * un manager, solo le proprie se l'utente è un driver. Le prenotazioni sono
	 * restituite già ordinate secondo le regole applicative.
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
}
