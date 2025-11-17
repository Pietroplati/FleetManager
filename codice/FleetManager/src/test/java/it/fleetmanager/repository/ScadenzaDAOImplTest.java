package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.util.TipoScadenza;

public class ScadenzaDAOImplTest {

	private ScadenzaDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {

		DatabaseManager.setTestUrl("jdbc:h2:mem:testScad;DB_CLOSE_DELAY=-1");

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testScad;DB_CLOSE_DELAY=-1");
				Statement st = conn.createStatement()) {

			st.execute("""
					    CREATE TABLE IF NOT EXISTS Scadenza (
					        idScadenza INT PRIMARY KEY,
					        tipoScadenza VARCHAR(20) NOT NULL,
					        data DATE NOT NULL,
					        notificata BOOLEAN NOT NULL,
					        targa VARCHAR(10) NOT NULL
					    );
					""");

			st.execute("DELETE FROM Scadenza");
		}

		dao = new ScadenzaDAOImpl();
	}

	@Test
	void testSaveAndGetById() {

		Scadenza s = new Scadenza(1, TipoScadenza.BOLLO, LocalDate.of(2025, 12, 31), false, "AB123CD");

		dao.save(s);

		Scadenza letta = dao.getScadenzaById(1);

		assertEquals(1, letta.getIdScadenza());
		assertEquals(TipoScadenza.BOLLO, letta.getTipoScadenza());
		assertEquals(LocalDate.of(2025, 12, 31), letta.getData());
		assertFalse(letta.getNotificata());
		assertEquals("AB123CD", letta.getTarga());
	}

	@Test
	void testUpdate() {

		Scadenza s = new Scadenza(2, TipoScadenza.REVISIONE, LocalDate.of(2025, 10, 10), false, "XY987ZZ");

		dao.save(s);

		s.setData(LocalDate.of(2025, 11, 11));
		s.setNotificata(true);

		dao.update(s);

		Scadenza letta = dao.getScadenzaById(2);

		assertEquals(LocalDate.of(2025, 11, 11), letta.getData());
		assertTrue(letta.getNotificata());
	}

	@Test
	void testDelete() {

		Scadenza s = new Scadenza(3, TipoScadenza.ASSICURAZIONE, LocalDate.of(2025, 5, 20), false, "ZZ111YY");

		dao.save(s);

		dao.delete(3);

		Scadenza letta = dao.getScadenzaById(3);

		assertEquals(ScadenzaDAOImpl.SCADENZA_INESISTENTE, letta);
	}

	@Test
	void testFindByVeicolo() {

		dao.save(new Scadenza(4, TipoScadenza.BOLLO, LocalDate.now(), false, "VEH001"));
		dao.save(new Scadenza(5, TipoScadenza.REVISIONE, LocalDate.now().plusDays(10), false, "VEH001"));
		dao.save(new Scadenza(6, TipoScadenza.TAGLIANDO, LocalDate.now().plusDays(20), false, "OTHER"));

		List<Scadenza> lista = dao.findByVeicolo("VEH001");

		assertEquals(2, lista.size());
		assertTrue(lista.stream().allMatch(s -> s.getTarga().equals("VEH001")));
	}

	@Test
	void testFindProssimeScadenze() {

		LocalDate oggi = LocalDate.now();

		dao.save(new Scadenza(7, TipoScadenza.BOLLO, oggi.minusDays(5), false, "CAR1")); // NON deve comparire
		dao.save(new Scadenza(8, TipoScadenza.BOLLO, oggi.plusDays(2), false, "CAR1")); // SÌ
		dao.save(new Scadenza(9, TipoScadenza.REVISIONE, oggi.plusDays(10), false, "CAR2")); // SÌ
		dao.save(new Scadenza(10, TipoScadenza.TAGLIANDO, oggi.plusDays(40), false, "CAR3")); // NO (oltre finoA)

		LocalDate finoA = oggi.plusDays(15);

		List<Scadenza> lista = dao.findProssimeScadenze(finoA);

		assertEquals(2, lista.size());
		assertTrue(lista.stream().anyMatch(s -> s.getIdScadenza() == 8));
		assertTrue(lista.stream().anyMatch(s -> s.getIdScadenza() == 9));
	}
}
