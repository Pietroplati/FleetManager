package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.DatabaseTestUtils;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.service.impl.GestoreLoginImpl;
import it.fleetmanager.util.RuoloUtente;

 class GestoreLoginTest {

    private GestoreLoginImpl gestoreLogin;
    private UtenteDAO utenteDAO;

    @BeforeEach
    void setupDatabase() throws Exception {
        DatabaseTestUtils.resetDatabase();
        utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
        gestoreLogin = new GestoreLoginImpl(utenteDAO);
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @Test
    void testLoginSuccess() {
        Utente u = gestoreLogin.login("manager@test.it", "pwd");
        assertNotNull(u);
        assertEquals(1, u.getIdUtente());
    }

    @Test
    void testLoginWrongPassword() {
        Utente u = gestoreLogin.login("manager@test.it", "wrong");
        assertNull(u);
    }

    @Test
    void testLoginEmailInesistente() {
        Utente u = gestoreLogin.login("fake@test.it", "abc");
        assertNull(u);
    }

    @Test
    void testLoginEmailVuota() {
        assertNull(gestoreLogin.login("", "abc"));
        assertNull(gestoreLogin.login("   ", "abc"));
    }

    @Test
    void testLoginNull() {
        assertNull(gestoreLogin.login(null, "abc"));
        assertNull(gestoreLogin.login("manager@test.it", null));
    }

    // ============================================================
    // CREATE UTENTE
    // ============================================================

    @Test
    void testCreateUtenteSuccess() {

        Utente nuovo = new Utente(
                100,
                "Anna",
                "Verdi",
                "anna@test.it",
                "pwd",
                RuoloUtente.DRIVER,
                "A"
        );

        boolean created = gestoreLogin.createUtente(nuovo);
        assertTrue(created);

        Utente u = utenteDAO.getUtenteByEmail("anna@test.it");
        assertEquals(100, u.getIdUtente());
    }

    @Test
    void testCreateUtenteEmailEsistente() {

        Utente dup = new Utente(
                200,
                "Mario2",
                "Rossi2",
                "manager@test.it",
                "pwd",
                RuoloUtente.DRIVER
        );

        assertFalse(gestoreLogin.createUtente(dup));
    }

    @Test
    void testCreateUtenteEmailNull() {

        Utente bad = new Utente(
                201,
                "X",
                "Y",
                null,
                "pwd",
                RuoloUtente.MANAGER
        );

        assertFalse(gestoreLogin.createUtente(bad));
    }

    // ============================================================
    // AGGIORNA PROFILO
    // ============================================================

    @Test
    void testAggiornaProfiloSuccess() {

        Utente u = utenteDAO.getUtenteById(1);
        u.setNome("MarioModificato");

        boolean ok = gestoreLogin.aggiornaProfilo(u);
        assertTrue(ok);

        Utente updated = utenteDAO.getUtenteById(1);
        assertEquals("MarioModificato", updated.getNome());
    }

    @Test
    void testAggiornaProfiloIdInvalido() {

        Utente fake = new Utente(
                -1,
                "X",
                "Y",
                "z@test.it",
                "pwd",
                RuoloUtente.DRIVER
        );

        assertFalse(gestoreLogin.aggiornaProfilo(fake));
    }

    // ============================================================
    // ELIMINA UTENTE
    // ============================================================

    @Test
    void testEliminaUtenteSuccess() {

        Utente nuovo = new Utente(
                150,
                "Test",
                "User",
                "delete@test.it",
                "pwd",
                RuoloUtente.DRIVER
        );

        gestoreLogin.createUtente(nuovo);

        boolean ok = gestoreLogin.eliminaUtente(150);
        assertTrue(ok);

        Utente deleted = utenteDAO.getUtenteById(150);
        assertEquals(UtenteDAOImpl.UTENTE_INESISTENTE, deleted);
    }

    @Test
    void testEliminaUtenteIdInesistente() {
        assertFalse(gestoreLogin.eliminaUtente(999));
    }

    @Test
    void testEliminaUtenteIdNegativo() {
        assertFalse(gestoreLogin.eliminaUtente(-5));
    }

    // ============================================================
    // GET UTENTE BY EMAIL
    // ============================================================

    @Test
    void testGetUtenteByEmailSuccess() {

        Utente u = gestoreLogin.getUtenteByEmail("manager@test.it");

        assertNotNull(u);
        assertEquals("Mario", u.getNome());
    }

    @Test
    void testGetUtenteByEmailInvalid() {
        assertNull(gestoreLogin.getUtenteByEmail("notfound@test.it"));
    }

    @Test
    void testGetUtenteByEmailNull() {
        assertNull(gestoreLogin.getUtenteByEmail(null));
        assertNull(gestoreLogin.getUtenteByEmail("   "));
    }

    // ============================================================
    // GET TUTTI UTENTI
    // ============================================================

    @Test
    void testGetTuttiUtenti() {

        var lista = gestoreLogin.getTuttiUtenti();

        assertTrue(lista.size() >= 2);
        assertTrue(lista.stream().anyMatch(u -> u.getEmail().equals("manager@test.it")));
        assertTrue(lista.stream().anyMatch(u -> u.getEmail().equals("driver@test.it")));
    }
}
