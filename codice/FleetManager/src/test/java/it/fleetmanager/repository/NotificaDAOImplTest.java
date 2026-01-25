package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.util.TipoNotifica;

/**
 * Test di integrazione per {@link NotificaDAOImpl}.
 * <p>
 * Verifica le principali operazioni CRUD e i metodi di query del DAO,
 * utilizzando il database H2 resettato ad ogni test.
 * </p>
 */
public class NotificaDAOImplTest {

	private NotificaDAO notificaDAO;

	/**
	 * Inizializza il database di test e il DAO prima di ogni test.
	 *
	 * @throws Exception in caso di errori durante il reset del database
	 */
	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
	}

	/**
	 * Verifica la corretta esecuzione di save() e il recupero della notifica
	 * tramite findByUtente().
	 */
	@Test
	void testSaveAndGetById() {

		Notifica n = new Notifica(null, TipoNotifica.PRENOTAZIONE, "Prenotazione confermata",
				LocalDateTime.of(2025, 1, 10, 9, 30), false, 2, null);

		notificaDAO.save(n);

		List<Notifica> list = notificaDAO.findByUtente(2);
		assertEquals(1, list.size());

		Notifica loaded = list.get(0);

		assertTrue(loaded.getIdNotifica() >= 0);
		assertEquals(TipoNotifica.PRENOTAZIONE, loaded.getTipoNotifica());
		assertEquals("Prenotazione confermata", loaded.getMessaggio());
		assertFalse(loaded.getLetta());
		assertEquals(2, loaded.getIdUtente());
		assertNull(loaded.getIdScadenza());
	}

	/**
	 * Verifica che la ricerca per ID di una notifica inesistente restituisca
	 * l'oggetto sentinella.
	 */
	@Test
	void testGetNotificaByIdNotFound() {
		Notifica n = notificaDAO.getNotificaById(9999);
		assertEquals(-1, n.getIdNotifica());
	}

	/**
	 * Verifica la corretta esecuzione di update().
	 */
	@Test
	void testUpdate() {

		Notifica n = new Notifica(null, TipoNotifica.MANUTENZIONE, "Manutenzione programmata", LocalDateTime.now(),
				false, 1, null);

		notificaDAO.save(n);

		Notifica saved = notificaDAO.findByUtente(1).get(0);
		saved.setLetta(true);
		saved.setMessaggio("Manutenzione completata");

		notificaDAO.update(saved);

		Notifica updated = notificaDAO.getNotificaById(saved.getIdNotifica());

		assertTrue(updated.getLetta());
		assertEquals("Manutenzione completata", updated.getMessaggio());
	}

	/**
	 * Verifica la corretta esecuzione di delete().
	 */
	@Test
	void testDelete() {

		Notifica n = new Notifica(null, TipoNotifica.SEGNALAZIONE, "Segnalazione veicolo", LocalDateTime.now(), false,
				1, null);

		notificaDAO.save(n);

		Notifica saved = notificaDAO.findByUtente(1).get(0);
		notificaDAO.delete(saved.getIdNotifica());

		Notifica deleted = notificaDAO.getNotificaById(saved.getIdNotifica());
		assertEquals(-1, deleted.getIdNotifica());
	}

	/**
	 * Verifica la corretta esecuzione di findByUtente() e l'ordinamento per data
	 * invio.
	 */
	@Test
	void testFindByUtente() {

		notificaDAO.save(new Notifica(null, TipoNotifica.PRENOTAZIONE, "N1", LocalDateTime.of(2025, 1, 1, 9, 0), false,
				2, null));

		notificaDAO.save(new Notifica(null, TipoNotifica.PRENOTAZIONE, "N2", LocalDateTime.of(2025, 1, 2, 9, 0), false,
				2, null));

		List<Notifica> list = notificaDAO.findByUtente(2);

		assertEquals(2, list.size());
		assertTrue(list.get(0).getDataInvio().isAfter(list.get(1).getDataInvio()));
	}

	/**
	 * Verifica la corretta esecuzione di findNonLette().
	 */
	@Test
	void testFindNonLette() {

		notificaDAO
				.save(new Notifica(null, TipoNotifica.SCADENZA, "Scadenza bollo", LocalDateTime.now(), false, 1, null));

		notificaDAO.save(new Notifica(null, TipoNotifica.SCADENZA, "Scadenza assicurazione",
				LocalDateTime.now().plusMinutes(1), true, 1, null));

		List<Notifica> list = notificaDAO.findNonLette(1);

		assertEquals(1, list.size());
		assertFalse(list.get(0).getLetta());
	}

	/**
	 * Verifica che findByScadenza(null) restituisca una lista vuota.
	 */
	@Test
	void testFindByScadenzaNull() {
		List<Notifica> list = notificaDAO.findByScadenza(null);
		assertTrue(list.isEmpty());
	}

}
