package it.fleetmanager;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.StatoPrenotazione;

public class App {
	private static final Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		log.info("FleetManager avviato.");
		log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
		System.out.println("Hello from FleetManager");
		
		

	}
}





/*public Prenotazione(int idPrenotazione, LocalDateTime dataInizio, LocalDateTime dataFine, StatoPrenotazione stato,
		int idUtente, String targa) {
	this.idPrenotazione = idPrenotazione;
	this.dataInizio = dataInizio;
	this.dataFine = dataFine;
	this.stato = stato;
	this.idUtente = idUtente;
	this.targa = targa;*/
