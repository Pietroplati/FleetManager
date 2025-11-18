package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImplTest {

	private NotificaDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {

		// Usa un H2 in-memory
		DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");

		// Creazione tabella Notifica
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
				Statement st = conn.createStatement()) {

			st.execute("""
					    CREATE TABLE IF NOT EXISTS Notifica (
					        idNotifica INT PRIMARY KEY,
					        tipoNotifica VARCHAR(50),
					        messaggio VARCHAR(255),
					        dataInvio TIMESTAMP,
					        letta BOOLEAN,
					        idUtente INT,
					        idScadenza INT
					    );
					""");

			st.execute("DELETE FROM Notifica"); // reset tabella
		}

		dao = new NotificaDAOImpl();
	}

	// ============================================================
	// TEST SAVE + GET BY ID
	// ============================================================
	@Test
	void testSaveAndGetById() {

		Notifica n = new Notifica(1, TipoNotifica.SCADENZA, "Revisione in scadenza", LocalDateTime.now(), false, 10,
				100);

		dao.save(n);

		Notifica letta = dao.getNotificaById(1);

		assertNotNull(letta);
		assertEquals(1, letta.getIdNotifica());
		assertEquals(TipoNotifica.SCADENZA, letta.getTipoNotifica());
		assertEquals(10, letta.getIdUtente());
		assertEquals(100, letta.getIdScadenza());
	}

	// ============================================================
	// TEST UPDATE
	// ============================================================
	@Test
	void testUpdate() {

		Notifica n = new Notifica(2, TipoNotifica.MANUTENZIONE, "Messaggio originale", LocalDateTime.now(), false, 20,
				200);

		dao.save(n);

		n.setMessaggio("Messaggio modificato");
		n.setLetta(true);

		dao.update(n);

		Notifica aggiornata = dao.getNotificaById(2);

		assertEquals("Messaggio modificato", aggiornata.getMessaggio());
		assertTrue(aggiornata.getLetta());
	}

	// ============================================================
	// TEST FIND BY UTENTE → LISTA
	// ============================================================
	@Test
	void testFindByUtente() {

		Notifica n1 = new Notifica(3, TipoNotifica.MANUTENZIONE, "Msg1", LocalDateTime.now().minusHours(2), false, 50,
				0);

		Notifica n2 = new Notifica(4, TipoNotifica.MANUTENZIONE, "Msg2", LocalDateTime.now().minusHours(1), false, 50,
				0);

		dao.save(n1);
		dao.save(n2);

		List<Notifica> lista = dao.findByUtente(50);

		assertEquals(2, lista.size());
		assertEquals(4, lista.get(0).getIdNotifica()); // prima la più recente
		assertEquals(3, lista.get(1).getIdNotifica());
	}

	// ============================================================
	// TEST FIND NON LETTE → LISTA
	// ============================================================
	@Test
	void testFindNonLette() {

		dao.save(new Notifica(5, TipoNotifica.SCADENZA, "Letta", LocalDateTime.now(), true, 99, 0));

		dao.save(new Notifica(6, TipoNotifica.SCADENZA, "Non letta 1", LocalDateTime.now().minusMinutes(10), false, 99,
				0));

		dao.save(new Notifica(7, TipoNotifica.SCADENZA, "Non letta 2", LocalDateTime.now(), false, 99, 0));

		List<Notifica> lista = dao.findNonLette(99);

		assertEquals(2, lista.size());
		assertEquals(6, lista.get(0).getIdNotifica()); // ordinamento ASC
		assertEquals(7, lista.get(1).getIdNotifica());
	}

	// ============================================================
	// TEST FIND BY SCADENZA → LISTA
	// ============================================================
	@Test
	void testFindByScadenza() {

		dao.save(
				new Notifica(8, TipoNotifica.SCADENZA, "Vecchio msg", LocalDateTime.now().minusHours(3), false, 1, 10));

		dao.save(new Notifica(9, TipoNotifica.SCADENZA, "Ultimo msg", LocalDateTime.now().minusHours(1), false, 1, 10));

		List<Notifica> lista = dao.findByScadenza(10);

		assertEquals(2, lista.size());
		assertEquals(9, lista.get(0).getIdNotifica());
		assertEquals("Ultimo msg", lista.get(0).getMessaggio());
	}

	// ============================================================
	// TEST DELETE
	// ============================================================
	@Test
	void testDelete() {

		dao.save(new Notifica(10, TipoNotifica.SCADENZA, "Da eliminare", LocalDateTime.now(), false, 5, 0));

		dao.delete(10);

		Notifica result = dao.getNotificaById(10);

		assertEquals(NotificaDAOImpl.NOTIFICA_INESISTENTE.toString(), result.toString());
	}
}
