package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.service.interfaces.GestoreManutenzioni;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoManutenzione;
import it.fleetmanager.util.TipoVeicolo;

class GestoreManutenzioniTest {

    private GestoreManutenzioni gestore;
    private VeicoloDAO veicoloDAO;
    private ManutenzioneDAO manutenzioneDAO;

    @BeforeEach
    void setup() throws Exception {

        // 🔥 reset DB H2 in RAM
        DatabaseTestUtils.resetDatabase();

        veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());
        manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());
        NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
        UtenteDAO utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());

        SistemaNotifiche sistemaNotifiche =
                new SistemaNotifiche(notificaDAO, utenteDAO);

        gestore = new GestoreManutenzioniImpl(
                manutenzioneDAO,
                veicoloDAO,
                sistemaNotifiche
        );
    }

    // ============================================================
    // PROGRAMMAZIONE MANUTENZIONE
    // ============================================================

    @Test
    void testProgrammareManutenzione() {

        // ARRANGE
        Veicolo v = new Veicolo(
                "AA111AA",
                TipoVeicolo.AUTO,
                "Fiat",
                "Panda",
                2020,
                StatoVeicolo.DISPONIBILE,
                30000
        );

        veicoloDAO.save(v);

        LocalDateTime data = LocalDateTime.now().plusDays(3);

        // ACT
        Manutenzione m = gestore.programmareManutenzione(
                v,
                data,
                TipoManutenzione.ORDINARIA,
                "Tagliando"
        );

        // ASSERT
        assertNotNull(m);
        assertEquals("AA111AA", m.getTarga());
        assertEquals(TipoManutenzione.ORDINARIA, m.getTipoManutenzione());

        Veicolo aggiornato = veicoloDAO.getVeicoloByTarga("AA111AA");
        assertEquals(StatoVeicolo.IN_MANUTENZIONE, aggiornato.getStatoVeicolo());

        List<Manutenzione> manutenzioni =
                manutenzioneDAO.findByVeicolo("AA111AA");

        assertEquals(1, manutenzioni.size());
    }

    // ============================================================
    // CHIUSURA MANUTENZIONE
    // ============================================================

    @Test
    void testChiudiManutenzione() {

        // ARRANGE
        Veicolo v = new Veicolo(
                "BB222BB",
                TipoVeicolo.AUTO,
                "VW",
                "Golf",
                2019,
                StatoVeicolo.IN_MANUTENZIONE,
                60000
        );
        veicoloDAO.save(v);

        Manutenzione m = new Manutenzione(
                1,
                LocalDateTime.now().minusDays(1),
                TipoManutenzione.ORDINARIA,
                "Cambio olio",
                "BB222BB"
        );
        manutenzioneDAO.save(m);

        // ACT
        gestore.chiudiManutenzione(1);

        // ASSERT
        Manutenzione aggiornata =
                manutenzioneDAO.getManutenzioneById(1);

        assertNotNull(aggiornata);
        assertTrue(aggiornata.getDescrizione().contains("CHIUSA"));

        Veicolo aggiornato = veicoloDAO.getVeicoloByTarga("BB222BB");
        assertEquals(StatoVeicolo.DISPONIBILE, aggiornato.getStatoVeicolo());
    }
}
