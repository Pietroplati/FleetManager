package it.fleetmanager.repository.dao;

import java.time.LocalDateTime;
import java.util.List;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.util.StatoPrenotazione;

public interface PrenotazioneDAO {

	void save(Prenotazione prenotazione);

	void update(Prenotazione prenotazione);

	void delete(int idPrenotazione);

	Prenotazione getById(int idPrenotazione);

	List<Prenotazione> findByDriver(int idDriver);

	List<Prenotazione> findByVeicolo(String targa);

	List<Prenotazione> findByStato(StatoPrenotazione statoPrenotazione);

	/**
	 * Controlla se esiste una prenotazione che si sovrappone nell'intervallo
	 * dataInizio-dataFine per il veicolo specificato.
	 */
	boolean existsOverlapping(String targa, LocalDateTime dataInizio, LocalDateTime dataFine);
	
	List<Prenotazione> findAll();
}
