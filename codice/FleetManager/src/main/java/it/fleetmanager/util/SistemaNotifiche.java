package it.fleetmanager.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;

/**
 * Sistema centralizzato per la gestione e l’invio delle notifiche. Recupera
 * autonomamente il manager tramite UtenteDAO.
 */
public class SistemaNotifiche {

	private final NotificaDAO notificaDAO;
	private final UtenteDAO utenteDAO;

	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public SistemaNotifiche(NotificaDAO notificaDAO, UtenteDAO utenteDAO) {
		this.notificaDAO = notificaDAO;
		this.utenteDAO = utenteDAO;
	}

	// ============================================================
	// UTILITY
	// ============================================================

	private void salva(Notifica n) {
		notificaDAO.save(n);
	}

	private Utente getManager() {
		return utenteDAO.getManager();
	}

	// ============================================================
	// SCADENZE
	// ============================================================

	public void inviaNotificaScadenza(Scadenza scadenza) {

		Utente manager = getManager();

		String msg = "Scadenza " + scadenza.getTipoScadenza() + " del veicolo " + scadenza.getTarga() + ": "
				+ scadenza.getData();

		Notifica n = new Notifica(null, TipoNotifica.SCADENZA, msg, LocalDateTime.now(), false, manager.getIdUtente(),
				scadenza.getIdScadenza());

		salva(n);
	}

	// ============================================================
	// PRENOTAZIONI
	// ============================================================

	/**
	 * 1️⃣ DRIVER richiede prenotazione → notifica al MANAGER
	 */
	public void notificaRichiestaPrenotazione(Utente driver, Prenotazione p) {

		Utente manager = getManager();

		String msg = "Nuova richiesta di prenotazione per il veicolo " + p.getTarga() + " da parte di "
				+ driver.getNome() + " " + driver.getCognome() + ". Periodo: " + p.getDataInizio().format(fmt) + " → "
				+ p.getDataFine().format(fmt) + ".";

		Notifica n = new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false,
				manager.getIdUtente(), null);

		salva(n);
	}

	/**
	 * 2️⃣ MANAGER conferma prenotazione → notifica al DRIVER
	 */
	public void notificaConfermaPrenotazione(Utente driver, Prenotazione p) {

		String msg = "La tua prenotazione per il veicolo " + p.getTarga() + " è stata approvata. Periodo: "
				+ p.getDataInizio().format(fmt) + " → " + p.getDataFine().format(fmt) + ".";

		Notifica n = new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false,
				driver.getIdUtente(), null);

		salva(n);
	}

	/**
	 * 3️⃣ MANAGER rifiuta prenotazione → notifica al DRIVER
	 */
	public void notificaRifiutoPrenotazione(Utente driver, Prenotazione p) {

		String msg = "La tua richiesta di prenotazione per il veicolo " + p.getTarga()
				+ " è stata rifiutata dal manager.";

		Notifica n = new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false,
				driver.getIdUtente(), null);

		salva(n);
	}

	/**
	 * 4️⃣ DRIVER annulla la sua prenotazione → notifica al MANAGER
	 */
	public void notificaAnnullamentoPrenotazioneDaDriver(Utente driver, Prenotazione p) {

		Utente manager = getManager();

		String msg = "Il driver " + driver.getNome() + " " + driver.getCognome()
				+ " ha annullato la prenotazione del veicolo " + p.getTarga() + ".";

		Notifica n = new Notifica(null, TipoNotifica.PRENOTAZIONE, msg, LocalDateTime.now(), false,
				manager.getIdUtente(), null);

		salva(n);
	}

	// ============================================================
	// MANUTENZIONI
	// ============================================================

	public void notificaManutenzioneProgrammata(int idUtente, String targa, LocalDateTime data) {

		String msg = "Manutenzione programmata per il veicolo " + targa + " in data " + data.toLocalDate() + ".";

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}

	public void notificaManutenzioneConclusa(int idUtente, String targa) {

		String msg = "Manutenzione completata per il veicolo " + targa + ".";

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}

	public void notificaInterventoStraordinario(int idUtente, String targa) {

		String msg = "Intervento straordinario segnalato sul veicolo " + targa + ".";

		salva(new Notifica(null, TipoNotifica.MANUTENZIONE, msg, LocalDateTime.now(), false, idUtente, null));
	}

}
