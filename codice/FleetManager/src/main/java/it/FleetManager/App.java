package it.fleetmanager;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

public class App {
    private static final Logger log = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        log.info("FleetManager avviato.");
        log.debug("Messaggio di debug (vedrai questo solo se il livello è DEBUG).");
        System.out.println("Hello from FleetManager");
        
        Utente utente = new Utente(4, "Mauro", "Zucchelli", "mauro@gmail.com", "Ciao1234", RuoloUtente.DRIVER, "U1446G487U");
        
        System.out.println(utente.toString());
        UtenteDAOImpl UDI = new UtenteDAOImpl();
        /*
        UDI.save(utente);
        utente.setNome("Giovanni");
        utente.setCognome("Perini");
        utente.setEmail("juan@gmail.com");
        UDI.update(utente);
        */
        Optional<Utente> utentePaolo;
        utentePaolo=UDI.getUtenteByEmail("marco.rossi@example.com");
        System.out.println(utentePaolo.toString());
       
    }
}
