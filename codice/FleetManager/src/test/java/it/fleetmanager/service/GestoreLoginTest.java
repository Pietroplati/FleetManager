package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.UtenteDAO;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

public class GestoreLoginTest {

	private GestoreLogin gestoreLogin;
	private UtenteDAO utenteDAO;

	private static final String TEST_DB_URL = "jdbc:h2:mem:fleetdb_test;DB_CLOSE_DELAY=-1";

	@BeforeEach
	void setupDatabase() throws Exception {

		// Forza DatabaseManager a usare l'URL in-memory
		DatabaseManager.setTestUrl(TEST_DB_URL);

		// Creiamo la tabella Utente in RAM
		try (Connection conn = DatabaseManager.getInstance().getConnection(); Statement st = conn.createStatement()) {

			st.execute("""
					    DROP TABLE IF EXISTS Utente;
					""");

			st.execute("""
					    CREATE TABLE Utente (
					        idUtente INT PRIMARY KEY,
					        nome VARCHAR(50) NOT NULL,
					        cognome VARCHAR(50) NOT NULL,
					        email VARCHAR(100) NOT NULL,
					        password VARCHAR(100) NOT NULL,
					        ruoloUtente VARCHAR(20) NOT NULL,
					        patente VARCHAR(50)
					    );
					""");
		}

		// Creiamo DAO reale collegato al DB in-memory
		utenteDAO = new UtenteDAOImpl();
		gestoreLogin = new GestoreLogin(utenteDAO);

		// Inseriamo alcuni utenti di test
		utenteDAO.save(new Utente(1, "Mario", "Rossi", "mario@example.com", "pass123", RuoloUtente.DRIVER, "B"));

		utenteDAO.save(new Utente(2, "Luca", "Bianchi", "luca@example.com", "admin", RuoloUtente.MANAGER));
	}

	@Test
	void testLoginSuccess() {
		Utente u = gestoreLogin.login("mario@example.com", "pass123");
		assertNotNull(u);
		assertEquals(1, u.getIdUtente());
	}

	@Test
	void testLoginWrongPassword() {
		Utente u = gestoreLogin.login("mario@example.com", "wrong");
		assertNull(u);
	}

	@Test
	void testLoginEmailInesistente() {
		Utente u = gestoreLogin.login("fake@example.com", "abc");
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
		assertNull(gestoreLogin.login("mario@example.com", null));
	}

	@Test
	void testCreateUtenteSuccess() {

		Utente nuovo = new Utente(3, "Anna", "Verdi", "anna@example.com", "pwd", RuoloUtente.DRIVER, "A");

		boolean created = gestoreLogin.createUtente(nuovo);

		assertTrue(created);

		Utente u = utenteDAO.getUtenteByEmail("anna@example.com");
		assertEquals(3, u.getIdUtente());
	}

	@Test
	void testCreateUtenteEmailEsistente() {

		Utente dup = new Utente(10, "Mario2", "Rossi2", "mario@example.com", "pwd", RuoloUtente.DRIVER);

		boolean created = gestoreLogin.createUtente(dup);

		assertFalse(created);
	}

	@Test
	void testCreateUtenteEmailNull() {
		Utente bad = new Utente(20, "X", "Y", null, "pwd", RuoloUtente.MANAGER);
		assertFalse(gestoreLogin.createUtente(bad));
	}

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
		Utente fake = new Utente(-1, "X", "Y", "z@z", "pwd", RuoloUtente.DRIVER);
		assertFalse(gestoreLogin.aggiornaProfilo(fake));
	}

	@Test
	void testEliminaUtenteSuccess() {
		boolean ok = gestoreLogin.eliminaUtente(1);
		assertTrue(ok);

		Utente deleted = utenteDAO.getUtenteById(1);
		assertEquals(-1, deleted.getIdUtente()); // UTENTE_INESISTENTE
	}

	@Test
	void testEliminaUtenteIdInesistente() {
		assertFalse(gestoreLogin.eliminaUtente(999));
	}

	@Test
	void testEliminaUtenteIdNegativo() {
		assertFalse(gestoreLogin.eliminaUtente(-5));
	}

	@Test
	void testGetUtenteByEmailSuccess() {
		Utente u = gestoreLogin.getUtenteByEmail("mario@example.com");
		assertNotNull(u);
		assertEquals("Mario", u.getNome());
	}

	@Test
	void testGetUtenteByEmailInvalid() {
		assertNull(gestoreLogin.getUtenteByEmail("notfound@mail.com"));
	}

	@Test
	void testGetUtenteByEmailNull() {
		assertNull(gestoreLogin.getUtenteByEmail(null));
		assertNull(gestoreLogin.getUtenteByEmail("   "));
	}

	@Test
	void testGetTuttiUtenti() {
		var lista = gestoreLogin.getTuttiUtenti();
		assertEquals(2, lista.size()); // Mario e Luca
	}
}
