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
 * Composition Root: centralizza le istanziazioni delle implementazioni.
 * Nessuna logica cambiata: stessi DAO, stesso DB singleton.
 */
public class AppContext {

    private static AppContext instance;

    private final H2DatabaseManager db;

    // ===== DAO singleton =====
    private final UtenteDAO utenteDAO;
    private final VeicoloDAO veicoloDAO;
    private final PrenotazioneDAO prenotazioneDAO;
    private final NotificaDAO notificaDAO;
    private final ManutenzioneDAO manutenzioneDAO;
    private final ScadenzaDAO scadenzaDAO;

    // ===== servizi cached =====
    private final SistemaNotifiche sistemaNotifiche;

    private volatile GestorePrenotazioni gestorePrenotazioni;
    private GestoreManutenzioniImpl gestoreManutenzioni;
    private UiFacade uiFacade;

    //aggiunto per LoginController pulito
    private GestoreLogin gestoreLogin;

    private AppContext() {
        this.db = H2DatabaseManager.getInstance();

        this.utenteDAO = new UtenteDAOImpl(db);
        this.veicoloDAO = new VeicoloDAOImpl(db);
        this.prenotazioneDAO = new PrenotazioneDAOImpl(db);
        this.notificaDAO = new NotificaDAOImpl(db);
        this.manutenzioneDAO = new ManutenzioneDAOImpl(db);
        this.scadenzaDAO = new ScadenzaDAOImpl(db);

        // istanza unica
        this.sistemaNotifiche = new SistemaNotifiche(notificaDAO, utenteDAO);
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    // ===== getter DAO =====
    public UtenteDAO getUtenteDAO() { return utenteDAO; }
    public VeicoloDAO getVeicoloDAO() { return veicoloDAO; }
    public PrenotazioneDAO getPrenotazioneDAO() { return prenotazioneDAO; }
    public NotificaDAO getNotificaDAO() { return notificaDAO; }
    public ManutenzioneDAO getManutenzioneDAO() { return manutenzioneDAO; }
    public ScadenzaDAO getScadenzaDAO() { return scadenzaDAO; }

    // ===== getter servizi =====
    public SistemaNotifiche getSistemaNotifiche() {
        return sistemaNotifiche;
    }

    public GestorePrenotazioni getGestorePrenotazioni() {
        if (gestorePrenotazioni == null) {
            synchronized (this) {
                if (gestorePrenotazioni == null) {
                    gestorePrenotazioni = new GestorePrenotazioniImpl(
                        prenotazioneDAO,
                        utenteDAO,
                        sistemaNotifiche
                    );
                }
            }
        }
        return gestorePrenotazioni;
    }


    public synchronized GestoreManutenzioniImpl getGestoreManutenzioni() {
        if (gestoreManutenzioni == null) {
            gestoreManutenzioni = new GestoreManutenzioniImpl(
                    manutenzioneDAO,
                    veicoloDAO,
                    sistemaNotifiche
            );
        }
        return gestoreManutenzioni;
    }

    /**
     * Facade per la UI: la UI non deve conoscere i DAO.
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
                    sistemaNotifiche
            );
        }
        return uiFacade;
    }

    //nuovo: gestione login centralizzata (UI non crea più GestoreLoginImpl)
    public synchronized GestoreLogin getGestoreLogin() {
        if (gestoreLogin == null) {
            gestoreLogin = new GestoreLoginImpl(utenteDAO);
        }
        return gestoreLogin;
    }
}
