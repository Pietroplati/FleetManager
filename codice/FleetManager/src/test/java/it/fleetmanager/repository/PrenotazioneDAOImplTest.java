package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class PrenotazioneDAOImplTest {

    private PrenotazioneDAO prenotazioneDAO;

    @BeforeEach
    void setup() throws Exception {
        DatabaseTestUtils.resetDatabase();
        prenotazioneDAO = new PrenotazioneDAOImpl(H2DatabaseManager.getInstance());
    }

    // ============================================================
    // SAVE + GET BY ID
    // ============================================================
    @Test
    void testSaveAndGetById() {

        Prenotazione p = new Prenotazione(
                0,
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "AB123CD"
        );

        prenotazioneDAO.save(p);

        assertTrue(p.getIdPrenotazione() > 0);

        Prenotazione loaded =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.CONFERMATA, loaded.getStato());
        assertEquals("AB123CD", loaded.getTarga());
        assertEquals(2, loaded.getIdUtente());
    }

    // ============================================================
    // GET BY ID NOT FOUND
    // ============================================================
    @Test
    void testGetById_NotFound() {
        Prenotazione p = prenotazioneDAO.getById(9999);
        assertEquals(-1, p.getIdPrenotazione());
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Test
    void testUpdate() {

        Prenotazione p = new Prenotazione(
                0,
                LocalDateTime.of(2025, 2, 1, 10, 0),
                LocalDateTime.of(2025, 2, 1, 12, 0),
                StatoPrenotazione.RICHIESTA,
                TipoPrenotazione.UTENTE,
                2,
                "GH819RJ"
        );

        prenotazioneDAO.save(p);

        p.setStato(StatoPrenotazione.CONFERMATA);
        prenotazioneDAO.update(p);

        Prenotazione updated =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(StatoPrenotazione.CONFERMATA, updated.getStato());
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Test
    void testDelete() {

        Prenotazione p = new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "T1"
        );

        prenotazioneDAO.save(p);

        prenotazioneDAO.delete(p.getIdPrenotazione());

        Prenotazione deleted =
                prenotazioneDAO.getById(p.getIdPrenotazione());

        assertEquals(-1, deleted.getIdPrenotazione());
    }

    // ============================================================
    // FIND BY DRIVER
    // ============================================================
    @Test
    void testFindByDriver() {

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "V1"
        ));

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "V2"
        ));

        List<Prenotazione> list =
                prenotazioneDAO.findByDriver(2);

        assertEquals(2, list.size());
        assertTrue(list.stream()
                .allMatch(p -> p.getIdUtente() == 2));
    }

    // ============================================================
    // FIND BY VEICOLO
    // ============================================================
    @Test
    void testFindByVeicolo() {

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "T2"
        ));

        List<Prenotazione> list =
                prenotazioneDAO.findByVeicolo("T2");

        assertEquals(1, list.size());
        assertEquals("T2", list.get(0).getTarga());
    }

    // ============================================================
    // FIND BY STATO
    // ============================================================
    @Test
    void testFindByStato() {

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA,
                TipoPrenotazione.UTENTE,
                2,
                "AB123CD"
        ));

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                StatoPrenotazione.ATTIVA,
                TipoPrenotazione.MANUTENZIONE,
                1,
                "AB123CD"
        ));

        List<Prenotazione> list =
                prenotazioneDAO.findByStato(StatoPrenotazione.ATTIVA);

        assertEquals(2, list.size());
        assertTrue(list.stream()
                .allMatch(p -> p.getStato() == StatoPrenotazione.ATTIVA));
    }

    // ============================================================
    // EXISTS OVERLAPPING
    // ============================================================
    @Test
    void testExistsOverlapping() {

        LocalDateTime start = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 3, 1, 12, 0);

        prenotazioneDAO.save(new Prenotazione(
                0,
                start,
                end,
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "V3"
        ));

        boolean exists =
                prenotazioneDAO.existsOverlapping(
                        "V3",
                        LocalDateTime.of(2025, 3, 1, 11, 0),
                        LocalDateTime.of(2025, 3, 1, 13, 0)
                );

        assertTrue(exists);
    }

    // ============================================================
    // FIND ALL
    // ============================================================
    @Test
    void testFindAll() {

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.CONFERMATA,
                TipoPrenotazione.UTENTE,
                2,
                "AB123CD"
        ));

        prenotazioneDAO.save(new Prenotazione(
                0,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                StatoPrenotazione.ANNULLATA,
                TipoPrenotazione.UTENTE,
                2,
                "GH819RJ"
        ));

        List<Prenotazione> list =
                prenotazioneDAO.findAll();

        assertEquals(2, list.size());
    }
}
