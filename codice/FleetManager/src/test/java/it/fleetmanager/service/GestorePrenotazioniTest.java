package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;
import it.fleetmanager.util.TipoVeicolo;

class GestorePrenotazioniTest {

    private GestorePrenotazioni gestore;
    private PrenotazioneDAO prenotazioneDAO;
    private VeicoloDAO veicoloDAO;
    private UtenteDAO utenteDAO;

    private Utente driver;
    private Utente manager;
    private Veicolo veicolo;

    @BeforeEach
    void setup() throws Exception {

        // 🔥 reset DB H2 in RAM
        DatabaseTestUtils.resetDatabase();

        prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());
        veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());
        utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
        NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());

        SistemaNotifiche sistemaNotifiche =
                new SistemaNotifiche(notificaDAO, utenteDAO);

        gestore = new GestorePrenotazioniImpl(
                prenotazioneDAO,
                veicoloDAO,
                utenteDAO,
                sistemaNotifiche
        );

        // =========================
        // DATI DI BASE
        // =========================

        manager = new Utente(
                1,
                "Mario",
                "Rossi",
                "manager@fleet.it",
                "pwd",
                RuoloUtente.MANAGER,
                null
        );

        driver = new Utente(
                2,
                "Luca",
                "Bianchi",
                "driver@fleet.it",
                "pwd",
                RuoloUtente.DRIVER,
                "AB123456"
        );

        utenteDAO.save(manager);
        utenteDAO.save(driver);

        veicolo = new Veicolo(
                "AA111AA",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2020,
                it.fleetmanager.util.StatoVeicolo.DISPONIBILE,
                30000
        );

        veicoloDAO.save(veicolo);
    }

    // ============================================================
    // CREA PRENOTAZIONE
    // ============================================================

    @Test
    void testCreaPrenotazione() {

        LocalDateTime inizio = LocalDateTime.now().plusHours(1);
        LocalDateTime fine = LocalDateTime.now().plusHours(3);

        Prenotazione p = gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        assertNotNull(p);
        assertEquals(StatoPrenotazione.RICHIESTA, p.getStato());
        assertEquals(TipoPrenotazione.UTENTE, p.getTipoPrenotazione());

        List<Prenotazione> lista =
                prenotazioneDAO.findByVeicolo(veicolo.getTarga());

        assertEquals(1, lista.size());
    }

    // ============================================================
    // CONFERMA PRENOTAZIONE (MANAGER)
    // ============================================================

    @Test
    void testConfermaPrenotazione() {

        LocalDateTime inizio = LocalDateTime.now().plusHours(1);
        LocalDateTime fine = LocalDateTime.now().plusHours(4);

        Prenotazione p =
                gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        gestore.confermaPrenotazione(p.getIdPrenotazione(), manager);

        Prenotazione aggiornata =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.CONFERMATA, aggiornata.getStato());
    }

    // ============================================================
    // ANNULLA PRENOTAZIONE (DRIVER)
    // ============================================================

    @Test
    void testAnnullaPrenotazioneDaDriver() {

        LocalDateTime inizio = LocalDateTime.now().plusHours(2);
        LocalDateTime fine = LocalDateTime.now().plusHours(5);

        Prenotazione p =
                gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        gestore.annullaPrenotazione(p.getIdPrenotazione(), driver);

        Prenotazione aggiornata =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.ANNULLATA, aggiornata.getStato());
    }

    // ============================================================
    // ATTIVA PRENOTAZIONE
    // ============================================================

    @Test
    void testAttivaPrenotazione() {

        LocalDateTime inizio = LocalDateTime.now().minusHours(1);
        LocalDateTime fine = LocalDateTime.now().plusHours(2);

        Prenotazione p = new Prenotazione(
                0,
                inizio,
                fine,
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                driver.getIdUtente(),
                veicolo.getTarga()
        );

        prenotazioneDAO.save(p);

        gestore.attivaPrenotazione(p.getIdPrenotazione());

        Prenotazione aggiornata =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.ATTIVA, aggiornata.getStato());
    }

    // ============================================================
    // COMPLETA PRENOTAZIONE
    // ============================================================

    @Test
    void testCompletaPrenotazione() {

        LocalDateTime inizio = LocalDateTime.now().minusHours(3);
        LocalDateTime fine = LocalDateTime.now().minusHours(1);

        Prenotazione p = new Prenotazione(
                0,
                inizio,
                fine,
                StatoPrenotazione.ATTIVA,
                TipoPrenotazione.UTENTE,
                driver.getIdUtente(),
                veicolo.getTarga()
        );

        prenotazioneDAO.save(p);

        gestore.completaPrenotazione(p.getIdPrenotazione());

        Prenotazione aggiornata =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.COMPLETATA, aggiornata.getStato());
    }

    // ============================================================
    // QUERY
    // ============================================================

    @Test
    void testGetPrenotazioniDriver() {

        LocalDateTime inizio = LocalDateTime.now().plusHours(1);
        LocalDateTime fine = LocalDateTime.now().plusHours(2);

        gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        List<Prenotazione> lista =
                gestore.getPrenotazioniDriver(driver);

        assertEquals(1, lista.size());
    }

    @Test
    void testGetPrenotazioniVeicolo() {

        LocalDateTime inizio = LocalDateTime.now().plusHours(1);
        LocalDateTime fine = LocalDateTime.now().plusHours(3);

        gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        List<Prenotazione> lista =
                gestore.getPrenotazioniVeicolo(veicolo);

        assertEquals(1, lista.size());
    }
}
