package it.fleetmanager.repository.dao;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Scadenza;

/**
 * Data Access Object (DAO) per la gestione delle {@link Scadenza}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * interrogazione delle scadenze associate ai veicoli.
 * </p>
 */
public interface ScadenzaDAO {

	/**
	 * Salva una nuova scadenza nel sistema.
	 *
	 * @param scadenza la scadenza da salvare
	 */
	void save(Scadenza scadenza);

	/**
	 * Aggiorna una scadenza esistente.
	 *
	 * @param scadenza la scadenza con i dati aggiornati
	 */
	void update(Scadenza scadenza);

	/**
	 * Elimina una scadenza identificata dal suo id.
	 *
	 * @param idScadenza identificativo della scadenza
	 */
	void delete(int idScadenza);

	/**
	 * Restituisce una scadenza a partire dal suo identificativo.
	 *
	 * @param idScadenza identificativo della scadenza
	 * @return la scadenza trovata, oppure un oggetto sentinella se non esiste
	 */
	Scadenza getScadenzaById(int idScadenza);

	/**
	 * Restituisce le scadenze imminenti fino a una data specificata.
	 *
	 * @param finoA data limite (inclusa) per la ricerca delle scadenze
	 * @return lista delle scadenze comprese tra la data corrente e la data indicata
	 */
	List<Scadenza> findProssimeScadenze(LocalDate finoA);

	/**
	 * Restituisce tutte le scadenze associate a un veicolo.
	 *
	 * @param targa targa del veicolo
	 * @return lista delle scadenze del veicolo, ordinata per data
	 */
	List<Scadenza> findByVeicolo(String targa);

	/**
	 * Restituisce tutte le scadenze presenti nel sistema.
	 *
	 * @return lista completa delle scadenze
	 */
	List<Scadenza> getTutteScadenze();
}
