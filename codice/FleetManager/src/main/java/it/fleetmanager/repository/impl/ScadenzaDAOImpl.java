package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.ScadenzaDAO;
import it.fleetmanager.util.TipoScadenza;

public class ScadenzaDAOImpl implements ScadenzaDAO {

	private final DatabaseManager db;

	public static final Scadenza SCADENZA_INESISTENTE = new Scadenza(-1, TipoScadenza.BOLLO, LocalDate.MIN, false,
			"N/A") {
		@Override
		public String toString() {
			return "Scadenza inesistente";
		}
	};

	public ScadenzaDAOImpl(DatabaseManager db) {
		this.db = db;
	}

	private Scadenza map(ResultSet rs) throws Exception {
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, s.getIdScadenza());
			ps.setString(2, s.getTipoScadenza().name());
			ps.setDate(3, java.sql.Date.valueOf(s.getData()));
			ps.setBoolean(4, s.getNotificata());
			ps.setString(5, s.getTarga());

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL save: " + e.getMessage());
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, s.getTipoScadenza().name());
			ps.setDate(2, java.sql.Date.valueOf(s.getData()));
			ps.setBoolean(3, s.getNotificata());
			ps.setString(4, s.getTarga());
			ps.setInt(5, s.getIdScadenza());

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL update: " + e.getMessage());
		}
	}

	@Override
	public void delete(int id) {
		String sql = "DELETE FROM Scadenza WHERE idScadenza = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL delete: " + e.getMessage());
		}
	}

	@Override
	public Scadenza getScadenzaById(int idScadenza) {
		String sql = "SELECT * FROM Scadenza WHERE idScadenza = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return SCADENZA_INESISTENTE;
				return map(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL getScadenzaById: " + e.getMessage());
			return SCADENZA_INESISTENTE;
		}
	}

	@Override
	public List<Scadenza> findProssimeScadenze(LocalDate finoA) {
		String sql = """
				SELECT * FROM Scadenza
				WHERE data >= CURRENT_DATE AND data <= ?
				ORDER BY data ASC
				""";

		List<Scadenza> lista = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, java.sql.Date.valueOf(finoA));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					lista.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL findProssimeScadenze: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Scadenza> findByVeicolo(String targa) {
		String sql = "SELECT * FROM Scadenza WHERE targa = ? ORDER BY data ASC";
		List<Scadenza> lista = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					lista.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL findByVeicolo: " + e.getMessage());
		}

		return lista;
	}
}
