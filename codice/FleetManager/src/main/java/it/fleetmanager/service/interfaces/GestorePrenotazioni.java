package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;

public interface GestorePrenotazioni {

	Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

	boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine);

	void confermaPrenotazione(int idPrenotazione, Utente manager);

	void annullaPrenotazione(int idPrenotazione, Utente utente);

	List<Prenotazione> getPrenotazioniDriver(Utente driver);

	List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo);

	void attivaPrenotazione(int idPrenotazione);
	
	void completaPrenotazione(int idPrenotazione);
	
	void aggiornaStatiPrenotazioni();
}
