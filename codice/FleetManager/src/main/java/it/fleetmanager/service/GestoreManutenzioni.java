package it.fleetmanager.service;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.ManutenzioneDAO;
import it.fleetmanager.repository.VeicoloDAO;
import it.fleetmanager.util.TipoManutenzione;
import it.fleetmanager.util.StatoVeicolo;

public class GestoreManutenzioni {

	private ManutenzioneDAO manutenzioneDAO;
	private VeicoloDAO veicoloDAO;

	public GestoreManutenzioni(ManutenzioneDAO manutenzioneDAO, VeicoloDAO veicoloDAO) {
		this.manutenzioneDAO = manutenzioneDAO;
		this.veicoloDAO = veicoloDAO;
	}

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

	public List<Manutenzione> getManutenzioniVeicolo(Veicolo veicolo) {

		if (veicolo == null)
			throw new IllegalArgumentException("Veicolo nullo.");

		return manutenzioneDAO.findByVeicolo(veicolo.getTarga());
	}

}
