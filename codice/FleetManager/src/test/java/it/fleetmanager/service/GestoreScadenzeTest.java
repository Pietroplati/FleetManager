package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.DatabaseTestUtils;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.service.impl.GestoreScadenzeImpl;
import it.fleetmanager.service.impl.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoScadenza;
import it.fleetmanager.util.TipoVeicolo;

public class GestoreScadenzeTest {

    private ScadenzaDAO scadenzaDAO;
    private NotificaDAO notificaDAO;
    private VeicoloDAO veicoloDAO;

    private SistemaNotifiche sistemaNotifiche;
    private GestoreScadenzeImpl gestore;

    private Veicolo veicolo;
    private String targaTest;

    @BeforeEach
    void setup() throws Exception {

        //reset DB H2 in RAM
        DatabaseTestUtils.resetDatabase();

        scadenzaDAO = new ScadenzaDAOImpl(H2DatabaseManager.getInstance());
        notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
        veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());

        // UtenteDAO serve solo a SistemaNotifiche
        UtenteDAO utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
        sistemaNotifiche = new SistemaNotifiche(notificaDAO, utenteDAO);

        gestore = new GestoreScadenzeImpl(
                scadenzaDAO,
                veicoloDAO,
                utenteDAO, sistemaNotifiche
        );

        //TARGA UNICA e ≤ 10 caratteri
        targaTest = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);

        veicolo = new Veicolo(
                targaTest,
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2018,
                StatoVeicolo.DISPONIBILE,
                10000
        );

        veicoloDAO.save(veicolo);
    }

    // ============================================================
    // CONTROLLA SCADENZE ENTRO
    // ============================================================

    @Test
    void testControllaScadenzeEntroNessuna() {

        Scadenza result =
                gestore.controllaScadenzeEntro(LocalDate.now().minusDays(5));

        assertEquals(-1, result.getIdScadenza());
    }

    @Test
    void testControllaScadenzeEntroSuccess() {

        Scadenza s = new Scadenza(
                50,
                TipoScadenza.BOLLO,
                LocalDate.now().plusDays(3),
                false,
                targaTest
        );

        scadenzaDAO.save(s);

        Scadenza result =
                gestore.controllaScadenzeEntro(LocalDate.now().plusDays(5));

        assertEquals(50, result.getIdScadenza());
    }

    // ============================================================
    // MARCA COME NOTIFICATA
    // ============================================================

    @Test
    void testMarcaComeNotificataSuccess() {

        Scadenza s = new Scadenza(
                70,
                TipoScadenza.REVISIONE,
                LocalDate.now().plusDays(10),
                false,
                targaTest
        );

        scadenzaDAO.save(s);

        gestore.marcaComeNotificata(70);

        Scadenza updated = scadenzaDAO.getScadenzaById(70);
        assertTrue(updated.getNotificata());
    }

    @Test
    void testMarcaComeNotificataInesistente() {

        assertThrows(
                IllegalArgumentException.class,
                () -> gestore.marcaComeNotificata(9999)
        );
    }

    // ============================================================
    // BLOCCA VEICOLO SE SCADUTA
    // ============================================================

    @Test
    void testBloccaVeicoloSeScaduta() {

        Scadenza s = new Scadenza(
                80,
                TipoScadenza.ASSICURAZIONE,
                LocalDate.now().minusDays(1),
                false,
                targaTest
        );

        scadenzaDAO.save(s);

        gestore.bloccaVeicoloSeScaduta(veicolo);

        Veicolo aggiornato =
                veicoloDAO.getVeicoloByTarga(targaTest);

        assertEquals(
                StatoVeicolo.NON_DISPONIBILE,
                aggiornato.getStatoVeicolo()
        );
    }

    // ============================================================
    // CONTROLLO PERIODICO
    // ============================================================

    @Test
    void testEseguiControlloPeriodico() {

        Scadenza s = new Scadenza(
                90,
                TipoScadenza.TAGLIANDO,
                LocalDate.now().plusDays(4),
                false,
                targaTest
        );

        scadenzaDAO.save(s);

        gestore.eseguiControlloPeriodico();

        Scadenza updated = scadenzaDAO.getScadenzaById(90);
        assertTrue(updated.getNotificata());

        List<Notifica> notifiche =
                notificaDAO.findByScadenza(90);

        assertEquals(1, notifiche.size());
    }
}
