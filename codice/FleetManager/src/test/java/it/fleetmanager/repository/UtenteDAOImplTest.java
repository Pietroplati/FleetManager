package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImplTest {

	private UtenteDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {

		// Forziamo DatabaseManager ad usare un DB H2 in-memory
		System.setProperty("jdbc.url.override", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");

		// Creiamo la tabella pulita per ogni test
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
				Statement st = conn.createStatement()) {

			st.execute("DROP TABLE IF EXISTS Utente");

			st.execute("""
					    CREATE TABLE Utente (
					        idUtente INT PRIMARY KEY,
					        nome VARCHAR(100),
					        cognome VARCHAR(100),
					        email VARCHAR(100),
					        password VARCHAR(100),
					        ruoloUtente VARCHAR(20),
					        patente VARCHAR(20)
					    );
					""");
		}

		dao = new UtenteDAOImpl();
	}

	@Test
	void testSaveAndGetByEmail() {

		Utente u = new Utente(1, "Mario", "Rossi", "mario@test.com", "pwd123", RuoloUtente.MANAGER);

		dao.save(u); // inserisce SOLO nel DB (ignora JSON nei test)

		Utente letto = dao.getUtenteByEmail("mario@test.com");

		assertNotNull(letto);
		assertEquals(1, letto.getIdUtente());
		assertEquals("Mario", letto.getNome());
		assertEquals(RuoloUtente.MANAGER, letto.getRuoloUtente());
	}

	@Test
	void testExistsByEmail() {
		Utente u = new Utente(2, "Luca", "Verdi", "luca@test.com", "abc", RuoloUtente.DRIVER);

		dao.save(u);

		assertTrue(dao.existsByEmail("luca@test.com"));
		assertFalse(dao.existsByEmail("non_esiste@test.com"));
	}

	@Test
	void testDelete() {
		Utente u = new Utente(3, "Anna", "Bianchi", "anna@test.com", "pass", RuoloUtente.DRIVER);

		dao.save(u);

		dao.delete(3);

		Utente letto = dao.getUtenteByEmail("anna@test.com");
		assertEquals(UtenteDAOImpl.UTENTE_INESISTENTE, letto);
	}

	@Test
	void testUpdate() {
		Utente u = new Utente(4, "Pietro", "Plati", "pietro@test.com", "000", RuoloUtente.DRIVER);

		dao.save(u);

		u.setNome("PietroMod");
		u.setPassword("xyz");

		dao.update(u);

		Utente letto = dao.getUtenteByEmail("pietro@test.com");

		assertEquals("PietroMod", letto.getNome());
		assertEquals("xyz", letto.getPassword());
	}
}
