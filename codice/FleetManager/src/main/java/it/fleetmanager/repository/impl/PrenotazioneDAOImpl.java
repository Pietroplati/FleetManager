package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

/**
 * Implementazione del DAO {@link PrenotazioneDAO}.
 * <p>
 * Questa classe gestisce tutte le operazioni di persistenza relative all'entità
 * {@link Prenotazione}, incapsulando l'accesso al database e separando la
 * logica di mapping dai dettagli SQL.
 * </p>
 *
 * <p>
 * Eventuali complessità ciclomatiche elevate sono dovute esclusivamente alla
 * gestione tecnica delle query SQL e del {@link ResultSet}, senza presenza di
 * logica di business.
 * </p>
 */
public class PrenotazioneDAOImpl implements PrenotazioneDAO {

	private static final Logger logger = LogManager.getLogger(PrenotazioneDAOImpl.class);
	private static final String ERRORE_SQL = "ERRORE SQL";

	private final ConnectionProvider db;

	/**
	 * Oggetto sentinella utilizzato quando una prenotazione non esiste.
	 */
	public static final Prenotazione PRENOTAZIONE_INESISTENTE = new Prenotazione(-1, LocalDateTime.MIN,
			LocalDateTime.MIN, StatoPrenotazione.ANNULLATA, TipoPrenotazione.UTENTE, -1, "N/A") {
		@Override
		public String toString() {
			return "Prenotazione inesistente";
		}
	};

	/**
	 * Costruttore.
	 *
	 * @param db provider delle connessioni al database
	 */
	public PrenotazioneDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	/**
	 * Mappa una riga del {@link ResultSet} in un oggetto {@link Prenotazione}.
	 *
	 * @param rs result set posizionato sulla riga corrente
	 * @return prenotazione mappata
	 * @throws SQLException in caso di errore SQL
	 */
	private Prenotazione map(ResultSet rs) throws SQLException {
		int idPren = rs.getInt("idPrenotazione");
		LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
		LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();
		StatoPrenotazione stato = StatoPrenotazione.valueOf(rs.getString("statoPrenotazione"));
		TipoPrenotazione tipo = TipoPrenotazione.valueOf(rs.getString("tipoPrenotazione"));
		int idUtente = rs.getInt("idUtente");
		String targa = rs.getString("targa");

		return new Prenotazione(idPren, dataInizio, dataFine, stato, tipo, idUtente, targa);
	}

	/*
	 * ===================================================== Functional interfaces
	 * di supporto =====================================================
	 */

	@FunctionalInterface
	private interface SQLConsumer<T> {
		void accept(T t) throws SQLException;
	}

	@FunctionalInterface
	private interface SQLFunction<T, R> {
		R apply(T t) throws SQLException;
	}

	/*
	 * ===================================================== Utility generiche
	 * =====================================================
	 */

	/**
	 * Esegue un'operazione all'interno di una transazione.
	 *
	 * @param ctx  contesto logico dell'operazione
	 * @param work codice da eseguire nella transazione
	 */
	private void inTransaction(String ctx, SQLConsumer<Connection> work) {
		try (Connection conn = db.getConnection()) {
			conn.setAutoCommit(false);
			work.accept(conn);
			conn.commit();
		} catch (SQLException e) {
			logger.error("{} {}: {}", ERRORE_SQL, ctx, e.getMessage(), e);
		}
	}

	/**
	 * Riempie una lista di prenotazioni a partire da un {@link ResultSet}.
	 *
	 * @param rs  result set
	 * @param out lista di destinazione
	 * @throws SQLException in caso di errore SQL
	 */
	private void fillList(ResultSet rs, List<Prenotazione> out) throws SQLException {
		while (rs.next()) {
			out.add(map(rs));
		}
	}

	/**
	 * Estrae una singola prenotazione da un {@link ResultSet}.
	 *
	 * @param rs           result set
	 * @param defaultValue valore di default se non presente
	 * @return prenotazione trovata o valore di default
	 * @throws SQLException in caso di errore SQL
	 */
	private Prenotazione extractOne(ResultSet rs, Prenotazione defaultValue) throws SQLException {
		return rs.next() ? map(rs) : defaultValue;
	}

	/**
	 * Estrae un valore booleano da una query di tipo COUNT.
	 *
	 * @param rs result set
	 * @return true se il conteggio è maggiore di zero
	 * @throws SQLException in caso di errore SQL
	 */
	private boolean extractExists(ResultSet rs) throws SQLException {
		return rs.next() && rs.getInt(1) > 0;
	}

