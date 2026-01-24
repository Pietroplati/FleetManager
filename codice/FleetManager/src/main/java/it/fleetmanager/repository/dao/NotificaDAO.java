package it.fleetmanager.repository.dao;

import java.util.List;

import it.fleetmanager.model.Notifica;

/**
 * Data Access Object (DAO) per la gestione delle {@link Notifica}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * interrogazione delle notifiche associate agli utenti del sistema.
 * </p>
 */
public interface NotificaDAO {

	/**
	 * Salva una nuova notifica nel sistema.
	 *
	 * @param notifica la notifica da salvare
	 */
	void save(Notifica notifica);

	/**
	 * Aggiorna una notifica esistente.
	 *
	 * @param notifica la notifica con i dati aggiornati
	 */
	void update(Notifica notifica);

	/**
	 * Elimina una notifica identificata dal suo id.
	 *
	 * @param idNotifica identificativo della notifica
	 */
	void delete(int idNotifica);

	/**
	 * Restituisce una notifica a partire dal suo identificativo.
	 *
	 * @param idNotifica identificativo della notifica
	 * @return la notifica trovata, oppure un oggetto sentinella se non esiste
	 */
	Notifica getNotificaById(int idNotifica);

	/**
	 * Restituisce tutte le notifiche associate a un utente.
	 *
	 * @param idUtente identificativo dell'utente
	 * @return lista delle notifiche dell'utente, ordinata per data
	 */
	List<Notifica> findByUtente(int idUtente);

	/**
	 * Restituisce tutte le notifiche non ancora lette di un utente.
	 *
	 * @param idUtente identificativo dell'utente
	 * @return lista delle notifiche non lette
	 */
	List<Notifica> findNonLette(int idUtente);

	/**
	 * Restituisce tutte le notifiche associate a una specifica scadenza.
	 *
	 * @param idScadenza identificativo della scadenza; se {@code null} viene
	 *                   restituita una lista vuota
	 * @return lista delle notifiche associate alla scadenza
	 */
	List<Notifica> findByScadenza(Integer idScadenza);

	/**
	 * Restituisce tutte le notifiche presenti nel sistema.
	 * <p>
	 * Metodo tipicamente utilizzato da utenti con ruolo di manager.
	 * </p>
	 *
	 * @return lista completa delle notifiche
	 */
	List<Notifica> findAll();
}
