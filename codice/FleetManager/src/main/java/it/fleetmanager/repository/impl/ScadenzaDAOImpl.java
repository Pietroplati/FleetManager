package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.dao.ScadenzaDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.TipoScadenza;

/**
 * Implementazione del DAO {@link ScadenzaDAO}.
 */
public class ScadenzaDAOImpl implements ScadenzaDAO {

	private static final Logger LOGGER = Logger.getLogger(ScadenzaDAOImpl.class.getName());

	private static final String ERRORE_SQL = "Errore SQL";

	private final ConnectionProvider db;

	/**
	 * Oggetto sentinella utilizzato quando una scadenza non esiste.
	 */
	public static final Scadenza SCADENZA_INESISTENTE = new Scadenza(-1, TipoScadenza.BOLLO, LocalDate.MIN, false,
			"N/A") {
		@Override
		public String toString() {
			return "Scadenza inesistente";
		}
	};

	/**
	 * Costruttore.
	 *
	 * @param db provider delle connessioni al database
	 */
	public ScadenzaDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	/*
	 * ===================================================== Mapping
	 * =====================================================
	 */

	/**
	 * Mappa una riga del {@link ResultSet} in un oggetto {@link Scadenza}.
	 *
	 * @param rs result set posizionato sulla riga corrente
	 * @return scadenza mappata
	 * @throws SQLException in caso di errore SQL
	 */
	private Scadenza map(ResultSet rs) throws SQLException {

		int id = rs.getInt("idScadenza");
		TipoScadenza tipo = TipoScadenza.valueOf(rs.getString("tipoScadenza"));
		LocalDate data = rs.getDate("data").toLocalDate();
		boolean notificata = rs.getBoolean("notificata");
		String targa = rs.getString("targa");

		return new Scadenza(id, tipo, data, notificata, targa);
	}

	/**
	 * Estrae una singola scadenza dal {@link ResultSet}.
	 *
	 * @param rs result set
	 * @return scadenza trovata o oggetto sentinella
	 * @throws SQLException in caso di errore SQL
	 */
	private Scadenza extractOne(ResultSet rs) throws SQLException {
		return rs.next() ? map(rs) : SCADENZA_INESISTENTE;
	}

	/**
	 * Riempie una lista di scadenze a partire da un {@link ResultSet}.
	 *
	 * @param rs  result set
	 * @param out lista di destinazione
	 * @throws SQLException in caso di errore SQL
	 */
	private void fillList(ResultSet rs, List<Scadenza> out) throws SQLException {

		while (rs.next()) {
			out.add(map(rs));
		}
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
			ps.setDate(3, Date.valueOf(s.getData()));
			ps.setBoolean(4, s.getNotificata());
			ps.setString(5, s.getTarga());

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante save()", e);
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
			ps.setDate(2, Date.valueOf(s.getData()));
			ps.setBoolean(3, s.getNotificata());
			ps.setString(4, s.getTarga());
			ps.setInt(5, s.getIdScadenza());

			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante update()", e);
		}
	}

	@Override
	public void delete(int id) {

		String sql = "DELETE FROM Scadenza WHERE idScadenza = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante delete()", e);
		}
	}

	@Override
	public Scadenza getScadenzaById(int idScadenza) {

		String sql = """
				SELECT idScadenza, tipoScadenza, data, notificata, targa
				FROM Scadenza
				WHERE idScadenza = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs);
			}

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante getScadenzaById()", e);
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, Date.valueOf(finoA));

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, lista);
			}

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante findProssimeScadenze()", e);
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, lista);
			}

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante findByVeicolo()", e);
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

			fillList(rs, lista);

		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, ERRORE_SQL + " durante getTutteScadenze()", e);
		}

		return lista;
	}
}
