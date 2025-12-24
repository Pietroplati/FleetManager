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
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;
import it.fleetmanager.util.TipoVeicolo;
import it.fleetmanager.util.StatoVeicolo;

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

        // 🔹 UTENTI GIÀ SEEDATI
        manager = utenteDAO.getUtenteById(1);
        driver = utenteDAO.getUtenteById(2);

        assertEquals(RuoloUtente.MANAGER, manager.getRuoloUtente());
        assertEquals(RuoloUtente.DRIVER, driver.getRuoloUtente());

        // 🔹 VEICOLO DI TEST
        veicolo = new Veicolo(
                "TEST123",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2021,
                StatoVeicolo.DISPONIBILE,
                20000
        );

        veicoloDAO.save(veicolo);
    }

    // ============================================================
    // CREA PRENOTAZIONE
    // ============================================================

    @Test
    void testCreaPrenotazione() {

        LocalDateTime inizio = LocalDateTime.now().plusDays(1);
        LocalDateTime fine = LocalDateTime.now().plusDays(1).plusHours(2);

        Prenotazione p = gestore.creaPrenotazione(driver, veicolo, inizio, fine);

        assertNotNull(p);
        assertEquals(StatoPrenotazione.RICHIESTA, p.getStato());
        assertEquals(TipoPrenotazione.UTENTE, p.getTipoPrenotazione());

        List<Prenotazione> lista =
                prenotazioneDAO.findByVeicolo(veicolo.getTarga());

        assertEquals(1, lista.size());
    }

    // ============================================================
    // CONFERMA PRENOTAZIONE
    // ============================================================

    @Test
    void testConfermaPrenotazione() {

        Prenotazione p = gestore.creaPrenotazione(
                driver,
                veicolo,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(3)
        );

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

        Prenotazione p = gestore.creaPrenotazione(
                driver,
                veicolo,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2)
        );

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

        Prenotazione p = new Prenotazione(
                0,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
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

        Prenotazione p = new Prenotazione(
                0,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1),
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

        gestore.creaPrenotazione(
                driver,
                veicolo,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );

        List<Prenotazione> lista =
                gestore.getPrenotazioniDriver(driver);

        assertEquals(1, lista.size());
    }

    @Test
    void testGetPrenotazioniVeicolo() {

        gestore.creaPrenotazione(
                driver,
                veicolo,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2)
        );

        List<Prenotazione> lista =
                gestore.getPrenotazioniVeicolo(veicolo);

        assertEquals(1, lista.size());
    }
}
