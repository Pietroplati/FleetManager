package it.fleetmanager.util;

import java.time.LocalDateTime;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;

public class SistemaNotifiche {

	private NotificaDAO notificaDAO;

	public SistemaNotifiche(NotificaDAO notificaDAO) {
		this.notificaDAO = notificaDAO;
	}

	public void inviaNotifica(Utente destinatario, String messaggio) {

		// ID = null → autoincrement
		Notifica n = new Notifica(
				null,                           
				TipoNotifica.SEGNALAZIONE,
				messaggio,
				LocalDateTime.now(),
				false,
				destinatario.getIdUtente(),
				null
		);

		notificaDAO.save(n);

		System.out.println("[NOTIFICA] A " + destinatario.getNome() + ": " + messaggio);
	}

	public void inviaNotificaScadenza(Utente manager, Scadenza scadenza) {

		String msg = "Scadenza " + scadenza.getTipoScadenza() + " per il veicolo " + scadenza.getTarga()
				+ " fissata al " + scadenza.getData();

		// ID = null → autoincrement
		Notifica n = new Notifica(
				null,                           // <--- CORRETTO
				TipoNotifica.SCADENZA,
				msg,
				LocalDateTime.now(),
				false,
				manager.getIdUtente(),
				scadenza.getIdScadenza()
		);

		notificaDAO.save(n);

		System.out.println("[SCADENZA] Manager " + manager.getNome() + ": " + msg);
	}

	public void inviaNotificaPrenotazione(Utente driver, Prenotazione prenotazione) {

		String msg = "Aggiornamento prenotazione del veicolo " + prenotazione.getTarga() + " dal "
				+ prenotazione.getDataInizio() + " al " + prenotazione.getDataFine() + " | Stato: "
				+ prenotazione.getStato();

		// ID = null → autoincrement
		Notifica n = new Notifica(
				null,                           // <--- CORRETTO
				TipoNotifica.PRENOTAZIONE,
				msg,
				LocalDateTime.now(),
				false,
				driver.getIdUtente(),
				null
		);

		notificaDAO.save(n);

		System.out.println("[PRENOTAZIONE] Driver " + driver.getNome() + ": " + msg);
	}
}
