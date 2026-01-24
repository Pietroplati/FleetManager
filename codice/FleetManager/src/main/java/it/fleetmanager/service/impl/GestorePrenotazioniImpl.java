package it.fleetmanager.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.service.interfaces.GestorePrenotazioni;
import it.fleetmanager.util.RuoloUtente;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class GestorePrenotazioniImpl implements GestorePrenotazioni {

    private final PrenotazioneDAO prenotazioneDAO;
    private final UtenteDAO utenteDAO;
    private final SistemaNotifiche sistemaNotifiche;

    public GestorePrenotazioniImpl(
            PrenotazioneDAO prenotazioneDAO,
            UtenteDAO utenteDAO,
            SistemaNotifiche sistemaNotifiche
    ) {
        this.prenotazioneDAO = prenotazioneDAO;
        this.utenteDAO = utenteDAO;
        this.sistemaNotifiche = sistemaNotifiche;
    }

    @Override
    public List<Prenotazione> getPrenotazioniVisibiliOrdinare(Utente utenteLoggato) {
        List<Prenotazione> tutte = prenotazioneDAO.findAll();

        if (utenteLoggato.getRuoloUtente() != RuoloUtente.MANAGER) {
            tutte = tutte.stream()
                    .filter(p -> p.getIdUtente() == utenteLoggato.getIdUtente())
                    .collect(Collectors.toList());
        }

        tutte.sort((a, b) -> {
            int cmp = Integer.compare(priorita(a, utenteLoggato), priorita(b, utenteLoggato));
            return (cmp != 0) ? cmp : confrontoTemporale(a, b);
        });

        return tutte;
    }

    @Override
    public Map<Integer, Utente> getUtentiById() {
        Map<Integer, Utente> map = new HashMap<>();
        for (Utente u : utenteDAO.getTuttiUtenti()) {
            map.put(u.getIdUtente(), u);
        }
        return map;
    }

    private int priorita(Prenotazione p, Utente utenteLoggato) {
        StatoPrenotazione s = p.getStato();
        boolean isManager = (utenteLoggato.getRuoloUtente() == RuoloUtente.MANAGER);

        if (isManager) {
            return switch (s) {
                case RICHIESTA -> 1;
                case ATTIVA -> 2;
                case CONFERMATA -> 3;
                case COMPLETATA -> 4;
                case ANNULLATA -> 5;
            };
        }

        return switch (s) {
            case ATTIVA -> 1;
            case RICHIESTA -> 2;
            case CONFERMATA -> 3;
            case COMPLETATA -> 4;
            case ANNULLATA -> 5;
        };
    }

    private int confrontoTemporale(Prenotazione a, Prenotazione b) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ta = a.getDataInizio().isAfter(now) ? a.getDataInizio() : a.getDataFine();
        LocalDateTime tb = b.getDataInizio().isAfter(now) ? b.getDataInizio() : b.getDataFine();
        return ta.compareTo(tb);
    }

    @Override
    public Prenotazione creaPrenotazione(Utente driver, Veicolo veicolo, LocalDateTime dataInizio,
                                        LocalDateTime dataFine) {

        if (driver.getPatente() == null)
            throw new IllegalArgumentException("L’utente non ha la patente, non può prenotare veicoli.");

        if (!validadisponibilita(veicolo, dataInizio, dataFine))
            throw new IllegalArgumentException(
                    "Il veicolo " + veicolo.getTarga() + " non è disponibile nelle date richieste.");

        Prenotazione p = new Prenotazione(0, dataInizio, dataFine, StatoPrenotazione.RICHIESTA, TipoPrenotazione.UTENTE,
                driver.getIdUtente(), veicolo.getTarga());

        prenotazioneDAO.save(p);

        sistemaNotifiche.notificaRichiestaPrenotazione(driver, p);

        return p;
    }

    @Override
    public boolean validadisponibilita(Veicolo veicolo, LocalDateTime dataInizio, LocalDateTime dataFine) {

        List<Prenotazione> prenotazioni = prenotazioneDAO.findByVeicolo(veicolo.getTarga());

        for (Prenotazione p : prenotazioni) {
            boolean overlap = dataInizio.isBefore(p.getDataFine()) && dataFine.isAfter(p.getDataInizio());

            if (overlap && p.getStato() != StatoPrenotazione.ANNULLATA)
                return false;
        }
        return true;
    }

    @Override
    public void confermaPrenotazione(int idPrenotazione, Utente manager) {

        if (manager.getRuoloUtente() != RuoloUtente.MANAGER)
            throw new IllegalArgumentException("Solo un manager può confermare una prenotazione.");

        Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

        if (p == null || p.getIdPrenotazione() == -1)
            throw new IllegalArgumentException("Prenotazione non trovata");

        p.setStato(StatoPrenotazione.CONFERMATA);
        prenotazioneDAO.update(p);

        Utente driver = utenteDAO.getUtenteById(p.getIdUtente());
        sistemaNotifiche.notificaConfermaPrenotazione(driver, p);
    }

    @Override
    public void annullaPrenotazione(int idPrenotazione, Utente utente) {

        Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

        if (p == null || p.getIdPrenotazione() == -1)
            throw new IllegalArgumentException("Prenotazione non trovata");

        p.setStato(StatoPrenotazione.ANNULLATA);
        prenotazioneDAO.update(p);

        Utente driver = utenteDAO.getUtenteById(p.getIdUtente());

        if (utente.getRuoloUtente() == RuoloUtente.MANAGER) {
            sistemaNotifiche.notificaRifiutoPrenotazione(driver, p);
        } else if (utente.getRuoloUtente() == RuoloUtente.DRIVER) {
            sistemaNotifiche.notificaAnnullamentoPrenotazioneDaDriver(driver, p);
        }
    }

    @Override
    public void attivaPrenotazione(int idPrenotazione) {
        Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

        if (p == null || p.getIdPrenotazione() == -1)
            throw new IllegalArgumentException("Prenotazione non trovata");

        if (LocalDateTime.now().isBefore(p.getDataInizio()))
            throw new IllegalStateException("Non è possibile attivare la prenotazione prima della data di inizio.");

        p.setStato(StatoPrenotazione.ATTIVA);
        prenotazioneDAO.update(p);
    }

    @Override
    public void completaPrenotazione(int idPrenotazione) {

        Prenotazione p = prenotazioneDAO.getById(idPrenotazione);

        if (p == null || p.getIdPrenotazione() == -1)
            throw new IllegalArgumentException("Prenotazione non trovata");

        if (LocalDateTime.now().isBefore(p.getDataFine()))
            throw new IllegalStateException("Non è possibile completare la prenotazione prima della data di fine.");

        p.setStato(StatoPrenotazione.COMPLETATA);
        prenotazioneDAO.update(p);
    }

    @Override
    public List<Prenotazione> getPrenotazioniDriver(Utente driver) {
        if (driver.getPatente() == null)
            throw new IllegalArgumentException("Questo utente non è un driver.");

        return prenotazioneDAO.findByDriver(driver.getIdUtente());
    }

    @Override
    public List<Prenotazione> getPrenotazioniVeicolo(Veicolo veicolo) {
        return prenotazioneDAO.findByVeicolo(veicolo.getTarga());
    }

    @Override
    public void aggiornaStatiPrenotazioni() {

        LocalDateTime now = LocalDateTime.now();

        for (Prenotazione p : prenotazioneDAO.findByStato(StatoPrenotazione.CONFERMATA)) {
            if (!now.isBefore(p.getDataInizio()) && now.isBefore(p.getDataFine())) {
                p.setStato(StatoPrenotazione.ATTIVA);
                prenotazioneDAO.update(p);
            }
        }

        for (Prenotazione p : prenotazioneDAO.findByStato(StatoPrenotazione.ATTIVA)) {
            if (!now.isBefore(p.getDataFine())) {
                p.setStato(StatoPrenotazione.COMPLETATA);
                prenotazioneDAO.update(p);
            }
        }
    }

    @Override
    public List<Prenotazione> getTuttePrenotazioni() {
        return prenotazioneDAO.findAll();
    }

   
}