	/**
	 * Esegue una query che restituisce una lista di prenotazioni.
	 */
	private List<Prenotazione> queryList(String ctx, String sql, SQLConsumer<PreparedStatement> binder) {

		List<Prenotazione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				fillList(rs, list);
			}

		} catch (SQLException e) {
			logger.error("{} {}: {}", ERRORE_SQL, ctx, e.getMessage(), e);
		}

		return list;
	}

	/**
	 * Esegue una query che restituisce una singola prenotazione.
	 */
	private Prenotazione queryOne(String ctx, String sql, SQLConsumer<PreparedStatement> binder,
			Prenotazione defaultValue) {

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return extractOne(rs, defaultValue);
			}

		} catch (SQLException e) {
			logger.error("{} {}: {}", ERRORE_SQL, ctx, e.getMessage(), e);
		}

		return defaultValue;
	}

	/**
	 * Esegue una query booleana.
	 */
	private boolean queryBoolean(String ctx, String sql, SQLConsumer<PreparedStatement> binder,
			SQLFunction<ResultSet, Boolean> extractor, boolean defaultValue) {

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return extractor.apply(rs);
			}

		} catch (SQLException e) {
			logger.error("{} {}: {}", ERRORE_SQL, ctx, e.getMessage(), e);
		}

		return defaultValue;
	}

	/**
	 * Effettua il binding dei campi comuni di una prenotazione.
	 */
	private void bindPrenotazioneCommon(PreparedStatement ps, Prenotazione p) throws SQLException {

		ps.setTimestamp(1, Timestamp.valueOf(p.getDataInizio()));
		ps.setTimestamp(2, Timestamp.valueOf(p.getDataFine()));
		ps.setString(3, p.getStato().name());
		ps.setString(4, p.getTipoPrenotazione().name());
		ps.setInt(5, p.getIdUtente());
		ps.setString(6, p.getTarga());
	}

	/*
	 * ===================================================== CRUD
	 * =====================================================
	 */

	@Override
	public void save(Prenotazione p) {
		String sql = """
				INSERT INTO Prenotazione
				(dataInizio, dataFine, statoPrenotazione,
				 tipoPrenotazione, idUtente, targa)
				VALUES (?, ?, ?, ?, ?, ?)
				""";

		inTransaction("save", conn -> {
			try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

				bindPrenotazioneCommon(ps, p);
				ps.executeUpdate();

				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) {
						p.setId(rs.getInt(1));
					}
				}
			}
		});
	}

	@Override
	public void update(Prenotazione p) {
		String sql = """
				UPDATE Prenotazione SET
				    dataInizio = ?,
				    dataFine = ?,
				    statoPrenotazione = ?,
				    tipoPrenotazione = ?,
				    idUtente = ?,
				    targa = ?
				WHERE idPrenotazione = ?
				""";

		inTransaction("update", conn -> {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				bindPrenotazioneCommon(ps, p);
				ps.setInt(7, p.getIdPrenotazione());
				ps.executeUpdate();
			}
		});
	}

	@Override
	public void delete(int id) {
		String sql = "DELETE FROM Prenotazione WHERE idPrenotazione = ?";

		inTransaction("delete", conn -> {
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, id);
				ps.executeUpdate();
			}
		});
	}

	/*
	 * ===================================================== Query pubbliche
	 * =====================================================
	 */

	@Override
	public Prenotazione getById(int id) {
		String sql = """
				SELECT idPrenotazione, dataInizio, dataFine,
				       statoPrenotazione, tipoPrenotazione,
				       idUtente, targa
				FROM Prenotazione
				WHERE idPrenotazione = ?
				""";

		return queryOne("getById", sql, ps -> ps.setInt(1, id), PRENOTAZIONE_INESISTENTE);
	}

	@Override
	public List<Prenotazione> findByDriver(int idUtente) {
		String sql = """
				SELECT idPrenotazione, dataInizio, dataFine,
				       statoPrenotazione, tipoPrenotazione,
				       idUtente, targa
				FROM Prenotazione
				WHERE idUtente = ?
				""";

		return queryList("findByDriver", sql, ps -> ps.setInt(1, idUtente));
	}

	@Override
	public List<Prenotazione> findByVeicolo(String targa) {
		String sql = """
				SELECT idPrenotazione, dataInizio, dataFine,
				       statoPrenotazione, tipoPrenotazione,
				       idUtente, targa
				FROM Prenotazione
				WHERE targa = ?
				""";

		return queryList("findByVeicolo", sql, ps -> ps.setString(1, targa));
	}

	@Override
	public List<Prenotazione> findByStato(StatoPrenotazione stato) {
		String sql = """
				SELECT idPrenotazione, dataInizio, dataFine,
				       statoPrenotazione, tipoPrenotazione,
				       idUtente, targa
				FROM Prenotazione
				WHERE statoPrenotazione = ?
				""";

		return queryList("findByStato", sql, ps -> ps.setString(1, stato.name()));
	}

	@Override
	public boolean existsOverlapping(String targa, LocalDateTime dataInizio, LocalDateTime dataFine) {

		String sql = """
				SELECT COUNT(*)
				FROM Prenotazione
				WHERE targa = ?
				  AND dataInizio < ?
				  AND dataFine > ?
				""";

		return queryBoolean("existsOverlapping", sql, ps -> {
			ps.setString(1, targa);
			ps.setTimestamp(2, Timestamp.valueOf(dataFine));
			ps.setTimestamp(3, Timestamp.valueOf(dataInizio));
		}, this::extractExists, false);
	}

	@Override
	public List<Prenotazione> findAll() {
		String sql = """
				SELECT idPrenotazione, dataInizio, dataFine,
				       statoPrenotazione, tipoPrenotazione,
				       idUtente, targa
				FROM Prenotazione
				""";

		return queryList("findAll", sql, null);
	}
}
