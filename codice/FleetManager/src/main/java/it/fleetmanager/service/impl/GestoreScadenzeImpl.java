package it.fleetmanager.service.impl;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.service.interfaces.GestoreScadenze;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;

public class GestoreScadenzeImpl implements GestoreScadenze {

	private final ScadenzaDAO scadenzaDAO;
	private final VeicoloDAO veicoloDAO;
	private final SistemaNotifiche sistemaNotifiche;

	public GestoreScadenzeImpl(ScadenzaDAO scadenzaDAO, VeicoloDAO veicoloDAO, SistemaNotifiche sistemaNotifiche) {
		this.scadenzaDAO = scadenzaDAO;
		this.veicoloDAO = veicoloDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	@Override
	public Scadenza controllaScadenzeEntro(LocalDate dataLimite) {

		if (dataLimite == null) {
			throw new IllegalArgumentException("La data limite non può essere null.");
		}

		List<Scadenza> scadenze = scadenzaDAO.findProssimeScadenze(dataLimite);

		if (scadenze.isEmpty()) {
			return ScadenzaDAOImpl.SCADENZA_INESISTENTE;
		}

		return scadenze.get(0);
	}

	@Override
	public void marcaComeNotificata(Integer idScadenza) {

		if (idScadenza == null || idScadenza <= 0) {
			throw new IllegalArgumentException("ID scadenza non valido");
		}

		Scadenza s = scadenzaDAO.getScadenzaById(idScadenza);

		if (s.getIdScadenza() == -1) {
			throw new IllegalArgumentException("Scadenza inesistente");
		}

		s.setNotificata(true);
		scadenzaDAO.update(s);
	}

	@Override
	public void bloccaVeicoloSeScaduta(Veicolo veicolo) {

		if (veicolo == null) {
			throw new IllegalArgumentException("Veicolo null");
		}

		List<Scadenza> scadenze = scadenzaDAO.findByVeicolo(veicolo.getTarga());

		for (Scadenza s : scadenze) {

			if (s.getData().isBefore(LocalDate.now())) {

				veicolo.setStatoVeicolo(StatoVeicolo.NON_DISPONIBILE);
				veicoloDAO.update(veicolo);

				// NOTIFICA AL MANAGER (Utente #1)
				Utente manager = new Utente(1, "Manager", "System", "", "", null); // ruolo non serve qui

				sistemaNotifiche.inviaNotifica(manager,
						"Attenzione: Veicolo " + veicolo.getTarga() + " bloccato per scadenza " + s.getTipoScadenza());

				return;
			}
		}
	}

	@Override
	public void eseguiControlloPeriodico() {

		LocalDate limite = LocalDate.now().plusDays(7);

		List<Scadenza> scadenze = scadenzaDAO.findProssimeScadenze(limite);

		for (Scadenza s : scadenze) {

			if (!s.getNotificata()) {

				Utente manager = new Utente(1, "Manager", "System", "", "", null);

				sistemaNotifiche.inviaNotificaScadenza(manager, s);

				s.setNotificata(true);
				scadenzaDAO.update(s);
			}

			Veicolo v = veicoloDAO.getVeicoloByTarga(s.getTarga());
			if (v != null && v.getStatoVeicolo() != StatoVeicolo.NON_DISPONIBILE) {
				bloccaVeicoloSeScaduta(v);
			}
		}
	}

}
