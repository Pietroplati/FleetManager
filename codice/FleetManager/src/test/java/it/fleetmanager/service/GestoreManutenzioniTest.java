package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoManutenzione;

public class GestoreManutenzioniTest {

	private GestoreManutenzioniImpl gestore;
	private ManutenzioneDAO manutenzioneDAO;
	private VeicoloDAO veicoloDAO;
	private Veicolo veicolo;

	@BeforeEach
	void setup() throws Exception {

		DatabaseTestUtils.resetDatabase();
		manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());
		veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());

		gestore = new GestoreManutenzioniImpl(manutenzioneDAO, veicoloDAO);

		// Veicolo di test presente nel DatabaseTestUtils
		veicolo = veicoloDAO.getVeicoloByTarga("T1");
		assertNotEquals("N/A", veicolo.getTarga(), "Il veicolo T1 deve esistere nel DB di test");
	}

	@Test
	void testProgrammareManutenzione() {
		Manutenzione m = gestore.programmareManutenzione(veicolo, LocalDate.of(2025, 1, 10), TipoManutenzione.ORDINARIA,
				"Cambio olio");

		assertNotNull(m);
		assertEquals("T1", m.getTarga());
		assertEquals(TipoManutenzione.ORDINARIA, m.getTipoManutenzione());

		// Veicolo deve essere IN_MANUTENZIONE
		Veicolo aggiornato = veicoloDAO.getVeicoloByTarga("T1");
		assertEquals(StatoVeicolo.IN_MANUTENZIONE, aggiornato.getStatoVeicolo());

		// Deve esistere nel DB
		Manutenzione salvata = manutenzioneDAO.getManutenzioneById(m.getIdManutenzione());
		assertEquals("Cambio olio", salvata.getDescrizione());
	}

	@Test
	void testProgrammareManutenzioneErroreVeicoloNull() {
		assertThrows(IllegalArgumentException.class,
				() -> gestore.programmareManutenzione(null, LocalDate.now(), TipoManutenzione.ORDINARIA, "Test"));
	}

	@Test
	void testProgrammareManutenzioneErroreDataNull() {
		assertThrows(IllegalArgumentException.class,
				() -> gestore.programmareManutenzione(veicolo, null, TipoManutenzione.ORDINARIA, "Test"));
	}

	@Test
	void testProgrammareManutenzioneErroreTipoNull() {
		assertThrows(IllegalArgumentException.class,
				() -> gestore.programmareManutenzione(veicolo, LocalDate.now(), null, "Test"));
	}

	@Test
	void testProgrammareManutenzioneErroreDescrizioneVuota() {
		assertThrows(IllegalArgumentException.class,
				() -> gestore.programmareManutenzione(veicolo, LocalDate.now(), TipoManutenzione.ORDINARIA, ""));
	}

	@Test
	void testSegnalareInterventoStraordinario() {
		Manutenzione m = gestore.segnalareInterventoStraordinario(veicolo, "Freni rotti");

		assertNotNull(m);
		assertEquals(TipoManutenzione.STRAORDINARIA, m.getTipoManutenzione());
		assertEquals("Freni rotti", m.getDescrizione());

		// Veicolo deve essere IN_MANUTENZIONE
		Veicolo aggiornato = veicoloDAO.getVeicoloByTarga("T1");
		assertEquals(StatoVeicolo.IN_MANUTENZIONE, aggiornato.getStatoVeicolo());
	}

	@Test
	void testSegnalareInterventoStraordinarioErroreVeicoloNull() {
		assertThrows(IllegalArgumentException.class, () -> gestore.segnalareInterventoStraordinario(null, "Problema"));
	}

	@Test
	void testChiudiManutenzione() {
		// Prima creo una manutenzione
		Manutenzione m = gestore.programmareManutenzione(veicolo, LocalDate.now(), TipoManutenzione.ORDINARIA,
				"Tagliando");

		gestore.chiudiManutenzione(m.getIdManutenzione());

		Manutenzione aggiornata = manutenzioneDAO.getManutenzioneById(m.getIdManutenzione());
		assertTrue(aggiornata.getDescrizione().contains("(CHIUSA)"));

		// Veicolo deve tornare DISPONIBILE
		Veicolo v = veicoloDAO.getVeicoloByTarga("T1");
		assertEquals(StatoVeicolo.DISPONIBILE, v.getStatoVeicolo());
	}

	@Test
	void testChiudiManutenzioneInesistente() {
		assertThrows(IllegalArgumentException.class, () -> gestore.chiudiManutenzione(999999));
	}

	@Test
	void testGetManutenzioniVeicolo() {
		gestore.programmareManutenzione(veicolo, LocalDate.now(), TipoManutenzione.ORDINARIA, "Test manutenzione");

		List<Manutenzione> lista = gestore.getManutenzioniVeicolo(veicolo);
		assertEquals(1, lista.size());
	}

	@Test
	void testGetManutenzioniVeicoloErroreVeicoloNull() {
		assertThrows(IllegalArgumentException.class, () -> gestore.getManutenzioniVeicolo(null));
	}
}
