package it.fleetmanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.DatabaseSeeder;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

public class App {
    private static final Logger log = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        log.info("FleetManager avviato.");
        log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
        System.out.println("Hello from FleetManager");
        
        Utente utente = new Utente(4, "Mauro", "Zucchelli", "mauro@gmail.com", "Ciao1234", RuoloUtente.DRIVER, "U1446G487U");
        
        UtenteDAOImpl UDI = new UtenteDAOImpl();
        
        UDI.save(utente);
        DatabaseSeeder.main(null);
    }
}
