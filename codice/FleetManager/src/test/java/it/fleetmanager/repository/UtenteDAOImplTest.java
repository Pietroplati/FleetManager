package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImplTest {

    private UtenteDAO utenteDAO;

    @BeforeEach
    void setup() throws Exception {
        DatabaseTestUtils.resetDatabase();
        utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
    }

    // ============================================================
    // SAVE + GET BY ID
    // ============================================================
    @Test
    void testSaveAndGetById() {

        Utente u = new Utente(
                10,
                "Giulia",
                "Bianchi",
                "giulia@test.it",
                "pwd",
                RuoloUtente.DRIVER,
                "ABC123"
        );

        utenteDAO.save(u);

        Utente loaded = utenteDAO.getUtenteById(10);

        assertEquals(10, loaded.getIdUtente());
        assertEquals("Giulia", loaded.getNome());
        assertEquals("Bianchi", loaded.getCognome());
        assertEquals("giulia@test.it", loaded.getEmail());
        assertEquals(RuoloUtente.DRIVER, loaded.getRuoloUtente());
        assertEquals("ABC123", loaded.getPatente());
    }

    // ============================================================
    // GET BY ID NOT FOUND
    // ============================================================
    @Test
    void testGetUtenteById_NotFound() {
        Utente u = utenteDAO.getUtenteById(9999);
        assertEquals(-1, u.getIdUtente());
    }

    // ============================================================
    // GET BY EMAIL (SEED)
    // ============================================================
    @Test
    void testGetUtenteByEmail() {

        Utente u = utenteDAO.getUtenteByEmail("driver@test.it");

        assertEquals(2, u.getIdUtente());
        assertEquals("Luca", u.getNome());
        assertEquals(RuoloUtente.DRIVER, u.getRuoloUtente());
    }

    @Test
    void testGetUtenteByEmail_NotFound() {
        Utente u = utenteDAO.getUtenteByEmail("notfound@test.it");
        assertEquals(-1, u.getIdUtente());
    }

    // ============================================================
    // EXISTS BY EMAIL
    // ============================================================
    @Test
    void testExistsByEmail() {
        assertTrue(utenteDAO.existsByEmail("manager@test.it"));
        assertFalse(utenteDAO.existsByEmail("inesistente@test.it"));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Test
    void testUpdate() {

        Utente u = utenteDAO.getUtenteById(2);

        u.setNome("Luca Modificato");
        u.setEmail("luca.mod@test.it");

        utenteDAO.update(u);

        Utente updated = utenteDAO.getUtenteById(2);

        assertEquals("Luca Modificato", updated.getNome());
        assertEquals("luca.mod@test.it", updated.getEmail());
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Test
    void testDelete() {

        Utente u = new Utente(
                20,
                "Da",
                "Cancellare",
                "delete@test.it",
                "pwd",
                RuoloUtente.DRIVER
        );

        utenteDAO.save(u);
        utenteDAO.delete(20);

        Utente deleted = utenteDAO.getUtenteById(20);
        assertEquals(-1, deleted.getIdUtente());
    }

    // ============================================================
    // GET TUTTI UTENTI
    // ============================================================
    @Test
    void testGetTuttiUtenti() {

        List<Utente> list = utenteDAO.getTuttiUtenti();

        assertTrue(list.size() >= 2);
        assertEquals(1, list.get(0).getIdUtente());
    }

    // ============================================================
    // GET MANAGER
    // ============================================================
    @Test
    void testGetManager() {

        Utente manager = utenteDAO.getManager();

        assertEquals(RuoloUtente.MANAGER, manager.getRuoloUtente());
        assertEquals(1, manager.getIdUtente());
    }
}
