package it.fleetmanager.model;

import java.time.LocalDate;

import it.fleetmanager.util.TipoScadenza;

public class Scadenza {

	private int idScadenza;
	private TipoScadenza tipoScadenza;
	private LocalDate data;
	private boolean notificata;

	public Scadenza(int idScadenza, TipoScadenza tipoScadenza, LocalDate data, boolean notificata) {
		this.idScadenza = idScadenza;
		this.tipoScadenza = tipoScadenza;
		this.data = data;
		this.notificata = notificata;
	}
	
	public int getIdScadenza() {
		return idScadenza;
	}
	
	public TipoScadenza getTipoScadenza() {
		return tipoScadenza;
	}
	
	public LocalDate getData() {
		return data;
	}
	
	public void setData(LocalDate data) {
		this.data = data;
	}
	
	public boolean getNotificata() {
		return notificata;
	}
	
	public void setNotificata(boolean notificata) {
		this.notificata = notificata;
	}
}
