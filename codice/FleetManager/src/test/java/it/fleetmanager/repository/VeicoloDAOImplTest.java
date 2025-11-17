package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

public class VeicoloDAOImplTest {

	private VeicoloDAOImpl dao;

	@BeforeEach
	void setup() throws Exception {

		DatabaseManager.setTestUrl("jdbc:h2:mem:testVeh;DB_CLOSE_DELAY=-1");

		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testVeh;DB_CLOSE_DELAY=-1");
			 Statement st = conn.createStatement()) {

			// TABELLA VEICOLO
			st.execute("""
			    CREATE TABLE IF NOT EXISTS Veicolo (
			        targa VARCHAR(10) PRIMARY KEY,
			        tipoVeicolo VARCHAR(20),
			        marca VARCHAR(50),
			        modello VARCHAR(50),
			        annoImmatricolazione INT,
			        statoVeicolo VARCHAR(20),
			        km INT
			    );
			""");

			// TABELLA PRENOTAZIONE (necessaria per getDisponibili)
			st.execute("""
			    CREATE TABLE IF NOT EXISTS Prenotazione (
			        idPrenotazione INT PRIMARY KEY,
			        dataInizio TIMESTAMP NOT NULL,
			        dataFine TIMESTAMP NOT NULL,
			        statoPrenotazione VARCHAR(20) NOT NULL,
			        tipoPrenotazione VARCHAR(20) NOT NULL,
			        idUtente INT NOT NULL,
			        targa VARCHAR(10) NOT NULL
			    );
			""");

			st.execute("DELETE FROM Prenotazione");
			st.execute("DELETE FROM Veicolo");
		}

		dao = new VeicoloDAOImpl();
	}

	@Test
	void testSaveAndGetByTarga() {
		Veicolo v = new Veicolo("AB123CD", TipoVeicolo.AUTO, "Fiat", "Panda", 2018, StatoVeicolo.DISPONIBILE, 45000);

		dao.save(v);

		Veicolo letto = dao.getVeicoloByTarga("AB123CD");

		assertEquals("AB123CD", letto.getTarga());
		assertEquals("Fiat", letto.getMarca());
		assertEquals("Panda", letto.getModello());
		assertEquals(2018, letto.getAnnoImmatricolazione());
		assertEquals(TipoVeicolo.AUTO, letto.getTipoVeicolo());
		assertEquals(StatoVeicolo.DISPONIBILE, letto.getStatoVeicolo());
	}

	@Test
	void testUpdate() {

		Veicolo v = new Veicolo("XY987ZZ", TipoVeicolo.FURGONE, "Ford", "Transit", 2020,
				StatoVeicolo.IN_MANUTENZIONE, 30000);

		dao.save(v);

		v.setKm(35000);
		v.setStatoVeicolo(StatoVeicolo.DISPONIBILE);

		dao.update(v);

		Veicolo letto = dao.getVeicoloByTarga("XY987ZZ");

		assertEquals(35000, letto.getKm());
		assertEquals(StatoVeicolo.DISPONIBILE, letto.getStatoVeicolo());
	}

	@Test
	void testDelete() {

		Veicolo v = new Veicolo("ZZ111YY", TipoVeicolo.AUTO, "BMW", "320d", 2016, StatoVeicolo.DISPONIBILE, 150000);

		dao.save(v);

		dao.delete("ZZ111YY");

		Veicolo letto = dao.getVeicoloByTarga("ZZ111YY");
		assertEquals(VeicoloDAOImpl.VEICOLO_INESISTENTE, letto);
	}

	@Test
	void testGetTuttiVeicoli() {

		dao.save(new Veicolo("T1", TipoVeicolo.AUTO, "Opel", "Corsa", 2015, StatoVeicolo.DISPONIBILE, 90000));
		dao.save(
				new Veicolo("T2", TipoVeicolo.FURGONE, "Renault", "Kangoo", 2019, StatoVeicolo.NON_DISPONIBILE, 50000));

		List<Veicolo> lista = dao.getTuttiVeicoli();

		assertEquals(2, lista.size());
	}

	@Test
	void testGetDisponibili() throws Exception {

		// Inseriamo 3 veicoli
		dao.save(new Veicolo("LIBERO1", TipoVeicolo.AUTO, "Fiat", "500", 2018, StatoVeicolo.DISPONIBILE, 30000));
		dao.save(new Veicolo("OCCUPATO", TipoVeicolo.AUTO, "Audi", "A3", 2020, StatoVeicolo.DISPONIBILE, 20000));
		dao.save(new Veicolo("LIBERO2", TipoVeicolo.FURGONE, "Ford", "Transit", 2021, StatoVeicolo.DISPONIBILE, 10000));

		// Inseriamo una prenotazione per OCCUPATO: dalle 10:00 alle 15:00
		try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testVeh;DB_CLOSE_DELAY=-1");
			 Statement st = conn.createStatement()) {

			st.execute("""
			    INSERT INTO Prenotazione
			    (idPrenotazione, dataInizio, dataFine, statoPrenotazione, tipoPrenotazione, idUtente, targa)
			    VALUES (
			        1,
			        '2025-11-20 10:00:00',
			        '2025-11-20 15:00:00',
			        'ATTIVA',
			        'UTENTE',
			        5,
			        'OCCUPATO'
			    );
			""");
		}

		// Cerchiamo veicoli liberi tra 14:00 e 16:00 → OCCUPATO è occupato → gli altri 2 sono liberi
		List<Veicolo> disponibili = dao.getDisponibili(
				LocalDateTime.of(2025, 11, 20, 14, 0),
				LocalDateTime.of(2025, 11, 20, 16, 0)
		);

		assertEquals(2, disponibili.size());
		assertTrue(disponibili.stream().anyMatch(v -> v.getTarga().equals("LIBERO1")));
		assertTrue(disponibili.stream().anyMatch(v -> v.getTarga().equals("LIBERO2")));
		assertFalse(disponibili.stream().anyMatch(v -> v.getTarga().equals("OCCUPATO")));
	}
}
