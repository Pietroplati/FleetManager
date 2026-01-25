package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.util.RuoloUtente;

/**
 * Test di integrazione per {@link UtenteDAOImpl}.
 * <p>
 * Verifica il corretto funzionamento delle operazioni CRUD e delle query
 * principali relative all'entità {@link Utente}, utilizzando il database H2 di
 * test inizializzato prima di ogni esecuzione.
 * </p>
 */
public class UtenteDAOImplTest {

	private UtenteDAO utenteDAO;

	/**
	 * Inizializza il database di test e il DAO prima di ogni test.
	 *
	 * @throws Exception in caso di errore durante il reset del database
	 */
	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());
	}

	/**
	 * Verifica la corretta esecuzione di save() e getUtenteById().
	 */
	@Test
	void testSaveAndGetById() {

		Utente u = new Utente(10, "Giulia", "Bianchi", "giulia@test.it", "pwd", RuoloUtente.DRIVER, "ABC123");

		utenteDAO.save(u);

		Utente loaded = utenteDAO.getUtenteById(10);

		assertEquals(10, loaded.getIdUtente());
		assertEquals("Giulia", loaded.getNome());
		assertEquals("Bianchi", loaded.getCognome());
		assertEquals("giulia@test.it", loaded.getEmail());
		assertEquals(RuoloUtente.DRIVER, loaded.getRuoloUtente());
		assertEquals("ABC123", loaded.getPatente());
	}

	/**
	 * Verifica che la ricerca per ID inesistente restituisca l'oggetto sentinella.
	 */
	@Test
	void testGetUtenteByIdNotFound() {
		Utente u = utenteDAO.getUtenteById(9999);
		assertEquals(-1, u.getIdUtente());
	}

	/**
	 * Verifica il recupero di un utente tramite email presente nel seed iniziale.
	 */
	@Test
	void testGetUtenteByEmail() {

		Utente u = utenteDAO.getUtenteByEmail("driver@test.it");

		assertEquals(2, u.getIdUtente());
		assertEquals("Luca", u.getNome());
		assertEquals(RuoloUtente.DRIVER, u.getRuoloUtente());
	}

	/**
	 * Verifica il comportamento di getUtenteByEmail() con email inesistente.
	 */
	@Test
	void testGetUtenteByEmailNotFound() {
		Utente u = utenteDAO.getUtenteByEmail("notfound@test.it");
		assertEquals(-1, u.getIdUtente());
	}

	/**
	 * Verifica il metodo existsByEmail().
	 */
	@Test
	void testExistsByEmail() {
		assertTrue(utenteDAO.existsByEmail("manager@test.it"));
		assertFalse(utenteDAO.existsByEmail("inesistente@test.it"));
	}

	/**
	 * Verifica la corretta esecuzione di update().
	 */
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

	/**
	 * Verifica la corretta esecuzione di delete().
	 */
	@Test
	void testDelete() {

		Utente u = new Utente(20, "Da", "Cancellare", "delete@test.it", "pwd", RuoloUtente.DRIVER);

		utenteDAO.save(u);
		utenteDAO.delete(20);

		Utente deleted = utenteDAO.getUtenteById(20);
		assertEquals(-1, deleted.getIdUtente());
	}

	/**
	 * Verifica il recupero di tutti gli utenti presenti nel database.
	 */
	@Test
	void testGetTuttiUtenti() {

		List<Utente> list = utenteDAO.getTuttiUtenti();

		assertTrue(list.size() >= 2);
		assertEquals(1, list.get(0).getIdUtente());
	}

	/**
	 * Verifica il recupero del manager di sistema.
	 */
	@Test
	void testGetManager() {

		Utente manager = utenteDAO.getManager();

		assertEquals(RuoloUtente.MANAGER, manager.getRuoloUtente());
		assertEquals(1, manager.getIdUtente());
	}
}
