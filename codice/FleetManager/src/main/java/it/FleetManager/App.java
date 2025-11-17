package it.fleetmanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.repository.impl.UtenteDAOImpl;

public class App {
	private static final Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		log.info("FleetManager avviato.");
		log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
		System.out.println("Hello from FleetManager");

		UtenteDAOImpl UDI=new UtenteDAOImpl();
		
	
	}
}
