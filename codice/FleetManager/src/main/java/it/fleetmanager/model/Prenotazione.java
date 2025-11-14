package it.fleetmanager.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.fleetmanager.util.StatoPrenotazione;

public class Prenotazione {
	private int idPrenotazione;
	private LocalDateTime dataInizio;
	private LocalDateTime dataFine;
	private StatoPrenotazione stato;
	private int idUtente;
	private String targa;

	public Prenotazione(@JsonProperty("idPrenotazione") Integer idPrenotazione,
			@JsonProperty("dataInizio") LocalDateTime dataInizio, @JsonProperty("dataFine") LocalDateTime dataFine,
			@JsonProperty("statoPrenotazione") StatoPrenotazione stato, @JsonProperty("idUtente") int idUtente,
			@JsonProperty("targa") String targa) {
		this.idPrenotazione = idPrenotazione;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.stato = stato;
		this.idUtente = idUtente;
		this.targa = targa;
	}

	public int getIdPrenotazione() {
		return idPrenotazione;
	}

	public LocalDateTime getDataInizio() {
		return dataInizio;
	}

	public void setDataInizio(LocalDateTime dataInizio) {
		this.dataInizio = dataInizio;
	}

	public LocalDateTime getDataFine() {
		return dataFine;
	}

	public void setDataFine(LocalDateTime dataFine) {
		this.dataFine = dataFine;
	}

	public StatoPrenotazione getStato() {
		return stato;
	}

	public void setStato(StatoPrenotazione stato) {
		this.stato = stato;
	}

	public int getIdUtente() {
		return idUtente;
	}
	
	public String getTarga() {
		return targa;
	}
}
