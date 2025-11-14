package it.fleetmanager.repository.impl;

import java.util.Optional;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.UtenteDAO;

public class UtenteDAOImpl implements UtenteDAO {

	@Override
	public Optional<Utente> getUtenteByEmail(String email) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Utente> getById(Integer id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public void save(Utente utente) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Utente utente) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean existsByEmail(String email) {
		// TODO Auto-generated method stub
		return false;
	}

}
