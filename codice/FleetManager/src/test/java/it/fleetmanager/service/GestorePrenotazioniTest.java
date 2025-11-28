package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.H2DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.repository.PrenotazioneDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoNotifica;
import it.fleetmanager.util.TipoPrenotazione;
import it.fleetmanager.util.TipoVeicolo;
import it.fleetmanager.util.StatoVeicolo;

public class GestorePrenotazioniTest {

	private PrenotazioneDAO prenotazioneDAO;
	private NotificaDAO notificaDAO;
	private SistemaNotifiche sistemaNotifiche;
	private GestorePrenotazioni gestore;

	private Utente driver;
	private Utente manager;
	private Veicolo veicolo;

	@BeforeEach
	void setup() throws Exception {
		DatabaseTestUtils.resetDatabase();

		prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());
		notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
		sistemaNotifiche = new SistemaNotifiche(notificaDAO);

		gestore = new GestorePrenotazioni(prenotazioneDAO, null, sistemaNotifiche);

		driver = new Utente(2, "Luca", "Verdi", "driver@example.com", "pwd", RuoloUtente.DRIVER, "B");
		manager = new Utente(1, "Mario", "Rossi", "manager@example.com", "pwd", RuoloUtente.MANAGER);

		veicolo = new Veicolo("T1", TipoVeicolo.AUTO, "Audi", "A1", 2021, StatoVeicolo.DISPONIBILE, 15000);
	}

	@Test
	void testCreaPrenotazioneSuccess() {
		LocalDateTime inizio = LocalDateTime.now().plusDays(1);
		LocalDateTime fine = inizio.plusDays(1);

		Prenotazione p = gestore.creaPrenotazione(driver, veicolo, inizio, fine);

		assertNotNull(p);
		assertEquals(StatoPrenotazione.RICHIESTA, p.getStato());

		List<Notifica> lista = notificaDAO.findByUtente(driver.getIdUtente());
		assertEquals(1, lista.size());
		assertEquals(TipoNotifica.PRENOTAZIONE, lista.get(0).getTipoNotifica());
	}

	@Test
	void testCreaPrenotazioneDriverSenzaPatente() {
		Utente senzaPatente = new Utente(50, "X", "Y", "xy@mail.com", "pwd", RuoloUtente.DRIVER);

		assertThrows(IllegalArgumentException.class, () -> gestore.creaPrenotazione(senzaPatente, veicolo,
				LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
	}

	@Test
	void testCreaPrenotazioneVeicoloNonDisponibile() {
		LocalDateTime d1 = LocalDateTime.now().plusDays(1);
		LocalDateTime d2 = d1.plusDays(1);

		prenotazioneDAO.save(new Prenotazione(100, d1, d2, StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE,
				driver.getIdUtente(), veicolo.getTarga()));

		assertThrows(IllegalArgumentException.class,
				() -> gestore.creaPrenotazione(driver, veicolo, d1.plusHours(2), d2.minusHours(2)));
	}

	@Test
	void testConfermaPrenotazioneSuccess() {
		prenotazioneDAO.save(new Prenotazione(200, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		gestore.confermaPrenotazione(200, manager);

		Prenotazione p = prenotazioneDAO.getById(200);
		assertEquals(StatoPrenotazione.CONFERMATA, p.getStato());

		List<Notifica> nots = notificaDAO.findByUtente(driver.getIdUtente());
		assertEquals(1, nots.size());
	}

	@Test
	void testConfermaPrenotazioneNonManager() {
		assertThrows(IllegalArgumentException.class, () -> gestore.confermaPrenotazione(1, driver));
	}

	@Test
	void testConfermaPrenotazioneInesistente() {
		assertThrows(IllegalArgumentException.class, () -> gestore.confermaPrenotazione(999, manager));
	}

	@Test
	void testAnnullaPrenotazioneSuccess() {
		prenotazioneDAO.save(new Prenotazione(300, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		gestore.annullaPrenotazione(300, driver);

		Prenotazione p = prenotazioneDAO.getById(300);
		assertEquals(StatoPrenotazione.ANNULLATA, p.getStato());

		List<Notifica> nots = notificaDAO.findByUtente(driver.getIdUtente());
		assertEquals(1, nots.size());
	}

	@Test
	void testAnnullaPrenotazioneInesistente() {
		assertThrows(IllegalArgumentException.class, () -> gestore.annullaPrenotazione(999, driver));
	}

	@Test
	void testGetPrenotazioniDriverSuccess() {
		prenotazioneDAO.save(new Prenotazione(400, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		List<Prenotazione> lista = gestore.getPrenotazioniDriver(driver);
		assertEquals(1, lista.size());
	}

	@Test
	void testGetPrenotazioniDriverSenzaPatente() {
		Utente fake = new Utente(500, "X", "Y", "mail", "pwd", RuoloUtente.DRIVER);

		assertThrows(IllegalArgumentException.class, () -> gestore.getPrenotazioniDriver(fake));
	}

	@Test
	void testGetPrenotazioniVeicoloSuccess() {
		prenotazioneDAO.save(new Prenotazione(600, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		List<Prenotazione> lista = gestore.getPrenotazioniVeicolo(veicolo);
		assertEquals(1, lista.size());
	}
}
