package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.TipoScadenza;

public class ScadenzaDAOImpl implements ScadenzaDAO {

    private static final java.util.logging.Logger LOGGER =
            java.util.logging.Logger.getLogger(ScadenzaDAOImpl.class.getName());

    private final ConnectionProvider db;

    public static final Scadenza SCADENZA_INESISTENTE =
            new Scadenza(-1, TipoScadenza.BOLLO, LocalDate.MIN, false, "N/A") {
                @Override
                public String toString() {
                    return "Scadenza inesistente";
                }
            };

    public ScadenzaDAOImpl(ConnectionProvider db) {
        this.db = db;
    }

    private Scadenza map(ResultSet rs) throws SQLException {
        int id = rs.getInt("idScadenza");
        TipoScadenza tipo = TipoScadenza.valueOf(rs.getString("tipoScadenza"));
        LocalDate data = rs.getDate("data").toLocalDate();
        boolean notificata = rs.getBoolean("notificata");
        String targa = rs.getString("targa");
        return new Scadenza(id, tipo, data, notificata, targa);
    }

    @Override
    public void save(Scadenza s) {
        String sql = """
                INSERT INTO Scadenza
                (idScadenza, tipoScadenza, data, notificata, targa)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, s.getIdScadenza());
            ps.setString(2, s.getTipoScadenza().name());
            ps.setDate(3, java.sql.Date.valueOf(s.getData()));
            ps.setBoolean(4, s.getNotificata());
            ps.setString(5, s.getTarga());

            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL save Scadenza", e);
        }
    }

    @Override
    public void update(Scadenza s) {
        String sql = """
                UPDATE Scadenza SET
                    tipoScadenza = ?,
                    data = ?,
                    notificata = ?,
                    targa = ?
                WHERE idScadenza = ?
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getTipoScadenza().name());
            ps.setDate(2, java.sql.Date.valueOf(s.getData()));
            ps.setBoolean(3, s.getNotificata());
            ps.setString(4, s.getTarga());
            ps.setInt(5, s.getIdScadenza());

            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL update Scadenza", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Scadenza WHERE idScadenza = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL delete Scadenza", e);
        }
    }

    @Override
    public Scadenza getScadenzaById(int idScadenza) {
        String sql = """
                SELECT idScadenza, tipoScadenza, data, notificata, targa
                FROM Scadenza
                WHERE idScadenza = ?
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idScadenza);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL getScadenzaById", e);
        }

        return SCADENZA_INESISTENTE;
    }

    @Override
    public List<Scadenza> findProssimeScadenze(LocalDate finoA) {
        String sql = """
                SELECT idScadenza, tipoScadenza, data, notificata, targa
                FROM Scadenza
                WHERE data >= CURRENT_DATE AND data <= ?
                ORDER BY data ASC
                """;

        List<Scadenza> lista = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(finoA));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL findProssimeScadenze", e);
        }

        return lista;
    }

    @Override
    public List<Scadenza> findByVeicolo(String targa) {
        String sql = """
                SELECT idScadenza, tipoScadenza, data, notificata, targa
                FROM Scadenza
                WHERE targa = ?
                ORDER BY data ASC
                """;

        List<Scadenza> lista = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(map(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL findByVeicolo", e);
        }

        return lista;
    }

    @Override
    public List<Scadenza> getTutteScadenze() {
        String sql = """
                SELECT idScadenza, tipoScadenza, data, notificata, targa
                FROM Scadenza
                ORDER BY data ASC
                """;

        List<Scadenza> lista = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(map(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Errore SQL getTutteScadenze", e);
        }

        return lista;
    }
}
