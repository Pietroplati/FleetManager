package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImpl implements NotificaDAO {

	private final DatabaseManager db;

	public static final Notifica NOTIFICA_INESISTENTE = new Notifica(-1, TipoNotifica.PRENOTAZIONE, "N/A",
			LocalDateTime.MIN, false, -1, null) {
		@Override
		public String toString() {
			return "Notifica inesistente";
		}
	};

	public NotificaDAOImpl(DatabaseManager db) {
		this.db = db;
	}

	private Notifica mapResultSet(ResultSet rs) throws Exception {

		int id = rs.getInt("idNotifica");
		TipoNotifica tipo = TipoNotifica.valueOf(rs.getString("tipoNotifica"));
		String messaggio = rs.getString("messaggio");
		LocalDateTime dataInvio = rs.getTimestamp("dataInvio").toLocalDateTime();
		boolean letta = rs.getBoolean("letta");
		int idUtente = rs.getInt("idUtente");

		Integer idScadenza = rs.getObject("idScadenza", Integer.class);

		return new Notifica(id, tipo, messaggio, dataInvio, letta, idUtente, idScadenza);
	}

	@Override
	public void save(Notifica n) {

		String sql = """
				INSERT INTO Notifica
				(idNotifica, tipoNotifica, messaggio, dataInvio, letta, idUtente, idScadenza)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, n.getIdNotifica());
			ps.setString(2, n.getTipoNotifica().name());
			ps.setString(3, n.getMessaggio());
			ps.setTimestamp(4, java.sql.Timestamp.valueOf(n.getDataInvio()));
			ps.setBoolean(5, n.getLetta());
			ps.setInt(6, n.getIdUtente());

			if (n.getIdScadenza() != null)
				ps.setInt(7, n.getIdScadenza());
			else
				ps.setNull(7, Types.INTEGER);

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL during save(): " + e.getMessage());
		}
	}

	@Override
	public void update(Notifica n) {

		String sql = """
				UPDATE Notifica SET
				    tipoNotifica = ?,
				    messaggio = ?,
				    dataInvio = ?,
				    letta = ?,
				    idUtente = ?,
				    idScadenza = ?
				WHERE idNotifica = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, n.getTipoNotifica().name());
			ps.setString(2, n.getMessaggio());
			ps.setTimestamp(3, java.sql.Timestamp.valueOf(n.getDataInvio()));
			ps.setBoolean(4, n.getLetta());
			ps.setInt(5, n.getIdUtente());

			if (n.getIdScadenza() != null)
				ps.setInt(6, n.getIdScadenza());
			else
				ps.setNull(6, Types.INTEGER);

			ps.setInt(7, n.getIdNotifica());

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL during update(): " + e.getMessage());
		}
	}

	@Override
	public void delete(int idNotifica) {

		String sql = "DELETE FROM Notifica WHERE idNotifica = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);
			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL during delete(): " + e.getMessage());
		}
	}

	@Override
	public Notifica getNotificaById(int idNotifica) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idNotifica = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapResultSet(rs);
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL during getNotificaById(): " + e.getMessage());
		}

		return NOTIFICA_INESISTENTE;
	}

	@Override
	public List<Notifica> findByUtente(int idUtente) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idUtente = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapResultSet(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL during findByUtente(): " + e.getMessage());
		}

		return list;
	}

	/*
	 * ---------------------------- FIND NON LETTE ----------------------------
	 */
	@Override
	public List<Notifica> findNonLette(int idUtente) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idUtente = ? AND letta = FALSE
				ORDER BY dataInvio ASC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapResultSet(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL during findNonLette(): " + e.getMessage());
		}

		return list;
	}

	@Override
	public List<Notifica> findByScadenza(Integer idScadenza) {

		if (idScadenza == null)
			return Collections.emptyList();

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idScadenza = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(mapResultSet(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL during findByScadenza(): " + e.getMessage());
		}

		return list;
	}
}
