package it.fleetmanager.model;

import java.time.LocalDateTime;
import it.fleetmanager.util.TipoNotifica;

public class Notifica {
	private int idNotifica;
	private TipoNotifica tipoNotifica;
	private String messaggio;
	private LocalDateTime dataInvio;
	private boolean letta;
	private int idUtente;
	private int idScadenza;

	public Notifica(int idNotifica, TipoNotifica tipoNotifica, String messaggio, LocalDateTime dataInvio, boolean letta,
			int idUtente, int idScadenza) {
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
