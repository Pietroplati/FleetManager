package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImplTest {

	private NotificaDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new NotificaDAOImpl(H2DatabaseManager.getInstance());
	}

	@Test
	void testSaveAndGetById() {
		Notifica n = new Notifica(1, TipoNotifica.SCADENZA, "Revisione in scadenza", LocalDateTime.now(), false, 1, 1);

		dao.save(n);

		Notifica letta = dao.getNotificaById(1);

		assertNotNull(letta);
		assertEquals(1, letta.getIdNotifica());
		assertEquals(TipoNotifica.SCADENZA, letta.getTipoNotifica());
		assertEquals("Revisione in scadenza", letta.getMessaggio());
		assertEquals(1, letta.getIdUtente());
		assertEquals(1, letta.getIdScadenza());
	}

	@Test
	void testUpdate() {

		Notifica n = new Notifica(2, TipoNotifica.MANUTENZIONE, "Messaggio originale", LocalDateTime.now(), false, 2,
				2);

		dao.save(n);

		n.setMessaggio("Messaggio modificato");
		n.setLetta(true);

		dao.update(n);

		Notifica aggiornata = dao.getNotificaById(2);

		assertNotNull(aggiornata);
		assertEquals("Messaggio modificato", aggiornata.getMessaggio());
		assertTrue(aggiornata.getLetta());
	}

	@Test
	void testFindByUtente() {

		int idUtente = 1;

		dao.save(new Notifica(3, TipoNotifica.MANUTENZIONE, "Msg1", LocalDateTime.now().minusHours(2), false, idUtente,
				1));

		dao.save(new Notifica(4, TipoNotifica.MANUTENZIONE, "Msg2", LocalDateTime.now().minusHours(1), false, idUtente,
				2));

		List<Notifica> lista = dao.findByUtente(idUtente);

		assertEquals(2, lista.size());
		assertEquals(4, lista.get(0).getIdNotifica());
		assertEquals(3, lista.get(1).getIdNotifica());
	}

	@Test
	void testFindNonLette() {

		int idUtente = 2;

		dao.save(new Notifica(5, TipoNotifica.SCADENZA, "Letta", LocalDateTime.now(), true, idUtente, null));

		dao.save(new Notifica(6, TipoNotifica.SCADENZA, "Non letta 1", LocalDateTime.now().minusMinutes(10), false,
				idUtente, 1));

		dao.save(new Notifica(7, TipoNotifica.SCADENZA, "Non letta 2", LocalDateTime.now(), false, idUtente, 2));

		List<Notifica> lista = dao.findNonLette(idUtente);

		assertEquals(2, lista.size());
		assertEquals(6, lista.get(0).getIdNotifica()); // più vecchia prima
		assertEquals(7, lista.get(1).getIdNotifica());
	}

	@Test
	void testFindByScadenza() {

		int idScadenza = 1;

		dao.save(new Notifica(8, TipoNotifica.SCADENZA, "Vecchio msg", LocalDateTime.now().minusHours(3), false, 1,
				idScadenza));

		dao.save(new Notifica(9, TipoNotifica.SCADENZA, "Ultimo msg", LocalDateTime.now().minusHours(1), false, 1,
				idScadenza));

		List<Notifica> lista = dao.findByScadenza(idScadenza);

		assertEquals(2, lista.size());
		assertEquals(9, lista.get(0).getIdNotifica());
		assertEquals("Ultimo msg", lista.get(0).getMessaggio());
	}

	@Test
	void testDelete() {

		dao.save(new Notifica(10, TipoNotifica.SCADENZA, "Da eliminare", LocalDateTime.now(), false, 1, 1));

		dao.delete(10);

		Notifica result = dao.getNotificaById(10);

		assertEquals(NotificaDAOImpl.NOTIFICA_INESISTENTE.toString(), result.toString());
	}
}
