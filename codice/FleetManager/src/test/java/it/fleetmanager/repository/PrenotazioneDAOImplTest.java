package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class PrenotazioneDAOImplTest {

	private PrenotazioneDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());
	}

	@Test
	void testSaveAndGetById() {

		Prenotazione p = new Prenotazione(1, LocalDateTime.of(2025, 11, 10, 8, 0),
				LocalDateTime.of(2025, 11, 10, 18, 0), StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE, 1,
				"AB123CD");

		dao.save(p);

		Prenotazione letta = dao.getById(1);

		assertEquals(1, letta.getIdPrenotazione());
		assertEquals(StatoPrenotazione.CONFERMATA, letta.getStato());
		assertEquals(TipoPrenotazione.UTENTE, letta.getTipoPrenotazione());
		assertEquals("AB123CD", letta.getTarga());
	}

	@Test
	void testUpdate() {

		Prenotazione p = new Prenotazione(2, LocalDateTime.of(2025, 11, 11, 8, 0),
				LocalDateTime.of(2025, 11, 11, 17, 0), StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, 2,
				"GH819RJ");

		dao.save(p);

		p.setStato(StatoPrenotazione.ATTIVA);
		p.setDataFine(LocalDateTime.of(2025, 11, 11, 19, 0));

		dao.update(p);

		Prenotazione letta = dao.getById(2);

		assertEquals(StatoPrenotazione.ATTIVA, letta.getStato());
		assertEquals(LocalDateTime.of(2025, 11, 11, 19, 0), letta.getDataFine());
	}

	@Test
	void testDelete() {

		Prenotazione p = new Prenotazione(3, LocalDateTime.of(2025, 11, 12, 9, 0),
				LocalDateTime.of(2025, 11, 12, 12, 0), StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE, 1, "T1");

		dao.save(p);

		dao.delete(3);

		Prenotazione letta = dao.getById(3);
		assertEquals(PrenotazioneDAOImpl.PRENOTAZIONE_INESISTENTE, letta);
	}

	@Test
	void testFindByDriver() {

		dao.save(new Prenotazione(4, LocalDateTime.now(), LocalDateTime.now().plusHours(2), StatoPrenotazione.ATTIVA,
				TipoPrenotazione.UTENTE, 2, "V1"));

		dao.save(new Prenotazione(5, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
				StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE, 2, "V2"));

		List<Prenotazione> lista = dao.findByDriver(2);
		assertEquals(2, lista.size());
	}

	@Test
	void testFindByVeicolo() {

		dao.save(new Prenotazione(6, LocalDateTime.now(), LocalDateTime.now().plusHours(3),
				StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE, 1, "V3"));

		List<Prenotazione> lista = dao.findByVeicolo("V3");

		assertEquals(1, lista.size());
		assertEquals("V3", lista.get(0).getTarga());
	}

	@Test
	void testExistsOverlapping() {

		dao.save(new Prenotazione(7, LocalDateTime.of(2025, 11, 20, 10, 0), LocalDateTime.of(2025, 11, 20, 15, 0),
				StatoPrenotazione.ATTIVA, TipoPrenotazione.UTENTE, 1, "ZZ000AA"));

		boolean res = dao.existsOverlapping("ZZ000AA", LocalDateTime.of(2025, 11, 20, 14, 0),
				LocalDateTime.of(2025, 11, 20, 16, 0));

		assertTrue(res);

		boolean res2 = dao.existsOverlapping("ZZ000AA", LocalDateTime.of(2025, 11, 20, 15, 0),
				LocalDateTime.of(2025, 11, 20, 18, 0));

		assertFalse(res2);
	}
}
