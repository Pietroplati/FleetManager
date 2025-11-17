package it.fleetmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.fleetmanager.util.RuoloUtente;

public class Utente {
	private int idUtente;
	private String nome;
	private String cognome;
	private String email;
	private String password;
	private RuoloUtente ruoloUtente;
	private String patente;

	public Utente(@JsonProperty("idUtente") Integer idUtente, @JsonProperty("nome") String nome,
			@JsonProperty("cognome") String cognome, @JsonProperty("email") String email,
			@JsonProperty("password") String password, @JsonProperty("ruoloUtente") RuoloUtente ruoloUtente) {
		this.idUtente = idUtente;
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
		this.ruoloUtente = ruoloUtente;
	}

	public Utente(@JsonProperty("idUtente") Integer idUtente, @JsonProperty("nome") String nome,
			@JsonProperty("cognome") String cognome, @JsonProperty("email") String email,
			@JsonProperty("password") String password, @JsonProperty("ruoloUtente") RuoloUtente ruoloUtente,
			@JsonProperty("patente") String patente) {
		this.idUtente = idUtente;
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
		this.ruoloUtente = ruoloUtente;
		this.patente = patente;
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

	public String getPatente() {
		return patente;
	}
	@Override
	public String toString() {
	    return "Utente {" +
	           "id=" + idUtente +
	           ", nome='" + nome + '\'' +
	           ", cognome='" + cognome + '\'' +
	           ", email='" + email + '\'' +
	           ", ruolo=" + ruoloUtente +
	           ", patente='" + patente + '\'' +
	           '}';
	}

}
