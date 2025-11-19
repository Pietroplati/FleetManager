package it.fleetmanager;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.TipoNotifica;

public class App {
	private static final Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		log.info("FleetManager avviato.");
		log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
		System.out.println("Hello from FleetManager");

		UtenteDAOImpl pes = new UtenteDAOImpl();
		List<Utente> utenti = pes.getTuttiUtenti();
		for (Utente utente : utenti) {
			System.out.println(utente.toString());
		}
		
	}
}
