package it.fleetmanager.service.interfaces;

import java.util.List;

import it.fleetmanager.model.Utente;

public interface GestoreLogin {

	Utente login(String email, String password);

	boolean createUtente(Utente nuovoUtente);

	boolean aggiornaProfilo(Utente utenteAggiornato);

	boolean eliminaUtente(int idUtente);

	Utente getUtenteByEmail(String email);

	List<Utente> getTuttiUtenti();

}
