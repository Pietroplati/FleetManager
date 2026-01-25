package it.fleetmanager.repository.dao;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Veicolo;

/**
 * Data Access Object (DAO) per la gestione dei {@link Veicolo}.
 * <p>
 * Questa interfaccia definisce il contratto per le operazioni di persistenza e
 * interrogazione dei veicoli presenti nel sistema.
 * </p>
 */
public interface VeicoloDAO {

	/**
	 * Salva un nuovo veicolo nel sistema.
	 *
	 * @param veicolo il veicolo da salvare
	 */
	void save(Veicolo veicolo);

	/**
	 * Aggiorna un veicolo esistente.
	 *
	 * @param veicolo il veicolo con i dati aggiornati
	 */
	void update(Veicolo veicolo);

	/**
	 * Elimina un veicolo identificato dalla sua targa.
	 *
	 * @param targa targa del veicolo
	 */
	void delete(String targa);

	/**
	 * Restituisce un veicolo a partire dalla sua targa.
	 *
	 * @param targa targa del veicolo
	 * @return il veicolo trovato, oppure un oggetto sentinella se non esiste
	 */
	Veicolo getVeicoloByTarga(String targa);

	/**
	 * Restituisce tutti i veicoli presenti nel sistema.
	 *
	 * @return lista completa dei veicoli
	 */
	List<Veicolo> getTuttiVeicoli();

	/**
	 * Restituisce i veicoli disponibili in un determinato intervallo temporale.
	 * <p>
	 * Un veicolo è considerato disponibile se non esistono prenotazioni che si
	 * sovrappongono all'intervallo indicato.
	 * </p>
	 *
	 * @param dataInizio data e ora di inizio dell'intervallo
	 * @param dataFine   data e ora di fine dell'intervallo
	 * @return lista dei veicoli disponibili nell'intervallo specificato
	 */
	List<Veicolo> getDisponibili(LocalDateTime dataInizio, LocalDateTime dataFine);
}
