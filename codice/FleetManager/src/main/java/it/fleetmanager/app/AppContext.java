package it.fleetmanager.app;

import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.db.H2DatabaseManager;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.impl.ScadenzaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.service.impl.GestoreLoginImpl;
import it.fleetmanager.service.impl.GestoreManutenzioniImpl;
import it.fleetmanager.service.impl.GestorePrenotazioniImpl;
import it.fleetmanager.service.impl.SistemaNotifiche;
import it.fleetmanager.service.impl.UiFacadeImpl;
import it.fleetmanager.service.interfaces.GestoreLogin;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.service.interfaces.UiFacade;

/**
 * {@code AppContext} rappresenta il <b>Composition Root</b> dell'applicazione
 * FleetManager.
 * <p>
 * Questa classe è responsabile della creazione, configurazione e fornitura
 * delle principali dipendenze del sistema (DAO e servizi), centralizzando
 * l'istanziazione delle implementazioni concrete.
 * </p>
 *
 * <p>
 * L'obiettivo è:
 * <ul>
 * <li>separare la logica applicativa dalla creazione degli oggetti</li>
 * <li>ridurre l'accoppiamento tra UI, servizi e repository</li>
 * <li>evitare istanziazioni dirette all'interno dei controller UI</li>
 * </ul>
 * </p>
 *
 * <p>
 * {@code AppContext} è implementato come <b>Singleton</b> ed espone factory
 * methods sincronizzati per l'accesso alle dipendenze condivise.
 * </p>
 */
public final class AppContext {

	/**
	 * Istanza singleton del contesto applicativo.
	 */
	private static AppContext instance;

	/**
	 * Database manager condiviso.
	 */
	private final H2DatabaseManager db;

	// ===== DAO singleton =====

	/** Data Access Object per la gestione degli utenti. */
	private final UtenteDAO utenteDAO;

	/** Data Access Object per la gestione dei veicoli. */
	private final VeicoloDAO veicoloDAO;

	/** Data Access Object per la gestione delle prenotazioni. */
	private final PrenotazioneDAO prenotazioneDAO;

	/** Data Access Object per la gestione delle notifiche. */
	private final NotificaDAO notificaDAO;

	/** Data Access Object per la gestione delle manutenzioni. */
	private final ManutenzioneDAO manutenzioneDAO;

	/** Data Access Object per la gestione delle scadenze. */
	private final ScadenzaDAO scadenzaDAO;

	// ===== servizi cached =====

	/**
	 * Sistema di notifiche condiviso tra i servizi.
	 */
	private final SistemaNotifiche sistemaNotifiche;

	/** Gestore delle prenotazioni (lazy initialization). */
	private GestorePrenotazioni gestorePrenotazioni;

	/** Gestore delle manutenzioni (lazy initialization). */
	private GestoreManutenzioniImpl gestoreManutenzioni;

	/** Facade per l'interazione tra UI e livello applicativo. */
	private UiFacade uiFacade;

	/**
	 * Gestore del login centralizzato.
	 */
	private GestoreLogin gestoreLogin;

	/**
	 * Costruttore privato.
	 * <p>
	 * Inizializza il database manager, i DAO e i servizi condivisi.
	 * </p>
	 */
	private AppContext() {
		this.db = H2DatabaseManager.getInstance();

		this.utenteDAO = new UtenteDAOImpl(db);
		this.veicoloDAO = new VeicoloDAOImpl(db);
		this.prenotazioneDAO = new PrenotazioneDAOImpl(db);
		this.notificaDAO = new NotificaDAOImpl(db);
		this.manutenzioneDAO = new ManutenzioneDAOImpl(db);
		this.scadenzaDAO = new ScadenzaDAOImpl(db);

		// istanza unica del sistema di notifiche
		this.sistemaNotifiche = new SistemaNotifiche(notificaDAO, utenteDAO);
	}

	/**
	 * Restituisce l'istanza singleton di {@code AppContext}.
	 *
	 * @return istanza unica del contesto applicativo
	 */
	public static synchronized AppContext getInstance() {
		if (instance == null) {
			instance = new AppContext();
		}
		return instance;
	}

	// ===== getter DAO =====

	/**
	 * @return DAO per la gestione degli utenti
	 */
	public UtenteDAO getUtenteDAO() {
		return utenteDAO;
	}

	/**
	 * @return DAO per la gestione dei veicoli
	 */
	public VeicoloDAO getVeicoloDAO() {
		return veicoloDAO;
	}

	/**
	 * @return DAO per la gestione delle prenotazioni
	 */
	public PrenotazioneDAO getPrenotazioneDAO() {
		return prenotazioneDAO;
	}

	/**
	 * @return DAO per la gestione delle notifiche
	 */
	public NotificaDAO getNotificaDAO() {
		return notificaDAO;
	}

	/**
	 * @return DAO per la gestione delle manutenzioni
	 */
	public ManutenzioneDAO getManutenzioneDAO() {
		return manutenzioneDAO;
	}

	/**
	 * @return DAO per la gestione delle scadenze
	 */
	public ScadenzaDAO getScadenzaDAO() {
		return scadenzaDAO;
	}

	// ===== getter servizi =====

	/**
	 * Restituisce il sistema di notifiche condiviso.
	 *
	 * @return sistema di notifiche
	 */
	public SistemaNotifiche getSistemaNotifiche() {
		return sistemaNotifiche;
	}

	/**
	 * Restituisce il gestore delle prenotazioni.
	 * <p>
	 * L'istanza viene creata alla prima richiesta (lazy initialization).
	 * </p>
	 *
	 * @return gestore delle prenotazioni
	 */
	public synchronized GestorePrenotazioni getGestorePrenotazioni() {
		if (gestorePrenotazioni == null) {
			gestorePrenotazioni = new GestorePrenotazioniImpl(prenotazioneDAO, utenteDAO, sistemaNotifiche);
		}
		return gestorePrenotazioni;
	}

	/**
	 * Restituisce il gestore delle manutenzioni.
	 *
	 * @return gestore delle manutenzioni
	 */
	public synchronized GestoreManutenzioniImpl getGestoreManutenzioni() {
		if (gestoreManutenzioni == null) {
			gestoreManutenzioni = new GestoreManutenzioniImpl(manutenzioneDAO, veicoloDAO, sistemaNotifiche);
		}
		return gestoreManutenzioni;
	}

	/**
	 * Restituisce la facade utilizzata dalla UI.
	 * <p>
	 * La UI non accede direttamente ai DAO ma interagisce esclusivamente tramite
	 * questa interfaccia.
	 * </p>
	 *
	 * @return facade per la UI
	 */
	public synchronized UiFacade getUiFacade() {
		if (uiFacade == null) {
			uiFacade = new UiFacadeImpl(
					veicoloDAO,
					prenotazioneDAO,
					manutenzioneDAO,
					scadenzaDAO,
					notificaDAO,
					utenteDAO,
					getGestorePrenotazioni(),
					getGestoreManutenzioni(),
					sistemaNotifiche);
		}
		return uiFacade;
	}

	/**
	 * Restituisce il gestore del login.
	 * <p>
	 * Centralizza la gestione dell'autenticazione evitando che la UI crei
	 * direttamente l'implementazione concreta.
	 * </p>
	 *
	 * @return gestore del login
	 */
	public synchronized GestoreLogin getGestoreLogin() {
		if (gestoreLogin == null) {
			gestoreLogin = new GestoreLoginImpl(utenteDAO);
		}
		return gestoreLogin;
	}
}
