package it.fleetmanager.service.impl;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.service.interfaces.GestoreManutenzioni;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoManutenzione;

public class GestoreManutenzioniImpl implements GestoreManutenzioni {

	private ManutenzioneDAO manutenzioneDAO;
	private VeicoloDAO veicoloDAO;
	private SistemaNotifiche sistemaNotifiche;

	// ID manager fisso nel sistema
	private static final int ID_MANAGER = 1;

	public GestoreManutenzioniImpl(ManutenzioneDAO manutenzioneDAO, VeicoloDAO veicoloDAO,
			SistemaNotifiche sistemaNotifiche) {
		this.manutenzioneDAO = manutenzioneDAO;
		this.veicoloDAO = veicoloDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	@Override
	public Manutenzione programmareManutenzione(Veicolo veicolo, LocalDate data, TipoManutenzione tipo,
			String descrizione) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

        if (data == null)
			throw new IllegalArgumentException("La data non può essere nulla.");

		if (tipo == null)
			throw new IllegalArgumentException("Tipo manutenzione nullo.");

		if (descrizione == null || descrizione.isBlank())
			throw new IllegalArgumentException("Descrizione non valida.");

		// Cambio stato veicolo
		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		// ID manutenzione incrementale
		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(
				nuovoId,
				data.atStartOfDay(),
				tipo,
				descrizione,
				veicolo.getTarga()
		);

		manutenzioneDAO.save(m);

		// NOTIFICHE ======================================================

		// 1) Notifica al manager
		sistemaNotifiche.notificaManutenzioneProgrammata(
				ID_MANAGER, veicolo.getTarga(), m.getData()
		);

		// 2) Notifica al driver (OPZIONALE, solo se controller la fornisce)
		if (veicolo.getKm() >= 0) { // placeholder, puoi aggiungere idDriver al veicolo
			// sistemaNotifiche.notificaManutenzioneProgrammata(idDriver, veicolo.getTarga(), m.getData());
		}

		return m;
	}

	@Override
	public Manutenzione segnalareInterventoStraordinario(Veicolo veicolo, String descrizione) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(
				nuovoId,
				LocalDate.now().atStartOfDay(),
				TipoManutenzione.STRAORDINARIA,
				descrizione,
				veicolo.getTarga()
		);

		manutenzioneDAO.save(m);

		// NOTIFICHE ======================================================

		// 1) Manager
		sistemaNotifiche.notificaInterventoStraordinario(
				ID_MANAGER, veicolo.getTarga()
		);

		// 2) Driver opzionale (placeholder)
		// sistemaNotifiche.notificaInterventoStraordinario(idDriver, veicolo.getTarga());

		return m;
	}

	@Override
	public void chiudiManutenzione(int idManutenzione) {

		Manutenzione m = manutenzioneDAO.getManutenzioneById(idManutenzione);

		if (m.getIdManutenzione() == -1)
			throw new IllegalArgumentException("Manutenzione inesistente.");

		Veicolo veicolo = veicoloDAO.getVeicoloByTarga(m.getTarga());
		if (veicolo == null)
			throw new IllegalStateException("Veicolo associato non trovato.");

		veicolo.setStatoVeicolo(StatoVeicolo.DISPONIBILE);
		veicoloDAO.update(veicolo);

		m.setDescrizione(m.getDescrizione() + " (CHIUSA)");
		manutenzioneDAO.update(m);

		// NOTIFICHE ======================================================

		// 1) Manager
		sistemaNotifiche.notificaManutenzioneConclusa(
				ID_MANAGER, veicolo.getTarga()
		);

		// 2) Driver (placeholder, se vuoi aggiungere campo idDriver)
		// sistemaNotifiche.notificaManutenzioneConclusa(idDriver, veicolo.getTarga());
	}

	@Override
	public List<Manutenzione> getManutenzioniVeicolo(Veicolo veicolo) {
		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		return manutenzioneDAO.findByVeicolo(veicolo.getTarga());
	}
}
