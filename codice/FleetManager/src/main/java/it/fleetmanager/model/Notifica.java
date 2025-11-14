package it.fleetmanager.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.fleetmanager.util.TipoNotifica;

public class Notifica {
	private int idNotifica;
	private TipoNotifica tipoNotifica;
	private String messaggio;
	private LocalDateTime dataInvio;
	private boolean letta;
	private int idUtente;
	private int idScadenza;

	public Notifica(@JsonProperty("idNotifica") int idNotifica, @JsonProperty("tipoNotifica") TipoNotifica tipoNotifica,
			@JsonProperty("messaggio") String messaggio, @JsonProperty("dataInvio") LocalDateTime dataInvio,
			@JsonProperty("letta") boolean letta, @JsonProperty("idUtente") int idUtente,
			@JsonProperty("idScadenza") int idScadenza) {
		this.idNotifica = idNotifica;
		this.tipoNotifica = tipoNotifica;
		this.messaggio = messaggio;
		this.dataInvio = dataInvio;
		this.letta = letta;
		
	}

	public int getIdNotifica() {
		return idNotifica;
	}

	public TipoNotifica getTipoNotifica() {
		return tipoNotifica;
	}

	public String getMessaggio() {
		return messaggio;
	}

	public LocalDateTime getDataInvio() {
		return dataInvio;
	}

	public boolean getLetta() {
		return letta;
	}

	public void setLetta(boolean letta) {
		this.letta = letta;
	}
	
	public int getIdUtente() {
		return idUtente;
	}
	
	public int getIdScadenza() {
		return idScadenza;
	}
}
