package it.fleetmanager.model;

import it.fleetmanager.util.RuoloUtente;

public class Driver extends Utente {
	private String patente;

	public Driver(Integer idUtente, String nome, String cognome, String email, String password, RuoloUtente ruoloUtente,
			String patente) {
		super(idUtente, nome, cognome, email, password, ruoloUtente);
		this.patente = patente;
	}

	public String getPatente() {
		return patente;
	}

}
