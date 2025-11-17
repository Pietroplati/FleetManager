package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.impl.PrenotazioneDAOImpl;
import it.fleetmanager.util.StatoPrenotazione;

public class PrenotazioneDAOImplTest {

    private PrenotazioneDAOImpl dao;

    @BeforeEach
    void setup() throws Exception {

        //======== Usa un database H2 in-memory separato ========
        DatabaseManager.setTestUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");

        //======== Crea la tabella PRENOTAZIONE nel DB di test ========
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
             Statement st = conn.createStatement()) {

            st.execute("""
                    CREATE TABLE IF NOT EXISTS Prenotazione (
                        idPrenotazione INT PRIMARY KEY,
                        dataInizio TIMESTAMP,
                        dataFine TIMESTAMP,
                        stato VARCHAR(50),
                        idUtente INT,
                        targa VARCHAR(20)
                    );
                """);

            // Pulisce la tabella prima di ogni test
            st.execute("DELETE FROM Prenotazione");
        }

        dao = new PrenotazioneDAOImpl();
    }

    // ======================================================
    // TEST SAVE + GETBYID
    // ======================================================
    @Test
    void testSaveAndGetById() {

        Prenotazione p = new Prenotazione(
                1,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                StatoPrenotazione.ATTIVA,
                99,
                "AB123CD"
        );

        dao.save(p);

        Prenotazione letta = dao.getById(1);

        assertNotNull(letta);
        assertEquals(1, letta.getIdPrenotazione());
        assertEquals("AB123CD", letta.getTarga());
        assertEquals(99, letta.getIdUtente());
    }

    // ======================================================
    // TEST UPDATE
    // ======================================================
    @Test
    void testUpdate() {

        Prenotazione p = new Prenotazione(
                2,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA,
                50,
                "BB002CC"
        );

        dao.save(p);

        // Modifico la prenotazione
        p.setDataFine(LocalDateTime.now().plusHours(4));
        p.setStato(StatoPrenotazione.COMPLETATA);

        dao.update(p);

        Prenotazione aggiornata = dao.getById(2);

        assertEquals(p.getDataFine(), aggiornata.getDataFine());
        assertEquals(StatoPrenotazione.COMPLETATA, aggiornata.getStato());
    }

    // ======================================================
    // TEST FINDBYDRIVER
    // ======================================================
    @Test
    void testFindByDriver() {

        dao.save(new Prenotazione(3, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA, 10, "AAA111"));

        dao.save(new Prenotazione(4, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA, 10, "BBB222"));

        List<Prenotazione> lista = dao.findByDriver(10);

        assertEquals(2, lista.size());
    }

    // ======================================================
    // TEST FINDBYVEICOLO
    // ======================================================
    @Test
    void testFindByVeicolo() {

        dao.save(new Prenotazione(5, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA, 30, "GP251YD"));

        dao.save(new Prenotazione(6, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA, 40, "GP251YD"));

        List<Prenotazione> lista = dao.findByVeicolo("GP251YD");

        assertEquals(2, lista.size());
    }

    // ======================================================
    // TEST FINDATTIVE
    // ======================================================
    @Test
    void testFindAttive() {

        dao.save(new Prenotazione(7, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA, 1, "GP251YD"));

        dao.save(new Prenotazione(8, LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                StatoPrenotazione.COMPLETATA, 1, "GP251YD"));

        List<Prenotazione> attive = dao.findAttive();

        assertEquals(1, attive.size());
    }

    // ======================================================
    // TEST EXISTSOVERLAPPING
    // ======================================================
    @Test
    void testExistsOverlapping() {

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);

        dao.save(new Prenotazione(9, start, end,
                StatoPrenotazione.ATTIVA, 99, "CAR_OVER"));

        boolean overlap = dao.existsOverlapping(
                "CAR_OVER",
                start.plusMinutes(10),
                start.plusHours(1)
        );

        assertTrue(overlap);
    }

    // ======================================================
    // TEST DELETE
    // ======================================================
    @Test
    void testDelete() {

        dao.save(new Prenotazione(
                10,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                StatoPrenotazione.ATTIVA,
                12,
                "GP251YD"
        ));

        dao.delete(10);

        assertNull(dao.getById(10));
    }
}
