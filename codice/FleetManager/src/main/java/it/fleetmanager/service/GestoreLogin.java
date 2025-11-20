package it.fleetmanager.service;

import java.util.List;
import java.util.Objects;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.UtenteDAO;

public class GestoreLogin {

	private final UtenteDAO utenteDAO;

	public GestoreLogin(UtenteDAO utenteDAO) {
		this.utenteDAO = utenteDAO;
	}

	/**
	 * Effettua il login di un utente dato email e password.
	 * 
	 * @return Utente se login corretto, altrimenti null.
	 */
	public Utente login(String email, String password) {

		if (email == null || password == null || email.isBlank() || password.isBlank()) {
			return null;
		}

		Utente utente = utenteDAO.getUtenteByEmail(email);

		if (utente == null) {
			return null; // utente inesistente
		}

		if (!Objects.equals(password, utente.getPassword())) {
			return null; // password errata
		}

		return utente; // login riuscito
	}

	/**
	 * Crea un nuovo utente se l'email non è già registrata.
	 * 
	 * @return true se la creazione è avvenuta correttamente.
	 */
	public boolean createUtente(Utente nuovoUtente) {

		if (nuovoUtente == null || nuovoUtente.getEmail() == null || nuovoUtente.getEmail().isBlank()) {
			return false;
		}

		if (utenteDAO.existsByEmail(nuovoUtente.getEmail())) {
			return false; // email già presente
		}

		utenteDAO.save(nuovoUtente);
		return true;
	}

	/**
	 * Esegue un logout logico. Non deve comunicare con il DB: la UI gestisce lo
	 * stato utente loggato.
	 */
	public void logout(Utente utente) {
		// nessuna logica di DB
		// sarà la UI a invalidare la sessione dell'utente
	}

	/**
	 * Aggiorna il profilo di un utente.
	 * 
	 * @return true se aggiornato correttamente.
	 */
	public boolean aggiornaProfilo(Utente utenteAggiornato) {

		if (utenteAggiornato == null || utenteAggiornato.getIdUtente() <= 0) {
			return false;
		}

		utenteDAO.update(utenteAggiornato);
		return true;
	}

	/**
	 * Cancella un utente dato il suo id.
	 */
	public boolean eliminaUtente(int idUtente) {

		if (idUtente <= 0) {
			return false;
		}

		Utente u = utenteDAO.getUtenteById(idUtente);
		if (u == null || u.getIdUtente() == -1) {
			return false;
		}

		utenteDAO.delete(idUtente);
		return true;
	}

	/**
	 * Restituisce l'utente con quella email oppure null.
	 */
	public Utente getUtenteByEmail(String email) {

		if (email == null || email.isBlank()) {
			return null;
		}

		Utente u = utenteDAO.getUtenteByEmail(email);

		// FIX: interpretare UTENTE_INESISTENTE
		if (u == null || u.getIdUtente() == -1) {
			return null;
		}

		return u;
	}

	/**
	 * Restituisce tutti gli utenti presenti nel sistema.
	 */
	public List<Utente> getTuttiUtenti() {
		return utenteDAO.getTuttiUtenti();
	}
}
