package it.fleetmanager.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;

public class SistemaNotifiche {

	private final NotificaDAO notificaDAO;
	private final UtenteDAO utenteDAO;

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public SistemaNotifiche(NotificaDAO notificaDAO, UtenteDAO utenteDAO) {
		this.notificaDAO = notificaDAO;
		this.utenteDAO = utenteDAO;
	}

	// ------------------------------------------------------------
	// Utility
	// ------------------------------------------------------------
	private void salva(Notifica n) {
		notificaDAO.save(n);
	}

	private Utente getManager() {
		return utenteDAO.getManager();
	}

	// ------------------------------------------------------------
	// SCADENZE
	// ------------------------------------------------------------
	public void inviaNotificaScadenza(Scadenza s) {

		Utente manager = getManager();

		String msg = """
				📌 Promemoria Scadenza
				Veicolo: %s
				Scadenza: %s
				Data: %s
				""".formatted(s.getTarga(), s.getTipoScadenza(), s.getData());

		salva(new Notifica(null, TipoNotifica.SCADENZA, msg, LocalDateTime.now(), false, manager.getIdUtente(),
				s.getIdScadenza()));
	}

	// ------------------------------------------------------------
	// PRENOTAZIONI
	// ------------------------------------------------------------

	// 1️⃣ Richiesta prenotazione → Manager
	public void notificaRichiestaPrenotazione(Utente driver, Prenotazione p) {

		Utente manager = getManager();

		String msg = """
				📝 Richiesta Prenotazione
				Da: %s %s
				Veicolo: %s
				Periodo: %s → %s
				""".formatted(driver.getNome(), driver.getCognome(), p.getTarga(), p.getDataInizio().format(fmt),
				p.getDataFine().format(fmt));

		salva(new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false, manager.getIdUtente(),
				null));
	}

	// 2️⃣ Conferma → Driver
	public void notificaConfermaPrenotazione(Utente driver, Prenotazione p) {

		String msg = """
				✔ Prenotazione Approvata
				Veicolo: %s
				Periodo: %s → %s
				""".formatted(p.getTarga(), p.getDataInizio().format(fmt), p.getDataFine().format(fmt));

		salva(new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false, driver.getIdUtente(),
				null));
	}

	// 3️⃣ Rifiuto → Driver
	public void notificaRifiutoPrenotazione(Utente driver, Prenotazione p) {

		String msg = """
				❌ Prenotazione Rifiutata
				Veicolo: %s
				Richiesta: %s → %s
				""".formatted(p.getTarga(), p.getDataInizio().format(fmt), p.getDataFine().format(fmt));

		salva(new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false, driver.getIdUtente(),
				null));
	}

	// 4️⃣ Driver annulla prenotazione → Manager
	public void notificaAnnullamentoPrenotazioneDaDriver(Utente driver, Prenotazione p) {

		Utente manager = getManager();

		String msg = """
				🔄 Prenotazione Annullata
				Driver: %s %s
				Veicolo: %s
				""".formatted(driver.getNome(), driver.getCognome(), p.getTarga());

		salva(new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false, manager.getIdUtente(),
				null));
	}

	// ------------------------------------------------------------
	// MANUTENZIONI (stile semplice per ora)
	// ------------------------------------------------------------
	public void notificaManutenzioneProgrammata(int idUtente, String targa, LocalDateTime data) {

		String msg = """
				🛠 Manutenzione Programmata
				Veicolo: %s
				Data: %s
				""".formatted(targa, data.toLocalDate());

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}

	public void notificaManutenzioneConclusa(int idUtente, String targa) {

		String msg = """
				🛠 Manutenzione Completata
				Veicolo: %s
				""".formatted(targa);

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}

	public void notificaInterventoStraordinario(int idUtente, String targa) {

		String msg = """
				⚠ Intervento Straordinario
				Veicolo: %s
				""".formatted(targa);

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}
}
