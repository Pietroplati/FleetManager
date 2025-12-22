package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImplTest {

    private NotificaDAO notificaDAO;

    @BeforeEach
    void setup() throws Exception {
        // DB H2 in RAM, schema identico a quello reale
        DatabaseTestUtils.resetDatabase();
        notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
    }

    @Test
    void testSaveAndGetById() {
        // Arrange
        Notifica n = new Notifica(
                null,
                TipoNotifica.PRENOTAZIONE,
                "Prenotazione confermata",
                LocalDateTime.of(2025, 1, 10, 9, 30),
                false,
                2,
                null
        );

        // Act
        notificaDAO.save(n);
        Notifica loaded = notificaDAO.getNotificaById(1);

        // Assert
        assertEquals(1, loaded.getIdNotifica());
        assertEquals(TipoNotifica.PRENOTAZIONE, loaded.getTipoNotifica());
        assertEquals("Prenotazione confermata", loaded.getMessaggio());
        assertFalse(loaded.getLetta());
        assertEquals(2, loaded.getIdUtente());
        assertNull(loaded.getIdScadenza());
    }

    @Test
    void testGetNotificaById_NotFound() {
        Notifica n = notificaDAO.getNotificaById(999);
        assertEquals(-1, n.getIdNotifica());
    }

    @Test
    void testUpdate() {
        // Arrange
        Notifica n = new Notifica(
                null,
                TipoNotifica.MANUTENZIONE,
                "Manutenzione programmata",
                false,
                1,
                null
        );

        notificaDAO.save(n);

        // Act
        Notifica saved = notificaDAO.getNotificaById(1);
        saved.setLetta(true);
        saved.setMessaggio("Manutenzione completata");
        notificaDAO.update(saved);

        Notifica updated = notificaDAO.getNotificaById(1);

        // Assert
        assertTrue(updated.getLetta());
        assertEquals("Manutenzione completata", updated.getMessaggio());
    }

    @Test
    void testDelete() {
        // Arrange
        Notifica n = new Notifica(
                null,
                TipoNotifica.SEGNALAZIONE,
                "Segnalazione veicolo",
                false,
                1,
                null
        );
        notificaDAO.save(n);

        // Act
        notificaDAO.delete(1);

        // Assert
        Notifica deleted = notificaDAO.getNotificaById(1);
        assertEquals(-1, deleted.getIdNotifica());
    }

    @Test
    void testFindByUtente() {
        // Arrange
        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.PRENOTAZIONE,
                "N1",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false,
                2,
                null
        ));

        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.PRENOTAZIONE,
                "N2",
                LocalDateTime.of(2025, 1, 2, 9, 0),
                false,
                2,
                null
        ));

        // Act
        List<Notifica> list = notificaDAO.findByUtente(2);

        // Assert
        assertEquals(2, list.size());
        assertTrue(list.get(0).getDataInvio().isAfter(list.get(1).getDataInvio()));
    }

    @Test
    void testFindNonLette() {
        // Arrange
        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.SCADENZA,
                "Scadenza bollo",
                false,
                1,
                1
        ));

        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.SCADENZA,
                "Scadenza assicurazione",
                true,
                1,
                2
        ));

        // Act
        List<Notifica> list = notificaDAO.findNonLette(1);

        // Assert
        assertEquals(1, list.size());
        assertFalse(list.get(0).getLetta());
    }

    @Test
    void testFindByScadenza() {
        // Arrange
        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.SCADENZA,
                "Bollo in scadenza",
                false,
                1,
                1
        ));

        notificaDAO.save(new Notifica(
                null,
                TipoNotifica.SCADENZA,
                "Assicurazione in scadenza",
                false,
                1,
                1
        ));

        // Act
        List<Notifica> list = notificaDAO.findByScadenza(1);

        // Assert
        assertEquals(2, list.size());
        assertTrue(list.get(0).getDataInvio().isAfter(list.get(1).getDataInvio()));
    }

    @Test
    void testFindByScadenza_Null() {
        List<Notifica> list = notificaDAO.findByScadenza(null);
        assertTrue(list.isEmpty());
    }
}
