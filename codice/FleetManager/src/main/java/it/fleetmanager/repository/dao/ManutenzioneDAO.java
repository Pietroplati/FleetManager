package it.fleetmanager.repository.dao;

import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.util.TipoManutenzione;

/**
 * Data Access Object (DAO) per la gestione delle {@link Manutenzione}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * recupero delle manutenzioni dal sistema di storage. Le implementazioni
 * concrete si occupano dei dettagli tecnici di accesso ai dati.
 * </p>
 */
public interface ManutenzioneDAO {

	/**
	 * Salva una nuova manutenzione nel sistema.
	 *
	 * @param manutenzione la manutenzione da salvare
	 */
	void save(Manutenzione manutenzione);

	/**
	 * Aggiorna una manutenzione esistente.
	 *
	 * @param manutenzione la manutenzione con i dati aggiornati
	 */
	void update(Manutenzione manutenzione);

	/**
	 * Elimina una manutenzione identificata dal suo id.
	 *
	 * @param idManutenzione identificativo della manutenzione
	 */
	void delete(int idManutenzione);

	/**
	 * Restituisce una manutenzione a partire dal suo identificativo.
	 *
	 * @param idManutenzione identificativo della manutenzione
	 * @return la manutenzione trovata, oppure un oggetto sentinella se non esiste
	 */
	Manutenzione getManutenzioneById(int idManutenzione);

	/**
	 * Restituisce tutte le manutenzioni associate a un veicolo.
	 *
	 * @param targa targa del veicolo
	 * @return lista delle manutenzioni del veicolo, ordinata
	 */
	List<Manutenzione> findByVeicolo(String targa);

	/**
	 * Restituisce tutte le manutenzioni di un determinato tipo.
	 *
	 * @param tipoManutenzione tipo di manutenzione
	 * @return lista delle manutenzioni del tipo specificato
	 */
	List<Manutenzione> findByTipo(TipoManutenzione tipoManutenzione);

	/**
	 * Restituisce il massimo identificativo di manutenzione presente nel sistema.
	 *
	 * @return valore massimo dell'id delle manutenzioni, oppure 0 se non presenti
	 */
	int getMaxId();

	/**
	 * Restituisce tutte le manutenzioni presenti nel sistema.
	 *
	 * @return lista completa delle manutenzioni
	 */
	List<Manutenzione> getTutteManutenzioni();
}
