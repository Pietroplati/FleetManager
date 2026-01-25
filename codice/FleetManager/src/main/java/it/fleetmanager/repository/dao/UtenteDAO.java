package it.fleetmanager.repository.dao;

import java.util.List;

import it.fleetmanager.model.Utente;

/**
 * Data Access Object (DAO) per la gestione degli {@link Utente}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * recupero degli utenti del sistema.
 * </p>
 */
public interface UtenteDAO {

	/**
	 * Salva un nuovo utente nel sistema.
	 *
	 * @param utente l'utente da salvare
	 */
	void save(Utente utente);

	/**
	 * Aggiorna un utente esistente.
	 *
	 * @param utente l'utente con i dati aggiornati
	 */
	void update(Utente utente);

	/**
	 * Elimina un utente identificato dal suo id.
	 *
	 * @param id identificativo dell'utente
	 */
	void delete(int id);

	/**
	 * Restituisce un utente a partire dal suo identificativo.
	 *
	 * @param id identificativo dell'utente
	 * @return l'utente trovato, oppure un oggetto sentinella se non esiste
	 */
	Utente getUtenteById(int id);

	/**
	 * Restituisce un utente a partire dal suo indirizzo email.
	 *
	 * @param email email dell'utente
	 * @return l'utente trovato, oppure un oggetto sentinella se non esiste
	 */
	Utente getUtenteByEmail(String email);

	/**
	 * Verifica se esiste già un utente registrato con l'email indicata.
	 *
	 * @param email email da verificare
	 * @return {@code true} se esiste un utente con l'email specificata,
	 *         {@code false} altrimenti
	 */
	boolean existsByEmail(String email);

	/**
	 * Restituisce tutti gli utenti presenti nel sistema.
	 *
	 * @return lista completa degli utenti
	 */
	List<Utente> getTuttiUtenti();

	/**
	 * Restituisce un utente con ruolo di manager.
	 * <p>
	 * Se sono presenti più manager, viene restituito il primo in base
	 * all'ordinamento predefinito.
	 * </p>
	 *
	 * @return l'utente manager, oppure un oggetto sentinella se non presente
	 */
	Utente getManager();
}
