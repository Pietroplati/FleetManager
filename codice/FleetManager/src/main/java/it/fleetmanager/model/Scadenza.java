package it.fleetmanager.model;

import java.time.LocalDate;
import it.fleetmanager.util.TipoScadenza;

public class Scadenza {

	private int idScadenza;
	private TipoScadenza tipoScadenza;
	private LocalDate data;
	private boolean notificata;
	private String targa;

	public Scadenza(int idScadenza, TipoScadenza tipoScadenza, LocalDate data, boolean notificata, String targa) {
		this.idScadenza = idScadenza;
		this.tipoScadenza = tipoScadenza;
		this.data = data;
		this.notificata = notificata;
		this.targa = targa;
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

	public String getTarga() {
		return targa;
	}

	@Override
	public String toString() {
		return "Scadenza {" + "idScadenza=" + idScadenza + ", tipoScadenza=" + tipoScadenza + ", data=" + data
				+ ", notificata=" + notificata + ", targa='" + targa + '\'' + '}';
	}

}
