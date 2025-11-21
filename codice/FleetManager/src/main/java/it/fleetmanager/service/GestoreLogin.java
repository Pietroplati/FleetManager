package it.fleetmanager.service;

import java.util.List;
import java.util.Objects;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.UtenteDAO;
import it.fleetmanager.repository.impl.UtenteDAOImpl;

public class GestoreLogin {

	private final UtenteDAO utenteDAO;

	public GestoreLogin(UtenteDAO utenteDAO) {
		this.utenteDAO = utenteDAO;
	}

	public Utente login(String email, String password) {

		if (email == null || password == null || email.isBlank() || password.isBlank()) {
			return null;
		}

		Utente utente = utenteDAO.getUtenteByEmail(email);

		if (utente == null || utente == UtenteDAOImpl.UTENTE_INESISTENTE) {
			return null;
		}

		if (!Objects.equals(password, utente.getPassword())) {
			return null;
		}

		return utente;
	}

	public boolean createUtente(Utente nuovoUtente) {

		if (nuovoUtente == null || nuovoUtente.getEmail() == null || nuovoUtente.getEmail().isBlank()) {
			return false;
		}

		if (utenteDAO.existsByEmail(nuovoUtente.getEmail())) {
			return false;
		}

		utenteDAO.save(nuovoUtente);
		return true;
	}

	public void logout(Utente utente) {
	}

	public boolean aggiornaProfilo(Utente utenteAggiornato) {

		if (utenteAggiornato == null || utenteAggiornato.getIdUtente() <= 0) {
			return false;
		}

		Utente existing = utenteDAO.getUtenteById(utenteAggiornato.getIdUtente());
		if (existing == null || existing == UtenteDAOImpl.UTENTE_INESISTENTE) {
			return false;
		}

		utenteDAO.update(utenteAggiornato);
		return true;
	}

	public boolean eliminaUtente(int idUtente) {

		if (idUtente <= 0) {
			return false;
		}

		Utente u = utenteDAO.getUtenteById(idUtente);
		if (u == null || u == UtenteDAOImpl.UTENTE_INESISTENTE) {
			return false;
		}

		utenteDAO.delete(idUtente);
		return true;
	}

	public Utente getUtenteByEmail(String email) {

		if (email == null || email.isBlank()) {
			return null;
		}

		Utente u = utenteDAO.getUtenteByEmail(email);
		if (u == null || u == UtenteDAOImpl.UTENTE_INESISTENTE) {
			return null;
		}

		return u;
	}

	public List<Utente> getTuttiUtenti() {
		return utenteDAO.getTuttiUtenti();
	}
}
