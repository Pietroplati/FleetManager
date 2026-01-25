package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.RuoloUtente;

/**
 * Implementazione del DAO {@link UtenteDAO}.
 */
public class UtenteDAOImpl implements UtenteDAO {

	private static final Logger logger = LogManager.getLogger(UtenteDAOImpl.class);

	private static final String ERRORE_SQL = "ERRORE SQL";

	private final ConnectionProvider db;

	/**
	 * Oggetto sentinella utilizzato quando un utente non esiste.
	 */
	public static final Utente UTENTE_INESISTENTE = new Utente(-1, "N/A", "N/A", "N/A", "N/A", RuoloUtente.MANAGER) {
		@Override
		public String toString() {
			return "Utente inesistente";
		}
	};

	/**
	 * Costruttore.
	 *
	 * @param db provider delle connessioni al database
	 */
	public UtenteDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	/*
	 * ===================================================== Mapping
	 * =====================================================
	 */

	/**
	 * Mappa una riga del {@link ResultSet} in un oggetto {@link Utente}.
	 *
	 * @param rs result set posizionato sulla riga corrente
	 * @return utente mappato
	 * @throws SQLException in caso di errore SQL
	 */
	private Utente map(ResultSet rs) throws SQLException {
		int idUtente = rs.getInt("idUtente");
		String nome = rs.getString("nome");
		String cognome = rs.getString("cognome");
		String email = rs.getString("email");
		String password = rs.getString("password");
		RuoloUtente ruolo = RuoloUtente.valueOf(rs.getString("ruoloUtente"));
		String patente = rs.getString("patente");

		return (patente == null) ? new Utente(idUtente, nome, cognome, email, password, ruolo)
				: new Utente(idUtente, nome, cognome, email, password, ruolo, patente);
	}

	/**
	 * Estrae un singolo utente dal {@link ResultSet}.
	 *
	 * @param rs result set
	 * @return utente trovato o oggetto sentinella
	 * @throws SQLException in caso di errore SQL
	 */
	private Utente extractOne(ResultSet rs) throws SQLException {
		return rs.next() ? map(rs) : UTENTE_INESISTENTE;
	}

	/**
	 * Riempie una lista di utenti a partire da un {@link ResultSet}.
	 *
	 * @param rs  result set
	 * @param out lista di destinazione
	 * @throws SQLException in caso di errore SQL
	 */
	private void fillList(ResultSet rs, List<Utente> out) throws SQLException {

		while (rs.next()) {
			out.add(map(rs));
		}
	}

	@Override
	public void save(Utente u) {

		String sql = """
				INSERT INTO Utente
				(idUtente, nome, cognome, email, password, ruoloUtente, patente)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, u.getIdUtente());
			ps.setString(2, u.getNome());
			ps.setString(3, u.getCognome());
			ps.setString(4, u.getEmail());
			ps.setString(5, u.getPassword());
			ps.setString(6, u.getRuoloUtente().name());
			ps.setString(7, u.getPatente());

			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante save(): {}", ERRORE_SQL, e.getMessage(), e);
		}
	}

	@Override
	public void update(Utente u) {

		String sql = """
				UPDATE Utente SET
				    nome = ?,
				    cognome = ?,
				    email = ?,
				    password = ?,
				    ruoloUtente = ?,
				    patente = ?
				WHERE idUtente = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, u.getNome());
			ps.setString(2, u.getCognome());
			ps.setString(3, u.getEmail());
			ps.setString(4, u.getPassword());
			ps.setString(5, u.getRuoloUtente().name());
			ps.setString(6, u.getPatente());
			ps.setInt(7, u.getIdUtente());

			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante update(): {}", ERRORE_SQL, e.getMessage(), e);
		}
	}

	@Override
	public void delete(int id) {

		String sql = "DELETE FROM Utente WHERE idUtente = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			logger.error("{} durante delete(): {}", ERRORE_SQL, e.getMessage(), e);
		}
	}

	@Override
	public Utente getUtenteById(int id) {

		String sql = "SELECT * FROM Utente WHERE idUtente = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs);
			}

		} catch (SQLException e) {
			logger.error("{} durante getUtenteById(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return UTENTE_INESISTENTE;
	}

	@Override
	public Utente getUtenteByEmail(String email) {

		String sql = "SELECT * FROM Utente WHERE email = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs);
			}

		} catch (SQLException e) {
			logger.error("{} durante getUtenteByEmail(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return UTENTE_INESISTENTE;
	}

	@Override
	public boolean existsByEmail(String email) {

		String sql = "SELECT 1 FROM Utente WHERE email = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}

		} catch (SQLException e) {
			logger.error("{} durante existsByEmail(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return false;
	}

	@Override
	public List<Utente> getTuttiUtenti() {

		String sql = "SELECT * FROM Utente ORDER BY idUtente ASC";
		List<Utente> list = new ArrayList<>();

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			fillList(rs, list);

		} catch (SQLException e) {
			logger.error("{} durante getTuttiUtenti(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return list;
	}

	@Override
	public Utente getManager() {

		String sql = """
				SELECT * FROM Utente
				WHERE ruoloUtente = 'MANAGER'
				ORDER BY idUtente ASC
				LIMIT 1
				""";

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			return extractOne(rs);

		} catch (SQLException e) {
			logger.error("{} durante getManager(): {}", ERRORE_SQL, e.getMessage(), e);
		}

		return UTENTE_INESISTENTE;
	}
}
