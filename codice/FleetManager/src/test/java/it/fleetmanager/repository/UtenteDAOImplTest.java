package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImplTest {

    private UtenteDAO utenteDAO;

    @BeforeEach
    void setup() throws Exception {
        // DB H2 in RAM con schema e seed identici al reale
        DatabaseTestUtils.resetDatabase();
        utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
    }

    @Test
    void testSaveAndGetById() {
        Utente u = new Utente(
                10,
                "Giulia",
                "Bianchi",
                "giulia@example.com",
                "pwd",
                RuoloUtente.DRIVER,
                "ABC123"
        );

        utenteDAO.save(u);

        Utente loaded = utenteDAO.getUtenteById(10);

        assertEquals(10, loaded.getIdUtente());
        assertEquals("Giulia", loaded.getNome());
        assertEquals("Bianchi", loaded.getCognome());
        assertEquals("giulia@example.com", loaded.getEmail());
        assertEquals(RuoloUtente.DRIVER, loaded.getRuoloUtente());
        assertEquals("ABC123", loaded.getPatente());
    }

    @Test
    void testGetUtenteById_NotFound() {
        Utente u = utenteDAO.getUtenteById(999);
        assertEquals(-1, u.getIdUtente());
    }

    @Test
    void testGetUtenteByEmail() {
        Utente u = utenteDAO.getUtenteByEmail("driver@example.com");

        assertEquals(2, u.getIdUtente());
        assertEquals("Luca", u.getNome());
        assertEquals(RuoloUtente.DRIVER, u.getRuoloUtente());
    }

    @Test
    void testGetUtenteByEmail_NotFound() {
        Utente u = utenteDAO.getUtenteByEmail("notfound@example.com");
        assertEquals(-1, u.getIdUtente());
    }

    @Test
    void testExistsByEmail() {
        assertTrue(utenteDAO.existsByEmail("manager@example.com"));
        assertFalse(utenteDAO.existsByEmail("inesistente@example.com"));
    }

    @Test
    void testUpdate() {
        Utente u = utenteDAO.getUtenteById(2);
        u.setNome("Luca Modificato");
        u.setEmail("luca.mod@example.com");

        utenteDAO.update(u);

        Utente updated = utenteDAO.getUtenteById(2);
        assertEquals("Luca Modificato", updated.getNome());
        assertEquals("luca.mod@example.com", updated.getEmail());
    }

    @Test
    void testDelete() {
        Utente u = new Utente(
                20,
                "Da",
                "Cancellare",
                "delete@example.com",
                "pwd",
                RuoloUtente.DRIVER
        );

        utenteDAO.save(u);
        utenteDAO.delete(20);

        Utente deleted = utenteDAO.getUtenteById(20);
        assertEquals(-1, deleted.getIdUtente());
    }

    @Test
    void testGetTuttiUtenti() {
        List<Utente> list = utenteDAO.getTuttiUtenti();

        // almeno i 2 seed
        assertTrue(list.size() >= 2);
        assertEquals(1, list.get(0).getIdUtente());
    }

    @Test
    void testGetManager() {
        Utente manager = utenteDAO.getManager();

        assertEquals(RuoloUtente.MANAGER, manager.getRuoloUtente());
        assertEquals(1, manager.getIdUtente());
    }
}
