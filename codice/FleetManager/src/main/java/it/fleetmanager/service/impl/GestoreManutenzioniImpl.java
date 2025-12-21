package it.fleetmanager.service.impl;

import java.time.LocalDateTime;
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

	private static final int ID_MANAGER = 1;

	public GestoreManutenzioniImpl(
			ManutenzioneDAO manutenzioneDAO,
			VeicoloDAO veicoloDAO,
			SistemaNotifiche sistemaNotifiche) {

		this.manutenzioneDAO = manutenzioneDAO;
		this.veicoloDAO = veicoloDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	// ============================================================
	// PROGRAMMAZIONE MANUTENZIONE
	// ============================================================

	@Override
	public Manutenzione programmareManutenzione(
			Veicolo veicolo,
			LocalDateTime dataInizio,
			TipoManutenzione tipo,
			String descrizione) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");
		if (dataInizio == null)
			throw new IllegalArgumentException("Data/ora non valida.");
		if (tipo == null)
			throw new IllegalArgumentException("Tipo manutenzione nullo.");
		if (descrizione == null || descrizione.isBlank())
			throw new IllegalArgumentException("Descrizione non valida.");

		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(
				nuovoId,
				dataInizio,
				tipo,
				descrizione,
				veicolo.getTarga()
		);

		manutenzioneDAO.save(m);

		sistemaNotifiche.notificaManutenzioneProgrammata(
				ID_MANAGER,
				veicolo.getTarga(),
				m.getData()
		);

		return m;
	}

	// ============================================================
	// INTERVENTO STRAORDINARIO
	// ============================================================

	@Override
	public Manutenzione segnalareInterventoStraordinario(
			Veicolo veicolo,
			String descrizione) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");
		if (descrizione == null || descrizione.isBlank())
			throw new IllegalArgumentException("Descrizione non valida.");

		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(
				nuovoId,
				LocalDateTime.now(),
				TipoManutenzione.STRAORDINARIA,
				descrizione,
				veicolo.getTarga()
		);

		manutenzioneDAO.save(m);

		sistemaNotifiche.notificaInterventoStraordinario(
				ID_MANAGER,
				veicolo.getTarga()
		);

		return m;
	}

	// ============================================================
	// CHIUSURA MANUTENZIONE
	// ============================================================

	@Override
	public void chiudiManutenzione(int idManutenzione) {

		Manutenzione m = manutenzioneDAO.getManutenzioneById(idManutenzione);

		if (m.getIdManutenzione() == -1)
			throw new IllegalArgumentException("Manutenzione inesistente.");

		Veicolo veicolo = veicoloDAO.getVeicoloByTarga(m.getTarga());

		veicolo.setStatoVeicolo(StatoVeicolo.DISPONIBILE);
		veicoloDAO.update(veicolo);

		m.setDescrizione(m.getDescrizione() + " (CHIUSA)");
		m.setOraFine(LocalDateTime.now()); // ORA FINE AUTOMATICA

		manutenzioneDAO.update(m);

		sistemaNotifiche.notificaManutenzioneConclusa(
				ID_MANAGER,
				veicolo.getTarga()
		);
	}

	// ============================================================
	// QUERY
	// ============================================================

	@Override
	public List<Manutenzione> getManutenzioniVeicolo(Veicolo veicolo) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		return manutenzioneDAO.findByVeicolo(veicolo.getTarga());
	}
}
