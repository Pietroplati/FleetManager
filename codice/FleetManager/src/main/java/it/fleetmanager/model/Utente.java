package it.fleetmanager.model;

import it.fleetmanager.util.RuoloUtente;

public abstract class Utente {
	private int idUtente;
	private String nome;
	private String cognome;
	private String email;
	private String password;
	private RuoloUtente ruoloUtente;

	public Utente(Integer idUtente, String nome, String cognome, String email, String password,
			RuoloUtente ruoloUtente) {
		this.idUtente = idUtente;
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
		this.ruoloUtente = ruoloUtente;
	}

	public int getIdUtente() {
		return idUtente;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getCognome() {
		return cognome;
	}
	
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public RuoloUtente getRuoloUtente() {
		return ruoloUtente;
	}
}
