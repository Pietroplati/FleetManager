package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.util.TipoManutenzione;

/**
 * Test di integrazione per {@link ManutenzioneDAOImpl}.
 * <p>
 * Verifica le principali operazioni CRUD e le query di ricerca del DAO,
 * utilizzando il database H2 resettato ad ogni test.
 * </p>
 */
public class ManutenzioneDAOImplTest {

	private ManutenzioneDAO manutenzioneDAO;

	/**
	 * Inizializza il database di test e il DAO prima di ogni test.
	 *
	 * @throws Exception in caso di errori durante il reset del database
	 */
	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());
	}

	/**
	 * Verifica la corretta esecuzione di save() e getManutenzioneById().
	 */
	@Test
	void testSaveAndGetById() {

		Manutenzione m = new Manutenzione(100, LocalDateTime.of(2025, 1, 10, 9, 0), TipoManutenzione.ORDINARIA,
				"Cambio olio", "AB123CD");

		manutenzioneDAO.save(m);

		Manutenzione loaded = manutenzioneDAO.getManutenzioneById(100);

		assertEquals(100, loaded.getIdManutenzione());
		assertEquals(TipoManutenzione.ORDINARIA, loaded.getTipoManutenzione());
		assertEquals("Cambio olio", loaded.getDescrizione());
		assertEquals("AB123CD", loaded.getTarga());
	}

	/**
	 * Verifica che la ricerca per ID di una manutenzione inesistente restituisca
	 * l'oggetto sentinella.
	 */
	@Test
	void testGetManutenzioneByIdNotFound() {
	    Manutenzione m = manutenzioneDAO.getManutenzioneById(9999);
	    assertEquals(-1, m.getIdManutenzione());
	}


	/**
	 * Verifica la corretta esecuzione di update().
	 */
	@Test
	void testUpdate() {

		Manutenzione m = new Manutenzione(101, LocalDateTime.of(2025, 2, 5, 10, 0), TipoManutenzione.STRAORDINARIA,
				"Freni", "GH819RJ");

		manutenzioneDAO.save(m);

		m.setDescrizione("Freni + dischi");
		manutenzioneDAO.update(m);

		Manutenzione updated = manutenzioneDAO.getManutenzioneById(101);
		assertEquals("Freni + dischi", updated.getDescrizione());
	}

	/**
	 * Verifica la corretta esecuzione di delete().
	 */
	@Test
	void testDelete() {

		Manutenzione m = new Manutenzione(102, LocalDateTime.now(), TipoManutenzione.REVISIONE, "Revisione", "ZZ000AA");

		manutenzioneDAO.save(m);
		manutenzioneDAO.delete(102);

		Manutenzione deleted = manutenzioneDAO.getManutenzioneById(102);
		assertEquals(-1, deleted.getIdManutenzione());
	}

	/**
	 * Verifica la corretta esecuzione di findByVeicolo().
	 */
	@Test
	void testFindByVeicolo() {

		manutenzioneDAO.save(new Manutenzione(200, LocalDateTime.of(2025, 3, 1, 9, 0), TipoManutenzione.ORDINARIA,
				"Tagliando", "T1"));

		manutenzioneDAO.save(new Manutenzione(201, LocalDateTime.of(2025, 3, 5, 9, 0), TipoManutenzione.REVISIONE,
				"Revisione", "T1"));

		List<Manutenzione> list = manutenzioneDAO.findByVeicolo("T1");

		assertEquals(2, list.size());
		assertTrue(list.get(0).getData().isBefore(list.get(1).getData()));
	}

	/**
	 * Verifica la corretta esecuzione di findByTipo().
	 */
	@Test
	void testFindByTipo() {

		manutenzioneDAO
				.save(new Manutenzione(300, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "Filtro aria", "V1"));

		manutenzioneDAO.save(new Manutenzione(301, LocalDateTime.now().plusMinutes(1), TipoManutenzione.ORDINARIA,
				"Filtro olio", "V2"));

		List<Manutenzione> list = manutenzioneDAO.findByTipo(TipoManutenzione.ORDINARIA);

		assertEquals(2, list.size());
		assertTrue(list.stream().allMatch(m -> m.getTipoManutenzione() == TipoManutenzione.ORDINARIA));
	}

	/**
	 * Verifica la corretta esecuzione di getMaxId().
	 */
	@Test
	void testGetMaxId() {

		manutenzioneDAO.save(new Manutenzione(400, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "Test", "V3"));

		manutenzioneDAO.save(new Manutenzione(401, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "Test2", "V3"));

		int maxId = manutenzioneDAO.getMaxId();
		assertEquals(401, maxId);
	}

	/**
	 * Verifica la corretta esecuzione di getTutteManutenzioni().
	 */
	@Test
	void testGetTutteManutenzioni() {

		manutenzioneDAO.save(
				new Manutenzione(500, LocalDateTime.of(2025, 1, 1, 8, 0), TipoManutenzione.ORDINARIA, "M1", "AB123CD"));

		manutenzioneDAO.save(
				new Manutenzione(501, LocalDateTime.of(2025, 1, 2, 8, 0), TipoManutenzione.REVISIONE, "M2", "GH819RJ"));

		List<Manutenzione> list = manutenzioneDAO.getTutteManutenzioni();

		assertEquals(2, list.size());
		assertTrue(list.get(0).getData().isBefore(list.get(1).getData()));
	}
}
