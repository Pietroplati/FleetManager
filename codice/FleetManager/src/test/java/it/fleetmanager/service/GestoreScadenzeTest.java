package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.H2DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.repository.ScadenzaDAO;
import it.fleetmanager.repository.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoScadenza;
import it.fleetmanager.util.TipoVeicolo;

public class GestoreScadenzeTest {

	private ScadenzaDAO scadenzaDAO;
	private NotificaDAO notificaDAO;
	private SistemaNotifiche sistemaNotifiche;
	private GestoreScadenze gestore;

	private VeicoloDAO veicoloDAO;
	private Veicolo veicolo;

	@BeforeEach
	void setup() throws Exception {

		// DB in-memory identico a quello reale
		DatabaseTestUtils.resetDatabase();

		// DAO reali
		scadenzaDAO = new ScadenzaDAOImpl(H2DatabaseManager.getInstance());
		notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
		sistemaNotifiche = new SistemaNotifiche(notificaDAO);

		// VeicoloDAO fake usato solo in RAM per aggiornare stato veicolo
		veicoloDAO = new VeicoloDAO() {

			Veicolo internal;

			@Override
			public void save(Veicolo v) {
				internal = v;
			}

			@Override
			public void update(Veicolo v) {
				internal = v;
			}

			@Override
			public Veicolo getVeicoloByTarga(String t) {
				return internal;
			}

			@Override
			public void delete(String t) {
			}

			@Override
			public List<Veicolo> getTuttiVeicoli() {
				return List.of();
			}

			@Override
			public List<Veicolo> getDisponibili(java.time.LocalDateTime a, java.time.LocalDateTime b) {
				return List.of();
			}
		};

		gestore = new GestoreScadenze(scadenzaDAO, veicoloDAO, sistemaNotifiche);

		// Veicolo già presente anche nel DB reale, ma usiamo quello in memoria per i
		// test
		veicolo = new Veicolo("AB123CD", TipoVeicolo.AUTO, "Fiat", "Panda", 2018, StatoVeicolo.DISPONIBILE, 10000);

		veicoloDAO.save(veicolo);
	}

	@Test
	void testControllaScadenzeEntroNessuna() {

		Scadenza result = gestore.controllaScadenzeEntro(LocalDate.now().minusDays(5));

		assertEquals(-1, result.getIdScadenza());
	}

	@Test
	void testControllaScadenzeEntroSuccess() {

		Scadenza s = new Scadenza(50, TipoScadenza.BOLLO, LocalDate.now().plusDays(3), false, "AB123CD");

		scadenzaDAO.save(s);

		Scadenza result = gestore.controllaScadenzeEntro(LocalDate.now().plusDays(5));

		assertEquals(50, result.getIdScadenza());
	}

	@Test
	void testMarcaComeNotificataSuccess() {

		Scadenza s = new Scadenza(70, TipoScadenza.REVISIONE, LocalDate.now().plusDays(10), false, "AB123CD");

		scadenzaDAO.save(s);

		gestore.marcaComeNotificata(70);

		Scadenza updated = scadenzaDAO.getScadenzaById(70);

		assertTrue(updated.getNotificata());
	}

	@Test
	void testMarcaComeNotificataInesistente() {

		assertThrows(IllegalArgumentException.class, () -> gestore.marcaComeNotificata(9999));
	}

	@Test
	void testBloccaVeicoloSeScaduta() {

		Scadenza s = new Scadenza(80, TipoScadenza.ASSICURAZIONE, LocalDate.now().minusDays(1), false, "AB123CD");

		scadenzaDAO.save(s);

		gestore.bloccaVeicoloSeScaduta(veicolo);

		assertEquals(StatoVeicolo.NON_DISPONIBILE, veicoloDAO.getVeicoloByTarga("AB123CD").getStatoVeicolo());
	}

	@Test
	void testEseguiControlloPeriodico() {

		Scadenza s = new Scadenza(90, TipoScadenza.TAGLIANDO, LocalDate.now().plusDays(4), false, "AB123CD");

		scadenzaDAO.save(s);

		gestore.eseguiControlloPeriodico();

		Scadenza updated = scadenzaDAO.getScadenzaById(90);

		assertTrue(updated.getNotificata());

		List<Notifica> notifiche = notificaDAO.findByScadenza(90);
		assertEquals(1, notifiche.size());
	}
}
