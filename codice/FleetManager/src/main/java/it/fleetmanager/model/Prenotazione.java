package it.fleetmanager.model;

import java.time.LocalDateTime;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class Prenotazione {
	private int idPrenotazione;
	private LocalDateTime dataInizio;
	private LocalDateTime dataFine;
	private StatoPrenotazione statoPrenotazione;
	private TipoPrenotazione tipoPrenotazione;
	private int idUtente;
	private String targa;

	public Prenotazione(int idPrenotazione, LocalDateTime dataInizio, LocalDateTime dataFine,
			StatoPrenotazione statoPrenotazione, TipoPrenotazione tipoPrenotazione, int idUtente, String targa) {
		this.idPrenotazione = idPrenotazione;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.statoPrenotazione = statoPrenotazione;
		this.tipoPrenotazione = tipoPrenotazione;
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
		return statoPrenotazione;
	}

	public void setStato(StatoPrenotazione statoPrenotazione) {
		this.statoPrenotazione = statoPrenotazione;
	}

	public TipoPrenotazione getTipoPrenotazione() {
		return tipoPrenotazione;
	}

	public int getIdUtente() {
		return idUtente;
	}

	public String getTarga() {
		return targa;
	}
	public void setId(int idPrenotazione) { 
		this.idPrenotazione = idPrenotazione;
	}

	@Override
	public String toString() {
		return "Prenotazione {" + "id=" + idPrenotazione + ", dataInizio=" + dataInizio + ", dataFine=" + dataFine
				+ ", stato=" + statoPrenotazione + ", tipo=" + tipoPrenotazione + ", idUtente=" + idUtente + ", targa='"
				+ targa + '\'' + '}';
	}

}
