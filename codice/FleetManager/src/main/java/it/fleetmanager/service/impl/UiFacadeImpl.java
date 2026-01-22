package it.fleetmanager.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoManutenzione;
import it.fleetmanager.util.TipoNotifica;


public class UiFacadeImpl implements UiFacade {

    private final VeicoloDAO veicoloDAO;
    private final PrenotazioneDAO prenotazioneDAO;
    private final ManutenzioneDAO manutenzioneDAO;
    private final ScadenzaDAO scadenzaDAO;
    private final NotificaDAO notificaDAO;
    private final UtenteDAO utenteDAO;

    private final GestorePrenotazioni gestorePrenotazioni;
    private final GestoreManutenzioniImpl gestoreManutenzioni;
    private final SistemaNotifiche sistemaNotifiche;

 // NOSONAR - Facade con molte dipendenze intenzionali (composition root)
    public UiFacadeImpl(
            VeicoloDAO veicoloDAO,
            PrenotazioneDAO prenotazioneDAO,
            ManutenzioneDAO manutenzioneDAO,
            ScadenzaDAO scadenzaDAO,
            NotificaDAO notificaDAO,
            UtenteDAO utenteDAO,
            GestorePrenotazioni gestorePrenotazioni,
            GestoreManutenzioniImpl gestoreManutenzioni,
            SistemaNotifiche sistemaNotifiche
    ) {

        this.veicoloDAO = veicoloDAO;
        this.prenotazioneDAO = prenotazioneDAO;
        this.manutenzioneDAO = manutenzioneDAO;
        this.scadenzaDAO = scadenzaDAO;
        this.notificaDAO = notificaDAO;
        this.utenteDAO = utenteDAO;
        this.gestorePrenotazioni = gestorePrenotazioni;
        this.gestoreManutenzioni = gestoreManutenzioni;
        this.sistemaNotifiche = sistemaNotifiche;
    }

    // ===== UTENTI =====
    @Override
    public List<Utente> getTuttiUtenti() {
        return utenteDAO.getTuttiUtenti();
    }

    @Override
    public Utente getUtenteById(int idUtente) {
        return utenteDAO.getUtenteById(idUtente);
    }

    // ===== VEICOLI =====
    @Override
    public List<Veicolo> getTuttiVeicoli() {
        return veicoloDAO.getTuttiVeicoli();
    }

    @Override
    public Veicolo getVeicoloByTarga(String targa) {
        return veicoloDAO.getVeicoloByTarga(targa);
    }

    @Override
    public void aggiornaVeicolo(Veicolo v) {
        veicoloDAO.update(v);
    }

    @Override
    public void eliminaVeicolo(String targa) {
        veicoloDAO.delete(targa);
    }

    // ===== PRENOTAZIONI =====
    @Override
    public List<Prenotazione> getTuttePrenotazioni() {
        return prenotazioneDAO.findAll();
    }

    @Override
    public List<Prenotazione> getPrenotazioniDriver(int idDriver) {
        return prenotazioneDAO.findByDriver(idDriver);
    }

    @Override
    public List<Prenotazione> getPrenotazioniByStato(StatoPrenotazione stato) {
        return prenotazioneDAO.findByStato(stato);
    }

