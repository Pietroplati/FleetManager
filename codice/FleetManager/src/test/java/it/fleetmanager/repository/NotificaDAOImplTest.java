package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;

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

	// TEST SAVE + GET BY ID
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
	}

	// TEST UPDATE
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
		assertTrue(aggiornata.getLetta()); // ✔ FIX
	}

	// TEST FIND BY UTENTE
	@Test
	void testFindByUtente() {

		dao.save(new Notifica(3, TipoNotifica.MANUTENZIONE, "Msg1", LocalDateTime.now().minusHours(2), false, 50, 0));

		dao.save(new Notifica(4, TipoNotifica.MANUTENZIONE, "Msg2", LocalDateTime.now().minusHours(1), false, 50, 0));

		Notifica result = dao.findByUtente(50);

		assertNotNull(result);
		assertEquals(4, result.getIdNotifica()); // la più recente
	}

	// TEST FIND NON LETTE
	@Test
	void testFindNonLette() {

		dao.save(new Notifica(5, TipoNotifica.SCADENZA, "Letta", LocalDateTime.now(), true, 99, 0));

		dao.save(new Notifica(6, TipoNotifica.SCADENZA, "Non letta", LocalDateTime.now(), false, 99, 0));

		Notifica n = dao.findNonLette(99);

		assertNotNull(n);
		assertEquals(6, n.getIdNotifica());
		assertFalse(n.getLetta()); // ✔ FIX
	}

	// TEST FIND BY SCADENZA
	@Test
	void testFindByScadenza() {

		dao.save(
				new Notifica(7, TipoNotifica.SCADENZA, "Vecchio msg", LocalDateTime.now().minusHours(3), false, 1, 10));

		dao.save(new Notifica(8, TipoNotifica.SCADENZA, "Ultimo msg", LocalDateTime.now().minusHours(1), false, 1, 10));

		Notifica result = dao.findByScadenza(10);

		assertEquals(8, result.getIdNotifica());
		assertEquals("Ultimo msg", result.getMessaggio());
	}

	// TEST DELETE
	@Test
	void testDelete() {

		dao.save(new Notifica(9, TipoNotifica.SCADENZA, "Da eliminare", LocalDateTime.now(), false, 5, 0));

		dao.delete(9);

		Notifica result = dao.getNotificaById(9);
		assertEquals(NotificaDAOImpl.NOTIFICA_INESISTENTE, result);
	}
}
