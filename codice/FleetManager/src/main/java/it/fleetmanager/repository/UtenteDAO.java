package it.fleetmanager.repository;

import it.fleetmanager.model.Utente;

import java.util.Optional;

public interface UtenteDAO {
	/**
	 * Inserisce un nuovo utente.
	 */
	public void save(Utente utente);

	/**
	 * Aggiorna un utente esistente.
	 */
	public void update(Utente utente);

	/**
	 * Cancella l'utente con id indicato.
	 */
	public void delete(int id);

	/**
	 * Restituisce l'utente identificato dall'id, se presente.
	 */
	public Utente getUtenteById(int id);

	/**
	 * Restituisce l'utente con l'email indicata, se presente.
	 */
	public Utente getUtenteByEmail(String email);

	/**
	 * @return true se esiste già un utente con quella email.
	 */
	public boolean existsByEmail(String email);
}
