package it.fleetmanager.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class GestorePrenotazioniImpl implements GestorePrenotazioni {

	private final PrenotazioneDAO prenotazioneDAO;
	private final VeicoloDAO veicoloDAO;
	private final UtenteDAO utenteDAO;
	private final SistemaNotifiche sistemaNotifiche;

	public GestorePrenotazioniImpl(PrenotazioneDAO prenotazioneDAO, VeicoloDAO veicoloDAO, UtenteDAO utenteDAO,
			SistemaNotifiche sistemaNotifiche) {
		this.prenotazioneDAO = prenotazioneDAO;
		this.veicoloDAO = veicoloDAO;
		this.utenteDAO = utenteDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	@Override
	public Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio,
			LocalDateTime dataFine) {

		if (driver.getPatente() == null)
			throw new IllegalArgumentException("L’utente non ha la patente, non può prenotare veicoli.");

		if (!validadisponibilita(veicolo, dataInizio, dataFine))
			throw new IllegalArgumentException(
					"Il veicolo " + veicolo.getTarga() + " non è disponibile nelle date richieste.");

		Prenotazione p = new Prenotazione(0, dataInizio, dataFine, StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE,
				driver.getIdUtente(), veicolo.getTarga());

		prenotazioneDAO.save(p);

		// 🔥 Notifica al manager: nuova richiesta
		sistemaNotifiche.notificaRichiestaPrenotazione(driver, p);

		return p;
	}

	@Override
	public boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine) {

		List<Prenotazione> prenotazioni = prenotazioneDAO.findByVeicolo(veicolo.getTarga());

		for (Prenotazione p : prenotazioni) {
			boolean overlap = dataInizio.isBefore(p.getDataFine()) && dataFine.isAfter(p.getDataInizio());

			if (overlap && p.getStato() != StatoPrenotazione.ANNULLATA)
				return false;
		}
		return true;
	}

	@Override
	public void confermaPrenotazione(int idPrenotazione, Utente manager) {

		if (manager.getRuoloUtente() != RuoloUtente.MANAGER)
			throw new IllegalArgumentException("Solo un manager può confermare una prenotazione.");

		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1)
			throw new IllegalArgumentException("Prenotazione non trovata");

		p.setStato(StatoPrenotazione.CONFERMATA);
		prenotazioneDAO.update(p);

		// 🔥 Recupero il driver vero e lo notifico
		Utente driver = utenteDAO.getUtenteById(p.getIdUtente());
		sistemaNotifiche.notificaConfermaPrenotazione(driver, p);
	}

	@Override
	public void annullaPrenotazione(int idPrenotazione, Utente utente) {

		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1)
			throw new IllegalArgumentException("Prenotazione non trovata");

		p.setStato(StatoPrenotazione.ANNULLATA);
		prenotazioneDAO.update(p);

		Utente driver = utenteDAO.getUtenteById(p.getIdUtente());

		if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {

			// 🔥 Manager rifiuta LA RICHIESTA
			sistemaNotifiche.notificaRifiutoPrenotazione(driver, p);

		} else if (utente.getRuoloUtente() == RuoloUtente.DRIVER) {

			// 🔥 Driver annulla la prenotazione
			sistemaNotifiche.notificaAnnullamentoPrenotazioneDaDriver(driver, p);
		}
	}

	@Override
	public void attivaPrenotazione(int idPrenotazione) {
		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1)
			throw new IllegalArgumentException("Prenotazione non trovata");

		if (LocalDateTime.now().isBefore(p.getDataInizio()))
			throw new IllegalStateException("Non è possibile attivare la prenotazione prima della data di inizio.");

		p.setStato(StatoPrenotazione.ATTIVA);
		prenotazioneDAO.update(p);
	}

	@Override
	public void completaPrenotazione(int idPrenotazione) {

		Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

		if (p == null || p.getIdPrenotazione() == -1)
			throw new IllegalArgumentException("Prenotazione non trovata");

		if (LocalDateTime.now().isBefore(p.getDataFine()))
			throw new IllegalStateException("Non è possibile completare la prenotazione prima della data di fine.");

		p.setStato(StatoPrenotazione.COMPLETATA);
		prenotazioneDAO.update(p);
	}

	@Override
	public List<Prenotazione> getPrenotazioniDriver(Utente driver) {
		if (driver.getPatente() == null)
			throw new IllegalArgumentException("Questo utente non è un driver.");

		return prenotazioneDAO.findByDriver(driver.getIdUtente());
	}

	@Override
	public List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo) {
		return prenotazioneDAO.findByVeicolo(veicolo.getTarga());
	}

	@Override
	public void aggiornaStatiPrenotazioni() {

		LocalDateTime now = LocalDateTime.now();

		// CONFERMATE → ATTIVE
		for (Prenotazione p : prenotazioneDAO.findByStato(StatoPrenotazione.CONFERMATA)) {
			if (!now.isBefore(p.getDataInizio()) && now.isBefore(p.getDataFine())) {
				p.setStato(StatoPrenotazione.ATTIVA);
				prenotazioneDAO.update(p);
			}
		}

		// ATTIVE → COMPLETATE
		for (Prenotazione p : prenotazioneDAO.findByStato(StatoPrenotazione.ATTIVA)) {
			if (!now.isBefore(p.getDataFine())) {
				p.setStato(StatoPrenotazione.COMPLETATA);
				prenotazioneDAO.update(p);
			}
		}
	}
}
