package it.fleetmanager.model;

import it.fleetmanager.util.RuoloUtente;

public class Manager extends Utente {

	public Manager(Integer idUtente, String nome, String cognome, String email, String password,
			RuoloUtente ruoloUtente) {
		super(idUtente, nome, cognome, email, password, ruoloUtente);
	}

}
