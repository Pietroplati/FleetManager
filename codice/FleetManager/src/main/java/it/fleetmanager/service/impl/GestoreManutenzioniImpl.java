package it.fleetmanager.service.impl;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.service.interfaces.GestoreManutenzioni;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoManutenzione;

public class GestoreManutenzioniImpl implements GestoreManutenzioni {

	private ManutenzioneDAO manutenzioneDAO;
	private VeicoloDAO veicoloDAO;

	public GestoreManutenzioniImpl(ManutenzioneDAO manutenzioneDAO, VeicoloDAO veicoloDAO) {
		this.manutenzioneDAO = manutenzioneDAO;
		this.veicoloDAO = veicoloDAO;
	}

	@Override
	public Manutenzione programmareManutenzione(Veicolo veicolo, LocalDate data, TipoManutenzione tipo,
			String descrizione) {

		if (veicolo == null) {
			throw new IllegalArgumentException("Veicolo nullo.");
		}

		if (data == null) {
			throw new IllegalArgumentException("La data non può essere nulla.");
		}

		if (tipo == null) {
			throw new IllegalArgumentException("Tipo manutenzione nullo.");
		}

		if (descrizione == null || descrizione.isBlank()) {
			throw new IllegalArgumentException("Descrizione non valida.");
		}

		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(nuovoId, data.atStartOfDay(), tipo, descrizione, veicolo.getTarga());

		manutenzioneDAO.save(m);

		return m;
	}

	@Override
	public Manutenzione segnalareInterventoStraordinario(Veicolo veicolo, String descrizione) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		veicolo.setStatoVeicolo(StatoVeicolo.IN_MANUTENZIONE);
		veicoloDAO.update(veicolo);

		int nuovoId = manutenzioneDAO.getMaxId() + 1;

		Manutenzione m = new Manutenzione(nuovoId, LocalDate.now().atStartOfDay(), TipoManutenzione.STRAORDINARIA,
				descrizione, veicolo.getTarga());

		manutenzioneDAO.save(m);

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
	}

	@Override
	public List<Manutenzione> getManutenzioniVeicolo(Veicolo veicolo) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		return manutenzioneDAO.findByVeicolo(veicolo.getTarga());
	}

}
