package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.dao.ManutenzioneDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.TipoManutenzione;

/**
 * Implementazione del DAO {@link ManutenzioneDAO}.
 */
public class ManutenzioneDAOImpl implements ManutenzioneDAO {

	private static final Logger LOGGER = LogManager.getLogger(ManutenzioneDAOImpl.class);

	private static final String ERRORE_SQL = "ERRORE SQL";

	private final ConnectionProvider db;

	/**
	 * Oggetto sentinella utilizzato quando una manutenzione non esiste.
	 */
	public static final Manutenzione MANUTENZIONE_INESISTENTE = new Manutenzione(-1, LocalDateTime.MIN,
			TipoManutenzione.ORDINARIA, "N/A", "N/A") {
		@Override
		public String toString() {
			return "Manutenzione inesistente";
		}
	};

	/**
	 * Costruttore.
	 *
	 * @param db provider delle connessioni al database
	 */
	public ManutenzioneDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	/**
	 * Mappa una riga del {@link ResultSet} in un oggetto {@link Manutenzione}.
	 *
	 * @param rs result set posizionato sulla riga corrente
	 * @return manutenzione mappata
	 * @throws SQLException in caso di errore SQL
	 */
	private Manutenzione map(ResultSet rs) throws SQLException {
		int id = rs.getInt("idManutenzione");
		LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
		TipoManutenzione tipo = TipoManutenzione.valueOf(rs.getString("tipoManutenzione"));
		String descrizione = rs.getString("descrizione");
		String targa = rs.getString("targa");

		return new Manutenzione(id, data, tipo, descrizione, targa);
	}

	/**
	 * Estrae una singola manutenzione dal {@link ResultSet}.
	 *
	 * @param rs result set
	 * @return manutenzione trovata o oggetto sentinella
	 * @throws SQLException in caso di errore SQL
	 */
	private Manutenzione extractOne(ResultSet rs) throws SQLException {
		return rs.next() ? map(rs) : MANUTENZIONE_INESISTENTE;
	}

	/**
	 * Riempie una lista di manutenzioni a partire da un {@link ResultSet}.
	 *
	 * @param rs  result set
	 * @param out lista di destinazione
	 * @throws SQLException in caso di errore SQL
	 */
	private void fillList(ResultSet rs, List<Manutenzione> out) throws SQLException {

		while (rs.next()) {
			out.add(map(rs));
		}
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
			ps.setTimestamp(2, Timestamp.valueOf(m.getData()));
			ps.setString(3, m.getTipoManutenzione().name());
			ps.setString(4, m.getDescrizione());
			ps.setString(5, m.getTarga());

			ps.executeUpdate();

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante save(): {}", ERRORE_SQL, e.getMessage(), e);
			}
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

			ps.setTimestamp(1, Timestamp.valueOf(m.getData()));
			ps.setString(2, m.getTipoManutenzione().name());
			ps.setString(3, m.getDescrizione());
			ps.setString(4, m.getTarga());
			ps.setInt(5, m.getIdManutenzione());

			ps.executeUpdate();

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante update(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}
	}

	@Override
	public void delete(int id) {

		String sql = "DELETE FROM Manutenzione WHERE idManutenzione = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante delete(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}
	}

	@Override
	public Manutenzione getManutenzioneById(int id) {

		String sql = """
				SELECT idManutenzione, data, tipoManutenzione,
				       descrizione, targa
				FROM Manutenzione
				WHERE idManutenzione = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs);
			}

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante getManutenzioneById(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}

		return MANUTENZIONE_INESISTENTE;
	}

	@Override
	public List<Manutenzione> findByVeicolo(String targa) {

		String sql = """
				SELECT idManutenzione, data, tipoManutenzione,
				       descrizione, targa
				FROM Manutenzione
				WHERE targa = ?
				ORDER BY data ASC
				""";

		List<Manutenzione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante findByVeicolo(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}

		return list;
	}

	@Override
	public List<Manutenzione> findByTipo(TipoManutenzione tipo) {

		String sql = """
				SELECT idManutenzione, data, tipoManutenzione,
				       descrizione, targa
				FROM Manutenzione
				WHERE tipoManutenzione = ?
				ORDER BY data ASC
				""";

		List<Manutenzione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, tipo.name());

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante findByTipo(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}

		return list;
	}

	@Override
	public int getMaxId() {

		String sql = "SELECT COALESCE(MAX(idManutenzione), 0) FROM Manutenzione";

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			return rs.next() ? rs.getInt(1) : 0;

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante getMaxId(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}

		return 0;
	}

	@Override
	public List<Manutenzione> getTutteManutenzioni() {

		String sql = """
				SELECT idManutenzione, data, tipoManutenzione,
				       descrizione, targa
				FROM Manutenzione
				ORDER BY data ASC
				""";

		List<Manutenzione> list = new ArrayList<>();

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			fillList(rs, list);

		} catch (SQLException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("{} durante getTutteManutenzioni(): {}", ERRORE_SQL, e.getMessage(), e);
			}
		}

		return list;
	}
}
