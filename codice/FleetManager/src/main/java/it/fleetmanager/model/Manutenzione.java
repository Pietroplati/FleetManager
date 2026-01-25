package it.fleetmanager.model;

import java.time.LocalDateTime;
import it.fleetmanager.util.TipoManutenzione;

public class Manutenzione {

	private int idManutenzione;
	private LocalDateTime data;       // data + ora inizio
	private LocalDateTime oraFine;    // null finché non chiusa
	private TipoManutenzione tipoManutenzione;
	private String descrizione;
	private String targa;

	public Manutenzione(
			int idManutenzione,
			LocalDateTime data,
			TipoManutenzione tipoManutenzione,
			String descrizione,
			String targa) {

		this.idManutenzione = idManutenzione;
		this.data = data;
		this.tipoManutenzione = tipoManutenzione;
		this.descrizione = descrizione;
		this.targa = targa;
		this.oraFine = null;
	}

	public int getIdManutenzione() {
		return idManutenzione;
	}

	public LocalDateTime getData() {
		return data;
	}

	public TipoManutenzione getTipoManutenzione() {
		return tipoManutenzione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public String getTarga() {
		return targa;
	}

	public LocalDateTime getOraFine() {
		return oraFine;
	}

	public void setOraFine(LocalDateTime oraFine) {
		this.oraFine = oraFine;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
}
