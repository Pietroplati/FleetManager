package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.NotificaDAO;
import it.fleetmanager.util.TipoNotifica;

public class NotificaDAOImpl implements NotificaDAO {

	public static final Notifica NOTIFICA_INESISTENTE = new Notifica(-1, TipoNotifica.PRENOTAZIONE, "N/A",
			LocalDateTime.MIN, false, -1, -1) {
		@Override
		public String toString() {
			return "Notifica inesistente";
		}
	};

	@Override
	public void save(Notifica notifica) {

		String sql = "INSERT INTO Notifica "
				+ "(idNotifica, tipoNotifica, messaggio, dataInvio, letta, idUtente, idScadenza) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, notifica.getIdNotifica());
			ps.setString(2, notifica.getTipoNotifica().name());
			ps.setString(3, notifica.getMessaggio());
			ps.setTimestamp(4, java.sql.Timestamp.valueOf(notifica.getDataInvio()));
			ps.setBoolean(5, notifica.getLetta());
			ps.setInt(6, notifica.getIdUtente());
			ps.setInt(7, notifica.getIdScadenza());

			ps.executeUpdate();
			System.out.println("✔ Notifica inserita correttamente nel database H2!");

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante save(notifica): " + e.getMessage());
		}
	}

	@Override
	public void update(Notifica notifica) {

		String sql = "UPDATE Notifica SET " + "tipoNotifica = ?, " + "messaggio = ?, " + "dataInvio = ?, "
				+ "letta = ?, " + "idUtente = ?, " + "idScadenza = ? " + "WHERE idNotifica = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, notifica.getTipoNotifica().name());
			ps.setString(2, notifica.getMessaggio());
			ps.setTimestamp(3, java.sql.Timestamp.valueOf(notifica.getDataInvio()));
			ps.setBoolean(4, notifica.getLetta());
			ps.setInt(5, notifica.getIdUtente());
			ps.setInt(6, notifica.getIdScadenza());
			ps.setInt(7, notifica.getIdNotifica());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("✔ Notifica aggiornata (ID: " + notifica.getIdNotifica() + ")");
			} else {
				System.err.println("⚠ Nessuna notifica trovata con ID " + notifica.getIdNotifica());
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
				System.out.println("✔ Notifica eliminata (ID: " + idNotifica + ")");
			} else {
				System.err.println("⚠ Nessuna notifica trovata con ID " + idNotifica);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante delete(notifica): " + e.getMessage());
		}
	}

	@Override
	public Notifica getNotificaById(int idNotifica) {

		String sql = "SELECT idNotifica, tipoNotifica, messaggio, dataInvio, letta, idUtente, idScadenza "
				+ "FROM Notifica WHERE idNotifica = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					System.err.println("⚠ Nessuna notifica trovata con ID " + idNotifica);
					return NOTIFICA_INESISTENTE;
				}

				return creaNotificaDaResultSet(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante getNotificaById: " + e.getMessage());
			return NOTIFICA_INESISTENTE;
		}
	}

	@Override
	public Notifica findByUtente(int idUtente) {

		String sql = "SELECT * FROM Notifica WHERE idUtente = ? ORDER BY dataInvio DESC LIMIT 1";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return creaNotificaDaResultSet(rs);
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByUtente: " + e.getMessage());
		}

		return NOTIFICA_INESISTENTE;
	}

	@Override
	public Notifica findNonLette(int idUtente) {

		String sql = "SELECT * FROM Notifica WHERE idUtente = ? AND letta = false ORDER BY dataInvio ASC LIMIT 1";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return creaNotificaDaResultSet(rs);
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findNonLette: " + e.getMessage());
		}

		return NOTIFICA_INESISTENTE;
	}

	@Override
	public Notifica findByScadenza(int idScadenza) {

		String sql = "SELECT * FROM Notifica WHERE idScadenza = ? ORDER BY dataInvio DESC LIMIT 1";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return creaNotificaDaResultSet(rs);
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByScadenza: " + e.getMessage());
		}

		return NOTIFICA_INESISTENTE;
	}

	private Notifica creaNotificaDaResultSet(ResultSet rs) throws Exception {

		int id = rs.getInt("idNotifica");
		TipoNotifica tipo = TipoNotifica.valueOf(rs.getString("tipoNotifica"));
		String messaggio = rs.getString("messaggio");
		LocalDateTime dataInvio = rs.getTimestamp("dataInvio").toLocalDateTime();
		boolean letta = rs.getBoolean("letta");
		int idUtente = rs.getInt("idUtente");
		int idScadenza = rs.getInt("idScadenza");

		return new Notifica(id, tipo, messaggio, dataInvio, letta, idUtente, idScadenza);
	}
}
