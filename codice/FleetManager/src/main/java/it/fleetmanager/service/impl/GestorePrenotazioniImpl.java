package it.fleetmanager.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class GestorePrenotazioniImpl implements GestorePrenotazioni {

	private PrenotazioneDAO prenotazioneDAO;
	private SistemaNotifiche sistemaNotifiche;

	public GestorePrenotazioniImpl(PrenotazioneDAO prenotazioneDAO, VeicoloDAO veicoloDAO,
			SistemaNotifiche sistemaNotifiche) {
		this.prenotazioneDAO = prenotazioneDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	@Override
	public Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio,
			LocalDateTime dataFine) {

		if (driver.getPatente() == null) {
			throw new IllegalArgumentException("L’utente non ha la patente, non può prenotare veicoli.");
		}

		boolean disponibile = validadisponibilita(veicolo, dataInizio, dataFine);

		if (!disponibile) {
			throw new IllegalArgumentException(
					"Il veicolo " + veicolo.getTarga() + " non è disponibile nelle date richieste.");
		}

		Prenotazione p = new Prenotazione(0, dataInizio, dataFine, StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE,
				driver.getIdUtente(), veicolo.getTarga());

		prenotazioneDAO.save(p);

		sistemaNotifiche.inviaNotificaPrenotazione(driver, p);

		return p;
	}

	@Override
	public boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine) {

		List<Prenotazione> prenotazioni = prenotazioneDAO.findByVeicolo(veicolo.getTarga());

		for (Prenotazione p : prenotazioni) {
			boolean overlap = dataInizio.isBefore(p.getDataFine()) && dataFine.isAfter(p.getDataInizio());

			if (overlap && p.getStato() != StatoPrenotazione.ANNULLATA) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void confermaPrenotazione(int idPrenotazione, Utente manager) {

		if (manager.getRuoloUtente() != RuoloUtente.MANAGER) {
			throw new IllegalArgumentException("Solo un manager può confermare una prenotazione.");
		}

		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1) {
			throw new IllegalArgumentException("Prenotazione non trovata");
		}

		p.setStato(StatoPrenotazione.CONFERMATA);

		prenotazioneDAO.update(p);

		sistemaNotifiche.inviaNotificaPrenotazione(new Utente(p.getIdUtente(), "", "", "", "", RuoloUtente.DRIVER), p);
	}

	@Override
	public void annullaPrenotazione(int idPrenotazione, Utente utente) {

		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1) {
			throw new IllegalArgumentException("Prenotazione non trovata");
		}

		p.setStato(StatoPrenotazione.ANNULLATA);

		prenotazioneDAO.update(p);

		sistemaNotifiche.inviaNotificaPrenotazione(new Utente(p.getIdUtente(), "", "", "", "", RuoloUtente.DRIVER), p);
	}

	@Override
	public List<Prenotazione> getPrenotazioniDriver(Utente driver) {
		if (driver.getPatente() == null) {
			throw new IllegalArgumentException("Questo utente non è un driver.");
		}

		return prenotazioneDAO.findByDriver(driver.getIdUtente());
	}

	@Override
	public List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo) {
		return prenotazioneDAO.findByVeicolo(veicolo.getTarga());
	}
}
