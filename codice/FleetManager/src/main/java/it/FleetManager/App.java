package it.fleetmanager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.ElencaDati;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;
import it.fleetmanager.util.TipoScadenza;

public class App {
	private static final Logger log = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		log.info("FleetManager avviato.");
		log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
		System.out.println("Hello from FleetManager");

		ScadenzaDAOImpl SDI = new ScadenzaDAOImpl();

		List<Scadenza> scadenze;

		scadenze = SDI.findByVeicolo("GH819RJ");

		for (Scadenza scadenza : scadenze) {
			System.out.println(scadenza.toString());
		}

		SDI.delete(77);

		scadenze = SDI.findByVeicolo("GH819RJ");

		for (Scadenza scadenza : scadenze) {
			System.out.println(scadenza.toString());
		}
	}
}