    @Override
    public Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine) {
        return gestorePrenotazioni.creaPrenotazione(driver, veicolo, dataInizio, dataFine);
    }

    @Override
    public void confermaPrenotazione(int idPrenotazione, Utente manager) {
        gestorePrenotazioni.confermaPrenotazione(idPrenotazione, manager);
    }

    @Override
    public void annullaPrenotazione(int idPrenotazione, Utente utente) {
        gestorePrenotazioni.annullaPrenotazione(idPrenotazione, utente);
    }

    @Override
    public void completaPrenotazione(int idPrenotazione) {
        gestorePrenotazioni.completaPrenotazione(idPrenotazione);
    }

    @Override
    public void aggiornaStatiPrenotazioni() {
        gestorePrenotazioni.aggiornaStatiPrenotazioni();
    }

    // ===== MANUTENZIONI =====
    @Override
    public List<Manutenzione> getTutteManutenzioni() {
        return manutenzioneDAO.getTutteManutenzioni();
    }

    @Override
    public void chiudiManutenzione(int idManutenzione) {
        gestoreManutenzioni.chiudiManutenzione(idManutenzione);
    }

    @Override
    public void programmareManutenzione(Veicolo v, LocalDateTime inizio, TipoManutenzione tipo, String descrizione) {
        gestoreManutenzioni.programmareManutenzione(v, inizio, tipo, descrizione);
    }

    @Override
    public void segnalareInterventoStraordinario(Veicolo v, String descrizione) {
        gestoreManutenzioni.segnalareInterventoStraordinario(v, descrizione);
    }

    // ===== SCADENZE =====
    @Override
    public List<Scadenza> getTutteScadenze() {
        return scadenzaDAO.getTutteScadenze();
    }

    @Override
    public void eliminaScadenza(int idScadenza) {
        scadenzaDAO.delete(idScadenza);
    }

    @Override
    public void aggiornaScadenza(Scadenza s) {
        scadenzaDAO.update(s);
    }

    @Override
    public void controllaScadenzeENotifica() {
        List<Scadenza> scadenze = scadenzaDAO.getTutteScadenze();

        LocalDate oggi = LocalDate.now();
        LocalDate limite = oggi.plusDays(7);

        for (Scadenza s : scadenze) {

            if (s.getNotificata())
                continue;

            if (!s.getData().isAfter(limite)) {

                sistemaNotifiche.inviaNotificaScadenza(s);

                s.setNotificata(true);
                scadenzaDAO.update(s);
            }
        }
    }

    //NOTIFICHE (granulari)
    @Override
    public List<Notifica> getTutteNotifiche() {
        return notificaDAO.findAll();
    }

    @Override
    public List<Notifica> getNotificheByUtente(int idUtente) {
        return notificaDAO.findByUtente(idUtente);
    }

    @Override
    public List<Notifica> getNotificheNonLette(int idUtente) {
        return notificaDAO.findNonLette(idUtente);
    }

    @Override
    public void aggiornaNotifica(Notifica n) {
        notificaDAO.update(n);
    }

    //NOTIFICHE (alto livello, compatibilità)
    @Override
    public List<Notifica> getNotifichePerUtente(Utente u) {
        if (u.getRuoloUtente() == RuoloUtente.MANAGER) {
            return notificaDAO.findAll();
        }
        return notificaDAO.findByUtente(u.getIdUtente());
    }

    @Override
    public void segnaComeLetta(Notifica n) {
        n.setLetta(true);
        notificaDAO.update(n);
    }

    @Override
    public void segnaTutteComeLette(Utente u) {
        List<Notifica> target;
        if (u.getRuoloUtente() == RuoloUtente.MANAGER) {
            target = notificaDAO.findAll();
        } else {
            target = notificaDAO.findNonLette(u.getIdUtente());
        }

        for (Notifica n : target) {
            if (!n.getLetta()) {
                n.setLetta(true);
                notificaDAO.update(n);
            }
        }
    }

    // ===== ACTION SPECIFICA NOTIFICHE =====
    @Override
    public void impostaVeicoloNonDisponibile(String targa, StatoVeicolo stato) {
        Veicolo v = veicoloDAO.getVeicoloByTarga(targa);
        if (v == null || "N/A".equals(v.getTarga())) {
            throw new IllegalArgumentException("Veicolo non trovato nel database.");
        }
        v.setStatoVeicolo(stato);
        veicoloDAO.update(v);
    }
    
    @Override
    public void inviaSegnalazioneStraordinaria(Utente driver, Veicolo veicolo, String descrizione) {

        Utente manager = utenteDAO.getTuttiUtenti().stream()
                .filter(u -> u.getRuoloUtente() == RuoloUtente.MANAGER)
                .findFirst()
                .orElse(null);

        if (manager == null) {
            throw new IllegalStateException("Nessun manager presente nel sistema.");
        }

        String msg = """
                Segnalazione straordinaria da %s %s
                Veicolo: %s (%s %s)
                Problema: %s
                """.formatted(
                driver.getNome(),
                driver.getCognome(),
                veicolo.getTarga(),
                veicolo.getMarca(),
                veicolo.getModello(),
                descrizione
        );

        Notifica n = new Notifica(null, TipoNotifica.SEGNALAZIONE, msg, false, manager.getIdUtente(), null);
        notificaDAO.save(n);
    }
    
    @Override
    public void salvaScadenza(Scadenza s) {
        if (s == null) throw new IllegalArgumentException("Scadenza null.");

        if (s.getIdScadenza() == 0) {
            scadenzaDAO.save(s);
        } else {
            scadenzaDAO.update(s);
        }
    }
    
    @Override
    public void salvaVeicolo(Veicolo v) {
        veicoloDAO.save(v);
    }

    @Override
    public List<Prenotazione> getPrenotazioniVisibiliOrdinare(Utente utenteLoggato) {
        return gestorePrenotazioni.getPrenotazioniVisibiliOrdinare(utenteLoggato);
    }

    @Override
    public Map<Integer, Utente> getUtentiById() {
        return gestorePrenotazioni.getUtentiById();
    }


}
