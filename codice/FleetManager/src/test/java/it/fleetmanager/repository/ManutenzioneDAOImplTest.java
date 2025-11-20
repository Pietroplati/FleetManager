package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.TipoManutenzione;

public class ManutenzioneDAOImplTest {

	private ManutenzioneDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new ManutenzioneDAOImpl();
	}

	@Test
	void testSaveAndGetById() {

		Manutenzione m = new Manutenzione(1, LocalDateTime.of(2025, 5, 10, 10, 0), TipoManutenzione.ORDINARIA,
				"Cambio olio", "AB123CD");

		dao.save(m);

		Manutenzione letto = dao.getManutenzioneById(1);

		assertEquals(1, letto.getIdManutenzione());
		assertEquals(LocalDateTime.of(2025, 5, 10, 10, 0), letto.getData());
		assertEquals(TipoManutenzione.ORDINARIA, letto.getTipoManutenzione());
		assertEquals("Cambio olio", letto.getDescrizione());
		assertEquals("AB123CD", letto.getTarga());
	}

	@Test
	void testUpdate() {

		Manutenzione m = new Manutenzione(2, LocalDateTime.of(2025, 6, 12, 14, 0), TipoManutenzione.ORDINARIA,
				"Tagliando completo", "GH819RJ");

		dao.save(m);

		m.setDescrizione("Tagliando + sostituzione filtri");
		m.setData(LocalDateTime.of(2025, 6, 12, 16, 0));

		dao.update(m);

		Manutenzione letto = dao.getManutenzioneById(2);

		assertEquals("Tagliando + sostituzione filtri", letto.getDescrizione());
		assertEquals(LocalDateTime.of(2025, 6, 12, 16, 0), letto.getData());
	}

	@Test
	void testDelete() {

		Manutenzione m = new Manutenzione(3, LocalDateTime.of(2025, 3, 15, 9, 0), TipoManutenzione.REVISIONE,
				"Revisione annuale", "ZZ000AA");

		dao.save(m);

		dao.delete(3);

		Manutenzione letto = dao.getManutenzioneById(3);
		assertEquals(ManutenzioneDAOImpl.MANUTENZIONE_INESISTENTE, letto);
	}

	@Test
	void testFindByVeicolo() {

		dao.save(new Manutenzione(10, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "A", "T1"));
		dao.save(new Manutenzione(11, LocalDateTime.now(), TipoManutenzione.STRAORDINARIA, "B", "T1"));
		dao.save(new Manutenzione(12, LocalDateTime.now(), TipoManutenzione.REVISIONE, "C", "T2"));

		List<Manutenzione> lista = dao.findByVeicolo("T1");

		assertEquals(2, lista.size());
		assertTrue(lista.stream().allMatch(m -> m.getTarga().equals("T1")));
	}

	@Test
	void testFindByTipo() {

		dao.save(new Manutenzione(20, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "X", "V1"));
		dao.save(new Manutenzione(21, LocalDateTime.now(), TipoManutenzione.ORDINARIA, "Y", "V2"));
		dao.save(new Manutenzione(22, LocalDateTime.now(), TipoManutenzione.STRAORDINARIA, "Z", "V3"));

		List<Manutenzione> lista = dao.findByTipo(TipoManutenzione.ORDINARIA);

		assertEquals(2, lista.size());
		assertTrue(lista.stream().allMatch(m -> m.getTipoManutenzione() == TipoManutenzione.ORDINARIA));
	}
}
