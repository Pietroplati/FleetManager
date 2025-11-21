package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.TipoScadenza;

public class ScadenzaDAOImplTest {

	private ScadenzaDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new ScadenzaDAOImpl();
	}

	@Test
	void testSaveAndGetById() {
		Scadenza s = new Scadenza(10, TipoScadenza.BOLLO, LocalDate.of(2025, 12, 31), false, "AB123CD");

		dao.save(s);
		Scadenza letta = dao.getScadenzaById(10);

		assertEquals(10, letta.getIdScadenza());
		assertEquals(TipoScadenza.BOLLO, letta.getTipoScadenza());
		assertEquals(LocalDate.of(2025, 12, 31), letta.getData());
		assertFalse(letta.getNotificata());
		assertEquals("AB123CD", letta.getTarga());
	}

	@Test
	void testUpdate() {
		Scadenza s = new Scadenza(20, TipoScadenza.REVISIONE, LocalDate.of(2025, 10, 10), false, "GH819RJ");

		dao.save(s);

		s.setData(LocalDate.of(2025, 11, 11));
		s.setNotificata(true);
		dao.update(s);

		Scadenza letta = dao.getScadenzaById(20);

		assertEquals(LocalDate.of(2025, 11, 11), letta.getData());
		assertTrue(letta.getNotificata());
	}

	@Test
	void testDelete() {
		Scadenza s = new Scadenza(30, TipoScadenza.ASSICURAZIONE, LocalDate.of(2025, 5, 20), false, "T1");

		dao.save(s);
		dao.delete(30);

		Scadenza letta = dao.getScadenzaById(30);
		assertEquals(ScadenzaDAOImpl.SCADENZA_INESISTENTE, letta);
	}

	@Test
	void testFindByVeicolo() {
		dao.save(new Scadenza(40, TipoScadenza.BOLLO, LocalDate.now(), false, "V2"));
		dao.save(new Scadenza(41, TipoScadenza.REVISIONE, LocalDate.now().plusDays(10), false, "V2"));
		dao.save(new Scadenza(42, TipoScadenza.TAGLIANDO, LocalDate.now().plusDays(20), false, "V3"));

		List<Scadenza> lista = dao.findByVeicolo("V2");

		assertEquals(2, lista.size());
		assertTrue(lista.stream().allMatch(s -> s.getTarga().equals("V2")));
	}

	@Test
	void testFindProssimeScadenze() {
		LocalDate oggi = LocalDate.now();

		dao.save(new Scadenza(50, TipoScadenza.BOLLO, oggi.minusDays(5), false, "T2"));
		dao.save(new Scadenza(51, TipoScadenza.BOLLO, oggi.plusDays(2), false, "T2"));
		dao.save(new Scadenza(52, TipoScadenza.REVISIONE, oggi.plusDays(10), false, "V3"));
		dao.save(new Scadenza(53, TipoScadenza.TAGLIANDO, oggi.plusDays(40), false, "AB123CD"));

		LocalDate finoA = oggi.plusDays(15);

		List<Scadenza> lista = dao.findProssimeScadenze(finoA);

		assertEquals(2, lista.size());
		assertTrue(lista.stream().anyMatch(s -> s.getIdScadenza() == 51));
		assertTrue(lista.stream().anyMatch(s -> s.getIdScadenza() == 52));
	}
}
