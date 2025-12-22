package it.fleetmanager.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.impl.ManutenzioneDAOImpl;
import it.fleetmanager.util.DatabaseTestUtils;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.TipoManutenzione;

public class ManutenzioneDAOImplTest {

    private ManutenzioneDAO manutenzioneDAO;

    @BeforeEach
    void setup() throws Exception {
        // 🔹 DB H2 in RAM, schema identico a quello reale
        DatabaseTestUtils.resetDatabase();
        manutenzioneDAO = new ManutenzioneDAOImpl(H2DatabaseManager.getInstance());
    }

    @Test
    void testSaveAndGetById() {
        // Arrange
        Manutenzione m = new Manutenzione(
                1,
                LocalDateTime.of(2025, 1, 10, 9, 0),
                TipoManutenzione.ORDINARIA,
                "Cambio olio",
                "AB123CD"
        );

        // Act
        manutenzioneDAO.save(m);
        Manutenzione loaded = manutenzioneDAO.getManutenzioneById(1);

        // Assert
        assertEquals(1, loaded.getIdManutenzione());
        assertEquals(TipoManutenzione.ORDINARIA, loaded.getTipoManutenzione());
        assertEquals("Cambio olio", loaded.getDescrizione());
        assertEquals("AB123CD", loaded.getTarga());
        assertEquals(m.getData(), loaded.getData());
    }

    @Test
    void testGetManutenzioneById_NotFound() {
        Manutenzione m = manutenzioneDAO.getManutenzioneById(999);
        assertEquals(-1, m.getIdManutenzione());
    }

    @Test
    void testUpdate() {
        // Arrange
        Manutenzione m = new Manutenzione(
                2,
                LocalDateTime.of(2025, 2, 5, 10, 0),
                TipoManutenzione.STRAORDINARIA,
                "Freni",
                "GH819RJ"
        );
        manutenzioneDAO.save(m);

        // Act
        m.setDescrizione("Freni + dischi");
        manutenzioneDAO.update(m);

        Manutenzione updated = manutenzioneDAO.getManutenzioneById(2);

        // Assert
        assertEquals("Freni + dischi", updated.getDescrizione());
    }

    @Test
    void testDelete() {
        // Arrange
        Manutenzione m = new Manutenzione(
                3,
                LocalDateTime.now(),
                TipoManutenzione.REVISIONE,
                "Revisione periodica",
                "ZZ000AA"
        );
        manutenzioneDAO.save(m);

        // Act
        manutenzioneDAO.delete(3);

        // Assert
        Manutenzione deleted = manutenzioneDAO.getManutenzioneById(3);
        assertEquals(-1, deleted.getIdManutenzione());
    }

    @Test
    void testFindByVeicolo() {
        // Arrange
        manutenzioneDAO.save(new Manutenzione(
                4,
                LocalDateTime.of(2025, 3, 1, 9, 0),
                TipoManutenzione.ORDINARIA,
                "Tagliando",
                "T1"
        ));

        manutenzioneDAO.save(new Manutenzione(
                5,
                LocalDateTime.of(2025, 3, 5, 9, 0),
                TipoManutenzione.REVISIONE,
                "Revisione",
                "T1"
        ));

        // Act
        List<Manutenzione> list = manutenzioneDAO.findByVeicolo("T1");

        // Assert
        assertEquals(2, list.size());
        assertTrue(list.get(0).getData().isBefore(list.get(1).getData()));
    }

    @Test
    void testFindByTipo() {
        // Arrange
        manutenzioneDAO.save(new Manutenzione(
                6,
                LocalDateTime.now(),
                TipoManutenzione.ORDINARIA,
                "Filtro aria",
                "V1"
        ));

        manutenzioneDAO.save(new Manutenzione(
                7,
                LocalDateTime.now(),
                TipoManutenzione.ORDINARIA,
                "Filtro olio",
                "V2"
        ));

        // Act
        List<Manutenzione> list = manutenzioneDAO.findByTipo(TipoManutenzione.ORDINARIA);

        // Assert
        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(
                m -> m.getTipoManutenzione() == TipoManutenzione.ORDINARIA
        ));
    }

    @Test
    void testGetMaxId() {
        // Arrange
        manutenzioneDAO.save(new Manutenzione(
                10,
                LocalDateTime.now(),
                TipoManutenzione.ORDINARIA,
                "Test",
                "V3"
        ));

        // Act
        int maxId = manutenzioneDAO.getMaxId();

        // Assert
        assertEquals(10, maxId);
    }

    @Test
    void testGetTutteManutenzioni() {
        // Arrange
        manutenzioneDAO.save(new Manutenzione(
                11,
                LocalDateTime.of(2025, 1, 1, 8, 0),
                TipoManutenzione.ORDINARIA,
                "M1",
                "AB123CD"
        ));

        manutenzioneDAO.save(new Manutenzione(
                12,
                LocalDateTime.of(2025, 1, 2, 8, 0),
                TipoManutenzione.REVISIONE,
                "M2",
                "GH819RJ"
        ));

        // Act
        List<Manutenzione> list = manutenzioneDAO.getTutteManutenzioni();

        // Assert
        assertEquals(2, list.size());
        assertTrue(list.get(0).getData().isBefore(list.get(1).getData()));
    }
}
