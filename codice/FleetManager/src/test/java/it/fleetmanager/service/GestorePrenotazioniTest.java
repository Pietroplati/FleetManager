package it.fleetmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.repository.PrenotazioneDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.SistemaNotifiche;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoNotifica;
import it.fleetmanager.util.TipoPrenotazione;
import it.fleetmanager.util.TipoVeicolo;

public class GestorePrenotazioniTest {

	private PrenotazioneDAO prenotazioneDAO;
	private NotificaDAO notificaDAO;
	private SistemaNotifiche sistemaNotifiche;
	private GestorePrenotazioni gestore;

	private Utente driver;
	private Utente manager;
	private Veicolo veicolo;

	private static final String TEST_DB_URL = "jdbc:h2:mem:fleet_test;DB_CLOSE_DELAY=-1";

	@BeforeEach
	void setup() throws Exception {

		DatabaseManager.setTestUrl(TEST_DB_URL);

		try (Connection conn = DatabaseManager.getInstance().getConnection(); Statement stmt = conn.createStatement()) {

			stmt.execute("DROP TABLE IF EXISTS Notifica;");
			stmt.execute("DROP TABLE IF EXISTS Prenotazione;");

			stmt.execute("""
					    CREATE TABLE Prenotazione (
					        idPrenotazione INT PRIMARY KEY,
					        dataInizio TIMESTAMP NOT NULL,
					        dataFine TIMESTAMP NOT NULL,
					        statoPrenotazione VARCHAR(20) NOT NULL,
					        tipoPrenotazione VARCHAR(20) NOT NULL,
					        idUtente INT NOT NULL,
					        targa VARCHAR(20) NOT NULL
					    );
					""");

			stmt.execute("""
					    CREATE TABLE Notifica (
					        idNotifica INT PRIMARY KEY,
					        tipoNotifica VARCHAR(20) NOT NULL,
					        messaggio VARCHAR(255) NOT NULL,
					        dataInvio TIMESTAMP NOT NULL,
					        letta BOOLEAN NOT NULL,
					        idUtente INT NOT NULL,
					        idScadenza INT
					    );
					""");
		}

		prenotazioneDAO = new PrenotazioneDAOImpl();
		notificaDAO = new NotificaDAOImpl();
		sistemaNotifiche = new SistemaNotifiche(notificaDAO);

		gestore = new GestorePrenotazioni(prenotazioneDAO, null, sistemaNotifiche);

		driver = new Utente(1, "Mario", "Rossi", "mario@mail.com", "pwd", RuoloUtente.DRIVER, "B");
		manager = new Utente(2, "Luca", "Bianchi", "luca@mail.com", "pwd", RuoloUtente.MANAGER);
		veicolo = new Veicolo("AB123CD", TipoVeicolo.AUTO, "Fiat", "Panda", 2020, StatoVeicolo.DISPONIBILE, 10000);
	}

	@Test
	void testCreaPrenotazioneSuccess() {

		LocalDateTime inizio = LocalDateTime.now().plusDays(1);
		LocalDateTime fine = inizio.plusDays(1);

		Prenotazione p = gestore.creaPrenotazione(driver, veicolo, inizio, fine);

		assertNotNull(p);
		assertEquals(StatoPrenotazione.RICHIESTA, p.getStato());

		Notifica notif = notificaDAO.getNotificaById(0);
		assertNotEquals(NotificaDAOImpl.NOTIFICA_INESISTENTE.getIdNotifica(), notif.getIdNotifica());
		assertEquals(TipoNotifica.PRENOTAZIONE, notif.getTipoNotifica());
	}

	@Test
	void testCreaPrenotazioneDriverSenzaPatente() {
		Utente senzaPatente = new Utente(10, "X", "Y", "mail", "pwd", RuoloUtente.DRIVER);
		assertThrows(IllegalArgumentException.class, () -> gestore.creaPrenotazione(senzaPatente, veicolo,
				LocalDateTime.now(), LocalDateTime.now().plusDays(1)));
	}

	@Test
	void testCreaPrenotazioneVeicoloNonDisponibile() {

		LocalDateTime oggi = LocalDateTime.now();
		LocalDateTime domani = oggi.plusDays(1);

		prenotazioneDAO.save(new Prenotazione(1, oggi, domani, StatoPrenotazione.CONFERMATA, TipoPrenotazione.UTENTE,
				driver.getIdUtente(), veicolo.getTarga()));

		assertThrows(IllegalArgumentException.class,
				() -> gestore.creaPrenotazione(driver, veicolo, oggi.plusHours(2), domani.minusHours(2)));
	}

	@Test
	void testConfermaPrenotazioneSuccess() {

		prenotazioneDAO.save(new Prenotazione(1, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		gestore.confermaPrenotazione(1, manager);

		Prenotazione aggiornata = prenotazioneDAO.getById(1);
		assertEquals(StatoPrenotazione.CONFERMATA, aggiornata.getStato());

		List<Notifica> notifiche = notificaDAO.findByUtente(driver.getIdUtente());
		assertEquals(1, notifiche.size());
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

		prenotazioneDAO.save(new Prenotazione(5, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		gestore.annullaPrenotazione(5, driver);

		Prenotazione aggiornata = prenotazioneDAO.getById(5);
		assertEquals(StatoPrenotazione.ANNULLATA, aggiornata.getStato());

		List<Notifica> notifiche = notificaDAO.findByUtente(driver.getIdUtente());
		assertEquals(1, notifiche.size());
	}

	@Test
	void testAnnullaPrenotazioneInesistente() {
		assertThrows(IllegalArgumentException.class, () -> gestore.annullaPrenotazione(999, driver));
	}

	@Test
	void testGetPrenotazioniDriverSuccess() {

		prenotazioneDAO.save(new Prenotazione(10, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		List<Prenotazione> lista = gestore.getPrenotazioniDriver(driver);
		assertEquals(1, lista.size());
	}

	@Test
	void testGetPrenotazioniDriverSenzaPatente() {
		Utente fake = new Utente(99, "X", "Y", "mail", "pwd", RuoloUtente.DRIVER);
		assertThrows(IllegalArgumentException.class, () -> gestore.getPrenotazioniDriver(fake));
	}

	@Test
	void testGetPrenotazioniVeicoloSuccess() {

		prenotazioneDAO.save(new Prenotazione(11, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
				StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE, driver.getIdUtente(), veicolo.getTarga()));

		List<Prenotazione> lista = gestore.getPrenotazioniVeicolo(veicolo);
		assertEquals(1, lista.size());
	}
}
