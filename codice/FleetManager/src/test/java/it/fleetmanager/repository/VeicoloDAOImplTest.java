package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

public class VeicoloDAOImplTest {

	private VeicoloDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();
		dao = new VeicoloDAOImpl();
	}

	@Test
	void testSaveAndGetByTarga() {
		Veicolo v = new Veicolo("TEST123", TipoVeicolo.AUTO, "Fiat", "Panda", 2018, StatoVeicolo.DISPONIBILE, 45000);
		dao.save(v);

		Veicolo letto = dao.getVeicoloByTarga("TEST123");

		assertEquals("TEST123", letto.getTarga());
		assertEquals("Fiat", letto.getMarca());
		assertEquals("Panda", letto.getModello());
		assertEquals(2018, letto.getAnnoImmatricolazione());
		assertEquals(TipoVeicolo.AUTO, letto.getTipoVeicolo());
		assertEquals(StatoVeicolo.DISPONIBILE, letto.getStatoVeicolo());
	}

	@Test
	void testUpdate() {
		Veicolo v = new Veicolo("UPD001", TipoVeicolo.FURGONE, "Ford", "Transit", 2020, StatoVeicolo.IN_MANUTENZIONE,
				30000);

		dao.save(v);

		v.setKm(35000);
		v.setStatoVeicolo(StatoVeicolo.DISPONIBILE);

		dao.update(v);

		Veicolo letto = dao.getVeicoloByTarga("UPD001");

		assertEquals(35000, letto.getKm());
		assertEquals(StatoVeicolo.DISPONIBILE, letto.getStatoVeicolo());
	}

	@Test
	void testDelete() {
		Veicolo v = new Veicolo("DEL999", TipoVeicolo.AUTO, "BMW", "320d", 2016, StatoVeicolo.DISPONIBILE, 150000);

		dao.save(v);
		dao.delete("DEL999");

		Veicolo letto = dao.getVeicoloByTarga("DEL999");
		assertEquals(VeicoloDAOImpl.VEICOLO_INESISTENTE, letto);
	}

	@Test
	void testGetTuttiVeicoli() {
		dao.save(new Veicolo("TUTTI1", TipoVeicolo.AUTO, "Opel", "Corsa", 2015, StatoVeicolo.DISPONIBILE, 90000));
		dao.save(new Veicolo("TUTTI2", TipoVeicolo.FURGONE, "Renault", "Kangoo", 2019, StatoVeicolo.NON_DISPONIBILE,
				50000));

		List<Veicolo> lista = dao.getTuttiVeicoli();
		assertTrue(lista.size() >= 2);
	}

	@Test
	void testGetDisponibili() throws Exception {
		dao.save(new Veicolo("LIB1", TipoVeicolo.AUTO, "Fiat", "500", 2018, StatoVeicolo.DISPONIBILE, 30000));
		dao.save(new Veicolo("OCC1", TipoVeicolo.AUTO, "Audi", "A3", 2020, StatoVeicolo.DISPONIBILE, 20000));
		dao.save(new Veicolo("LIB2", TipoVeicolo.FURGONE, "Ford", "Transit", 2021, StatoVeicolo.DISPONIBILE, 10000));

		try (Connection conn = DatabaseManager.getInstance().getConnection(); Statement st = conn.createStatement()) {

			st.execute("""
					    INSERT INTO Prenotazione
					    (idPrenotazione, dataInizio, dataFine, statoPrenotazione, tipoPrenotazione, idUtente, targa)
					    VALUES (
					        100,
					        '2025-11-20 10:00:00',
					        '2025-11-20 15:00:00',
					        'ATTIVA',
					        'UTENTE',
					        1,
					        'OCC1'
					    );
					""");
		}

		List<Veicolo> disponibili = dao.getDisponibili(LocalDateTime.of(2025, 11, 20, 14, 0),
				LocalDateTime.of(2025, 11, 20, 16, 0));

		assertTrue(disponibili.stream().anyMatch(v -> v.getTarga().equals("LIB1")));
		assertTrue(disponibili.stream().anyMatch(v -> v.getTarga().equals("LIB2")));
		assertFalse(disponibili.stream().anyMatch(v -> v.getTarga().equals("OCC1")));
	}
}
