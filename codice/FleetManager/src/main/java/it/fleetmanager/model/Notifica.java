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
	private Integer idScadenza;

	public Notifica(Integer idNotifica, TipoNotifica tipoNotifica, String messaggio, LocalDateTime dataInvio,
			boolean letta, int idUtente, Integer idScadenza) {

		this.idNotifica = (idNotifica == null ? -1 : idNotifica);
		this.tipoNotifica = tipoNotifica;
		this.messaggio = messaggio;
		this.dataInvio = dataInvio;
		this.letta = letta;
		this.idUtente = idUtente;
		this.idScadenza = idScadenza;
	}

	public Notifica(Integer idNotifica, TipoNotifica tipoNotifica, String messaggio, boolean letta, int idUtente,
			Integer idScadenza) {

		this(idNotifica, tipoNotifica, messaggio, LocalDateTime.now(), letta, idUtente, idScadenza);
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

	public Integer getIdScadenza() {
		return idScadenza;
	}

	public void setMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}

	@Override
	public String toString() {
		return "Notifica {" + "idNotifica=" + idNotifica + ", tipoNotifica=" + tipoNotifica + ", messaggio='"
				+ messaggio + '\'' + ", dataInvio=" + dataInvio + ", letta=" + letta + ", idUtente=" + idUtente
				+ ", idScadenza=" + (idScadenza != null ? idScadenza : "null") + '}';
	}

}
