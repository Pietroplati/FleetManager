package it.fleetmanager.repository.dao;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.util.StatoPrenotazione;

/**
 * Data Access Object (DAO) per la gestione delle {@link Prenotazione}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * interrogazione delle prenotazioni dei veicoli.
 * </p>
 */
public interface PrenotazioneDAO {

	/**
	 * Salva una nuova prenotazione nel sistema.
	 *
	 * @param prenotazione la prenotazione da salvare
	 */
	void save(Prenotazione prenotazione);

	/**
	 * Aggiorna una prenotazione esistente.
	 *
	 * @param prenotazione la prenotazione con i dati aggiornati
	 */
	void update(Prenotazione prenotazione);

	/**
	 * Elimina una prenotazione identificata dal suo id.
	 *
	 * @param idPrenotazione identificativo della prenotazione
	 */
	void delete(int idPrenotazione);

	/**
	 * Restituisce una prenotazione a partire dal suo identificativo.
	 *
	 * @param idPrenotazione identificativo della prenotazione
	 * @return la prenotazione trovata, oppure un oggetto sentinella se non esiste
	 */
	Prenotazione getById(int idPrenotazione);

	/**
	 * Restituisce tutte le prenotazioni effettuate da un driver.
	 *
	 * @param idDriver identificativo del driver
	 * @return lista delle prenotazioni del driver
	 */
	List<Prenotazione> findByDriver(int idDriver);

	/**
	 * Restituisce tutte le prenotazioni associate a un veicolo.
	 *
	 * @param targa targa del veicolo
	 * @return lista delle prenotazioni del veicolo
	 */
	List<Prenotazione> findByVeicolo(String targa);

	/**
	 * Restituisce tutte le prenotazioni con un determinato stato.
	 *
	 * @param statoPrenotazione stato della prenotazione
	 * @return lista delle prenotazioni con lo stato specificato
	 */
	List<Prenotazione> findByStato(StatoPrenotazione statoPrenotazione);

	/**
	 * Verifica se esiste una prenotazione che si sovrappone all'intervallo
	 * temporale specificato per il veicolo indicato.
	 *
	 * @param targa      targa del veicolo
	 * @param dataInizio data e ora di inizio dell'intervallo
	 * @param dataFine   data e ora di fine dell'intervallo
	 * @return {@code true} se esiste almeno una prenotazione sovrapposta,
	 *         {@code false} altrimenti
	 */
	boolean existsOverlapping(String targa, LocalDateTime dataInizio, LocalDateTime dataFine);

	/**
	 * Restituisce tutte le prenotazioni presenti nel sistema.
	 *
	 * @return lista completa delle prenotazioni
	 */
	List<Prenotazione> findAll();
}
