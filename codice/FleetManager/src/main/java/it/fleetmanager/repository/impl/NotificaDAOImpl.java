package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.TipoNotifica;

/**
 * Implementazione del DAO {@link NotificaDAO}.
 */
public class NotificaDAOImpl implements NotificaDAO {

	private static final Logger logger = LogManager.getLogger(NotificaDAOImpl.class);

	private static final String ERRORE_SQL = "ERRORE SQL";

	private final ConnectionProvider db;

	/**
	 * Oggetto sentinella utilizzato quando una notifica non esiste.
	 */
	public static final Notifica NOTIFICA_INESISTENTE = new Notifica(-1, TipoNotifica.PRENOTAZIONE, "N/A",
			LocalDateTime.MIN, false, -1, null) {
		@Override
		public String toString() {
			return "Notifica inesistente";
		}
	};

	/**
	 * Costruttore.
	 *
	 * @param db provider delle connessioni al database
	 */
	public NotificaDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	/*
	 * ===================================================== Mapping
	 * =====================================================
	 */

	/**
	 * Mappa una riga del {@link ResultSet} in un oggetto {@link Notifica}.
	 *
	 * @param rs result set posizionato sulla riga corrente
	 * @return notifica mappata
	 * @throws SQLException in caso di errore SQL
	 */
	private Notifica map(ResultSet rs) throws SQLException {

		int id = rs.getInt("idNotifica");
		TipoNotifica tipo = TipoNotifica.valueOf(rs.getString("tipoNotifica"));
		String messaggio = rs.getString("messaggio");
		LocalDateTime dataInvio = rs.getTimestamp("dataInvio").toLocalDateTime();
		boolean letta = rs.getBoolean("letta");
		int idUtente = rs.getInt("idUtente");
		Integer idScadenza = rs.getObject("idScadenza", Integer.class);

		return new Notifica(id, tipo, messaggio, dataInvio, letta, idUtente, idScadenza);
	}

	/**
	 * Estrae una singola notifica dal {@link ResultSet}.
	 *
	 * @param rs result set
	 * @return notifica trovata o oggetto sentinella
	 * @throws SQLException in caso di errore SQL
	 */
	private Notifica extractOne(ResultSet rs) throws SQLException {
		return rs.next() ? map(rs) : NOTIFICA_INESISTENTE;
	}

	/**
	 * Riempie una lista di notifiche a partire da un {@link ResultSet}.
	 *
	 * @param rs  result set
	 * @param out lista di destinazione
	 * @throws SQLException in caso di errore SQL
	 */
	private void fillList(ResultSet rs, List<Notifica> out) throws SQLException {

		while (rs.next()) {
			out.add(map(rs));
		}
	}

	@Override
	public void save(Notifica n) {

		String sql = """
				INSERT INTO Notifica
				(tipoNotifica, messaggio, dataInvio,
				 letta, idUtente, idScadenza)
				VALUES (?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, n.getTipoNotifica().name());
			ps.setString(2, n.getMessaggio());
			ps.setTimestamp(3, Timestamp.valueOf(n.getDataInvio()));
			ps.setBoolean(4, n.getLetta());
			ps.setInt(5, n.getIdUtente());

			if (n.getIdScadenza() != null) {
				ps.setInt(6, n.getIdScadenza());
			} else {
				ps.setNull(6, Types.INTEGER);
			}

			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante save(): {}", ERRORE_SQL, e.getMessage(), e);
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
			ps.setTimestamp(3, Timestamp.valueOf(n.getDataInvio()));
			ps.setBoolean(4, n.getLetta());
			ps.setInt(5, n.getIdUtente());

			if (n.getIdScadenza() != null) {
				ps.setInt(6, n.getIdScadenza());
			} else {
				ps.setNull(6, Types.INTEGER);
			}

			ps.setInt(7, n.getIdNotifica());
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante update(): {}", ERRORE_SQL, e.getMessage(), e);
		}
	}

	@Override
	public void delete(int idNotifica) {

		String sql = "DELETE FROM Notifica WHERE idNotifica = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante delete(): {}", ERRORE_SQL, e.getMessage(), e);
		}
	}

	@Override
	public Notifica getNotificaById(int idNotifica) {

		String sql = """
				SELECT idNotifica, tipoNotifica, messaggio,
				       dataInvio, letta, idUtente, idScadenza
				FROM Notifica
				WHERE idNotifica = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idNotifica);

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs);
			}

		} catch (SQLException e) {
			logger.error("{} durante getNotificaById(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return NOTIFICA_INESISTENTE;
	}

	@Override
	public List<Notifica> findByUtente(int idUtente) {

		String sql = """
				SELECT idNotifica, tipoNotifica, messaggio,
				       dataInvio, letta, idUtente, idScadenza
				FROM Notifica
				WHERE idUtente = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			logger.error("{} durante findByUtente(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return list;
	}

	@Override
	public List<Notifica> findNonLette(int idUtente) {

		String sql = """
				SELECT idNotifica, tipoNotifica, messaggio,
				       dataInvio, letta, idUtente, idScadenza
				FROM Notifica
				WHERE idUtente = ? AND letta = FALSE
				ORDER BY dataInvio ASC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			logger.error("{} durante findNonLette(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return list;
	}

	@Override
	public List<Notifica> findByScadenza(Integer idScadenza) {

		if (idScadenza == null) {
			return Collections.emptyList();
		}

		String sql = """
				SELECT idNotifica, tipoNotifica, messaggio,
				       dataInvio, letta, idUtente, idScadenza
				FROM Notifica
				WHERE idScadenza = ?
				ORDER BY dataInvio DESC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			logger.error("{} durante findByScadenza(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return list;
	}

	@Override
	public List<Notifica> findAll() {

		String sql = """
				SELECT idNotifica, tipoNotifica, messaggio,
				       dataInvio, letta, idUtente, idScadenza
				FROM Notifica
				ORDER BY dataInvio DESC
				""";

		List<Notifica> list = new ArrayList<>();

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			fillList(rs, list);

		} catch (SQLException e) {
			logger.error("{} durante findAll(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return list;
	}
}
