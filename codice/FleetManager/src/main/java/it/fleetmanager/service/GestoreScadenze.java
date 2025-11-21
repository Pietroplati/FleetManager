package it.fleetmanager.service;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.ScadenzaDAO;
import it.fleetmanager.repository.VeicoloDAO;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;

public class GestoreScadenze {

	private final ScadenzaDAO scadenzaDAO;
	private final VeicoloDAO veicoloDAO;
	private final SistemaNotifiche sistemaNotifiche;

	public GestoreScadenze(ScadenzaDAO scadenzaDAO, VeicoloDAO veicoloDAO, SistemaNotifiche sistemaNotifiche) {
		this.scadenzaDAO = scadenzaDAO;
		this.veicoloDAO = veicoloDAO;
		this.sistemaNotifiche = sistemaNotifiche;
	}

	/**
	 * Restituisce la prima scadenza non ancora notificata che cade entro la data
	 * indicata. Ritorna SCADENZA_INESISTENTE se non ci sono scadenze.
	 */
	public Scadenza controllaScadenzeEntro(LocalDate dataLimite) {

		if (dataLimite == null) {
			throw new IllegalArgumentException("La data limite non può essere null.");
		}

		List<Scadenza> scadenze = scadenzaDAO.findProssimeScadenze(dataLimite);

		// Nessuna scadenza trovata
		if (scadenze.isEmpty()) {
			return ScadenzaDAOImpl.SCADENZA_INESISTENTE;
		}

		// Restituisco semplicemente la prima scadenza (sono ordinate per data)
		return scadenze.get(0);
	}

	/**
	 * Imposta notificata = true per una scadenza.
	 */
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

	/**
	 * Se esiste una scadenza scaduta (data < oggi) blocca il veicolo.
	 */
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

	/**
	 * Controllo periodico generale: - trova scadenze entro 7 giorni - invia
	 * notifiche - blocca veicoli con scadenze passate
	 */
	public void eseguiControlloPeriodico() {

		LocalDate limite = LocalDate.now().plusDays(7);

		List<Scadenza> scadenze = scadenzaDAO.findProssimeScadenze(limite);

		for (Scadenza s : scadenze) {

			// notifiche SOLO se non è già notificata
			if (!s.getNotificata()) {

				Utente manager = new Utente(1, "Manager", "System", "", "", null);

				sistemaNotifiche.inviaNotificaScadenza(manager, s);

				s.setNotificata(true);
				scadenzaDAO.update(s);
			}

			// blocco veicolo se necessario
			Veicolo v = veicoloDAO.getVeicoloByTarga(s.getTarga());
			if (v != null && v.getStatoVeicolo() != StatoVeicolo.NON_DISPONIBILE) {
				bloccaVeicoloSeScaduta(v);
			}
		}
	}

}
