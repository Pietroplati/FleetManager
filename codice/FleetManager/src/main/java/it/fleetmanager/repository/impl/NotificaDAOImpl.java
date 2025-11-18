package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImpl implements NotificaDAO {

	public static final Notifica NOTIFICA_INESISTENTE = new Notifica(-1, TipoNotifica.PRENOTAZIONE, "N/A",
			LocalDateTime.MIN, false, -1, null) {
		@Override
		public String toString() {
			return "Notifica inesistente";
		}
	};

	private Notifica mapNotificaDaResultSet(ResultSet rs) throws Exception {

		int id = rs.getInt("idNotifica");
		TipoNotifica tipo = TipoNotifica.valueOf(rs.getString("tipoNotifica"));
		String messaggio = rs.getString("messaggio");
		LocalDateTime dataInvio = rs.getTimestamp("dataInvio").toLocalDateTime();
		boolean letta = rs.getBoolean("letta");
		int idUtente = rs.getInt("idUtente");

		// Campo nullable → va letto con getObject()
		Integer idScadenza = rs.getObject("idScadenza", Integer.class);

		return new Notifica(id, tipo, messaggio, dataInvio, letta, idUtente, idScadenza);
	}

	@Override
	public void save(Notifica notifica) {

		String sql = """
				INSERT INTO Notifica
				(idNotifica, tipoNotifica, messaggio, dataInvio, letta, idUtente, idScadenza)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, notifica.getIdNotifica());
			ps.setString(2, notifica.getTipoNotifica().name());
			ps.setString(3, notifica.getMessaggio());
			ps.setTimestamp(4, java.sql.Timestamp.valueOf(notifica.getDataInvio()));
			ps.setBoolean(5, notifica.getLetta());
			ps.setInt(6, notifica.getIdUtente());

			if (notifica.getIdScadenza() != null) {
				ps.setInt(7, notifica.getIdScadenza());
			} else {
				ps.setNull(7, Types.INTEGER);
			}

			ps.executeUpdate();
			System.out.println("Notifica inserita correttamente!");

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante save(notifica): " + e.getMessage());
		}
	}

	@Override
	public void update(Notifica notifica) {

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

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, notifica.getTipoNotifica().name());
			ps.setString(2, notifica.getMessaggio());
			ps.setTimestamp(3, java.sql.Timestamp.valueOf(notifica.getDataInvio()));
			ps.setBoolean(4, notifica.getLetta());
			ps.setInt(5, notifica.getIdUtente());

			if (notifica.getIdScadenza() != null) {
				ps.setInt(6, notifica.getIdScadenza());
			} else {
				ps.setNull(6, Types.INTEGER);
			}

			ps.setInt(7, notifica.getIdNotifica());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Notifica aggiornata (ID: " + notifica.getIdNotifica() + ")");
			} else {
				System.err.println("Nessuna notifica trovata con ID " + notifica.getIdNotifica());
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante update(notifica): " + e.getMessage());
		}
	}

	@Override
	public void delete(int idNotifica) {

		String sql = "DELETE FROM Notifica WHERE idNotifica = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);
			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Notifica eliminata (ID: " + idNotifica + ")");
			} else {
				System.err.println("Nessuna notifica trovata con ID " + idNotifica);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante delete(notifica): " + e.getMessage());
		}
	}

	@Override
	public Notifica getNotificaById(int idNotifica) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idNotifica = ?
				""";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return mapNotificaDaResultSet(rs);
				}
			}

			System.err.println("Nessuna notifica trovata con ID " + idNotifica);
			return NOTIFICA_INESISTENTE;

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante getNotificaById: " + e.getMessage());
			return NOTIFICA_INESISTENTE;
		}
	}

	@Override
	public List<Notifica> findByUtente(int idUtente) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idUtente = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> lista = new java.util.ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapNotificaDaResultSet(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByUtente: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Notifica> findNonLette(int idUtente) {

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idUtente = ? AND letta = FALSE
				ORDER BY dataInvio ASC
				""";

		List<Notifica> lista = new java.util.ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapNotificaDaResultSet(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findNonLette: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Notifica> findByScadenza(Integer idScadenza) {

		if (idScadenza == null)
			return java.util.Collections.emptyList();

		String sql = """
				SELECT *
				FROM Notifica
				WHERE idScadenza = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> lista = new java.util.ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapNotificaDaResultSet(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByScadenza: " + e.getMessage());
		}

		return lista;
	}

}
