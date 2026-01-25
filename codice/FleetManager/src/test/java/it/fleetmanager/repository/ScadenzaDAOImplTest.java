package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.util.TipoScadenza;

/**
 * Test di integrazione per {@link ScadenzaDAOImpl}.
 * <p>
 * Verifica le principali operazioni CRUD e i metodi di query del DAO,
 * utilizzando il database H2 resettato ad ogni test.
 * </p>
 */
public class ScadenzaDAOImplTest {

	private ScadenzaDAO scadenzaDAO;

	/**
	 * Inizializza il database di test e il DAO prima di ogni test.
	 *
	 * @throws Exception in caso di errori durante il reset del database
	 */
	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		scadenzaDAO = new ScadenzaDAOImpl(H2DatabaseManager.getInstance());
	}

	/**
	 * Verifica la corretta esecuzione di save() e getScadenzaById().
	 */
	@Test
	void testSaveAndGetById() {
		Scadenza s = new Scadenza(10, TipoScadenza.BOLLO, LocalDate.of(2025, 1, 10), false, "AB123CD");

		scadenzaDAO.save(s);

		Scadenza loaded = scadenzaDAO.getScadenzaById(10);

		assertEquals(10, loaded.getIdScadenza());
		assertEquals(TipoScadenza.BOLLO, loaded.getTipoScadenza());
		assertEquals(LocalDate.of(2025, 1, 10), loaded.getData());
		assertFalse(loaded.getNotificata());
		assertEquals("AB123CD", loaded.getTarga());
	}

	/**
	 * Verifica che la ricerca per ID inesistente restituisca l'oggetto sentinella.
	 */
	@Test
	void testGetScadenzaByIdNotFound() {
		Scadenza s = scadenzaDAO.getScadenzaById(999);
		assertEquals(-1, s.getIdScadenza());
	}

	/**
	 * Verifica la corretta esecuzione di update().
	 */
	@Test
	void testUpdate() {
		Scadenza s = new Scadenza(20, TipoScadenza.ASSICURAZIONE, LocalDate.of(2025, 3, 15), false, "T1");

		scadenzaDAO.save(s);

		s.setNotificata(true);
		scadenzaDAO.update(s);

		Scadenza updated = scadenzaDAO.getScadenzaById(20);
		assertTrue(updated.getNotificata());
	}

	/**
	 * Verifica la corretta esecuzione di delete().
	 */
	@Test
	void testDelete() {
		Scadenza s = new Scadenza(30, TipoScadenza.REVISIONE, LocalDate.of(2025, 6, 20), false, "V1");

		scadenzaDAO.save(s);
		scadenzaDAO.delete(30);

		Scadenza deleted = scadenzaDAO.getScadenzaById(30);
		assertEquals(-1, deleted.getIdScadenza());
	}

	/**
	 * Verifica la corretta esecuzione di findByVeicolo().
	 */
	@Test
	void testFindByVeicolo() {
		Scadenza s1 = new Scadenza(40, TipoScadenza.BOLLO, LocalDate.of(2025, 4, 10), false, "T2");

		Scadenza s2 = new Scadenza(41, TipoScadenza.TAGLIANDO, LocalDate.of(2025, 5, 10), false, "T2");

		scadenzaDAO.save(s1);
		scadenzaDAO.save(s2);

		List<Scadenza> list = scadenzaDAO.findByVeicolo("T2");

		assertTrue(list.size() >= 2);
		assertTrue(list.stream().allMatch(s -> s.getTarga().equals("T2")));
	}

	/**
	 * Verifica la corretta esecuzione di findProssimeScadenze().
	 */
	@Test
	void testFindProssimeScadenze() {
		Scadenza s = new Scadenza(50, TipoScadenza.BOLLO, LocalDate.now().plusDays(5), false, "V2");

		scadenzaDAO.save(s);

		List<Scadenza> list = scadenzaDAO.findProssimeScadenze(LocalDate.now().plusDays(10));

		assertTrue(list.stream().anyMatch(sc -> sc.getIdScadenza() == 50 && sc.getTarga().equals("V2")));
	}

	/**
	 * Verifica la corretta esecuzione di getTutteScadenze() e l'incremento del
	 * numero di record.
	 */
	@Test
	void testGetTutteScadenze() {
		int initialSize = scadenzaDAO.getTutteScadenze().size();

		scadenzaDAO.save(new Scadenza(60, TipoScadenza.TAGLIANDO, LocalDate.of(2025, 7, 1), false, "AB123CD"));

		scadenzaDAO.save(new Scadenza(61, TipoScadenza.BOLLO, LocalDate.of(2025, 8, 1), false, "GH819RJ"));

		List<Scadenza> list = scadenzaDAO.getTutteScadenze();

		assertEquals(initialSize + 2, list.size());
		assertTrue(list.get(0).getData().isBefore(list.get(list.size() - 1).getData()));
	}
}
