package it.fleetmanager.repository.dao;

import it.fleetmanager.model.Utente;

import java.util.List;


public interface UtenteDAO {
	/**
	 * Inserisce un nuovo utente.
	 */
	void save(Utente utente);

	/**
	 * Aggiorna un utente esistente.
	 */
	void update(Utente utente);

	/**
	 * Cancella l'utente con id indicato.
	 */
	void delete(int id);

	/**
	 * Restituisce l'utente identificato dall'id, se presente.
	 */
	Utente getUtenteById(int id);

	/**
	 * Restituisce l'utente con l'email indicata, se presente.
	 */
	Utente getUtenteByEmail(String email);

	/**
	 * @return true se esiste già un utente con quella email.
	 */
	boolean existsByEmail(String email);
	
	List<Utente> getTuttiUtenti();
	
	Utente getManager();
}
