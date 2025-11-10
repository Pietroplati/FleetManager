package it.fleetmanager.model;

import java.time.LocalDateTime;

import it.fleetmanager.util.TipoManutenzione;

public class Manutenzione {
	private int idManutenzione;
	private LocalDateTime data;
	private TipoManutenzione tipoManutenzione;
	private String descrizione;

	public Manutenzione(int idManutenzione, LocalDateTime data, TipoManutenzione tipoManutenzione, String descrizione) {
		this.idManutenzione = idManutenzione;
		this.data = data;
		this.tipoManutenzione = tipoManutenzione;
		this.descrizione = descrizione;
	}
	
	public int getIdManutenzione() {
		return idManutenzione;
	}
	
	public LocalDateTime getData() {
		return data;
	}
	
	public void setData(LocalDateTime data) {
		this.data = data;
	}
	
	public TipoManutenzione getTipoManutenzione() {
		return tipoManutenzione;
	}
	
	public String getDescrizione() {
		return descrizione;
	}
	
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
}
