package it.fleetmanager.model;

import java.time.LocalDateTime;
import it.fleetmanager.util.StatoPrenotazione;

public class Prenotazione {
	private int idPrenotazione;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private StatoPrenotazione stato;

    public Prenotazione(Integer idPrenotazione, LocalDateTime dataInizio, LocalDateTime dataFine, StatoPrenotazione stato) {
        this.idPrenotazione = idPrenotazione;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.stato = stato;
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

}
