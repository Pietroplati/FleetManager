package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.TipoManutenzione;

public class ManutenzioneDAOImpl implements ManutenzioneDAO {

    private static final Logger logger = LogManager.getLogger(ManutenzioneDAOImpl.class);

    private final ConnectionProvider db;

    public static final Manutenzione MANUTENZIONE_INESISTENTE = new Manutenzione(-1, LocalDateTime.MIN,
            TipoManutenzione.ORDINARIA, "N/A", "N/A") {
        @Override
        public String toString() {
            return "Manutenzione inesistente";
        }
    };

    public ManutenzioneDAOImpl(ConnectionProvider db) {
        this.db = db;
    }

    private Manutenzione map(ResultSet rs) throws SQLException {
        int id = rs.getInt("idManutenzione");
        LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
        TipoManutenzione tipo = TipoManutenzione.valueOf(rs.getString("tipoManutenzione"));
        String descrizione = rs.getString("descrizione");
        String targa = rs.getString("targa");

        return new Manutenzione(id, data, tipo, descrizione, targa);
    }

    @Override
    public void save(Manutenzione m) {
        String sql = """
                INSERT INTO Manutenzione
                (idManutenzione, data, tipoManutenzione, descrizione, targa)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, m.getIdManutenzione());
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(m.getData()));
            ps.setString(3, m.getTipoManutenzione().name());
            ps.setString(4, m.getDescrizione());
            ps.setString(5, m.getTarga());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante save(): {}", e.getMessage(), e);
        }
    }

    @Override
    public void update(Manutenzione m) {
        String sql = """
                UPDATE Manutenzione SET
                    data = ?,
                    tipoManutenzione = ?,
                    descrizione = ?,
                    targa = ?
                WHERE idManutenzione = ?
                """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(m.getData()));
            ps.setString(2, m.getTipoManutenzione().name());
            ps.setString(3, m.getDescrizione());
            ps.setString(4, m.getTarga());
            ps.setInt(5, m.getIdManutenzione());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante update(): {}", e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {

        String sql = "DELETE FROM Manutenzione WHERE idManutenzione = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante delete(): {}", e.getMessage(), e);
        }
    }

    @Override
    public Manutenzione getManutenzioneById(int id) {

        String sql = """
                SELECT idManutenzione, data, tipoManutenzione, descrizione, targa
                FROM Manutenzione
                WHERE idManutenzione = ?
                """;

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante getManutenzioneById(): {}", e.getMessage(), e);
        }

        return MANUTENZIONE_INESISTENTE;
    }

    @Override
    public List<Manutenzione> findByVeicolo(String targa) {

        String sql = """
                SELECT idManutenzione, data, tipoManutenzione, descrizione, targa
                FROM Manutenzione
                WHERE targa = ?
                ORDER BY data ASC
                """;

        List<Manutenzione> list = new ArrayList<>();

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante findByVeicolo(): {}", e.getMessage(), e);
        }

        return list;
    }

    @Override
    public List<Manutenzione> findByTipo(TipoManutenzione tipo) {

        String sql = """
                SELECT idManutenzione, data, tipoManutenzione, descrizione, targa
                FROM Manutenzione
                WHERE tipoManutenzione = ?
                ORDER BY data ASC
                """;

        List<Manutenzione> list = new ArrayList<>();

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(map(rs));
            }

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante findByTipo(): {}", e.getMessage(), e);
        }

        return list;
    }

    @Override
    public int getMaxId() {

        String sql = "SELECT COALESCE(MAX(idManutenzione), 0) FROM Manutenzione";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante getMaxId(): {}", e.getMessage(), e);
        }

        return 0;
    }

    @Override
    public List<Manutenzione> getTutteManutenzioni() {

        String sql = """
                SELECT idManutenzione, data, tipoManutenzione, descrizione, targa
                FROM Manutenzione
                ORDER BY data ASC
                """;

        List<Manutenzione> list = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (SQLException e) {
            logger.error("ERRORE SQL durante getTutteManutenzioni(): {}", e.getMessage(), e);
        }

        return list;
    }
}
