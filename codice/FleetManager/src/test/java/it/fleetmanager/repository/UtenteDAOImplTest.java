package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImplTest {

	private UtenteDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new UtenteDAOImpl(H2DatabaseManager.getInstance());
	}

	@Test
	void testSaveAndGetByEmail() {
		Utente u = new Utente(10, "Mario", "Rossi", "mario@test.com", "pwd", RuoloUtente.MANAGER);
		dao.save(u);

		Utente letto = dao.getUtenteByEmail("mario@test.com");

		assertEquals(10, letto.getIdUtente());
		assertEquals("Mario", letto.getNome());
		assertEquals("Rossi", letto.getCognome());
		assertEquals(RuoloUtente.MANAGER, letto.getRuoloUtente());
	}

	@Test
	void testExistsByEmail() {
		Utente u = new Utente(20, "Luca", "Verdi", "luca@test.com", "abc", RuoloUtente.DRIVER);
		dao.save(u);

		assertTrue(dao.existsByEmail("luca@test.com"));
		assertFalse(dao.existsByEmail("non_esiste@test.com"));
	}

	@Test
	void testDelete() {
		Utente u = new Utente(30, "Anna", "Bianchi", "anna@test.com", "pass", RuoloUtente.DRIVER);
		dao.save(u);

		dao.delete(30);

		Utente letto = dao.getUtenteByEmail("anna@test.com");
		assertEquals(UtenteDAOImpl.UTENTE_INESISTENTE, letto);
	}

	@Test
	void testUpdate() {
		Utente u = new Utente(40, "Pietro", "Plati", "pietro@test.com", "000", RuoloUtente.DRIVER);
		dao.save(u);

		u.setNome("PietroMod");
		u.setPassword("PWD_NEW");

		dao.update(u);

		Utente letto = dao.getUtenteById(40);

		assertEquals("PietroMod", letto.getNome());
		assertEquals("PWD_NEW", letto.getPassword());
	}
}
