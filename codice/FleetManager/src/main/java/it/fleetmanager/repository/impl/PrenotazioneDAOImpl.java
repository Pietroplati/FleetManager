package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.db.ConnectionProvider;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

	private final ConnectionProvider db;

	public static final Prenotazione PRENOTAZIONE_INESISTENTE = new Prenotazione(-1, LocalDateTime.MIN, LocalDateTime.MIN,
			StatoPrenotazione.ANNULLATA, TipoPrenotazione.UTENTE, -1, "N/A") {
		@Override
		public String toString() {
			return "Prenotazione inesistente";
		}
	};

	public PrenotazioneDAOImpl(ConnectionProvider db) {
		this.db = db;
	}

	// Mapping
	private Prenotazione map(ResultSet rs) throws Exception {
		int idPren = rs.getInt("idPrenotazione");
		LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
		LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();
		StatoPrenotazione stato = StatoPrenotazione.valueOf(rs.getString("statoPrenotazione"));
		TipoPrenotazione tipo = TipoPrenotazione.valueOf(rs.getString("tipoPrenotazione"));
		int idUtente = rs.getInt("idUtente");
		String targa = rs.getString("targa");

		return new Prenotazione(idPren, dataInizio, dataFine, stato, tipo, idUtente, targa);
	}

	// Helper funzionali (no framework, nessuna logica cambiata)
	@FunctionalInterface
	private interface SQLConsumer<T> {
		void accept(T t) throws Exception;
	}

	@FunctionalInterface
	private interface SQLFunction<T, R> {
		R apply(T t) throws Exception;
	}

	// Helper JDBC comuni
	private void inTransaction(String ctx, SQLConsumer<Connection> work) {
		try (Connection conn = db.getConnection()) {
			conn.setAutoCommit(false);
			work.accept(conn);
			conn.commit(); // stessa logica: commit esplicito
		} catch (Exception e) {
			System.err.println("ERRORE SQL " + ctx + ": " + e.getMessage());
		}
	}

	private List<Prenotazione> queryList(String ctx, String sql, SQLConsumer<PreparedStatement> binder) {
		List<Prenotazione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(map(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL " + ctx + ": " + e.getMessage());
		}

		return list;
	}

	private Prenotazione queryOne(String ctx, String sql, SQLConsumer<PreparedStatement> binder, Prenotazione defaultValue) {
		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return defaultValue;
				return map(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL " + ctx + ": " + e.getMessage());
			return defaultValue;
		}
	}

	private boolean queryBoolean(String ctx, String sql, SQLConsumer<PreparedStatement> binder, SQLFunction<ResultSet, Boolean> extractor,
			boolean defaultValue) {

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			if (binder != null) {
				binder.accept(ps);
			}

			try (ResultSet rs = ps.executeQuery()) {
				return extractor.apply(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL " + ctx + ": " + e.getMessage());
		}

		return defaultValue;
	}

	private void bindPrenotazioneCommon(PreparedStatement ps, Prenotazione p) throws Exception {
		ps.setTimestamp(1, Timestamp.valueOf(p.getDataInizio()));
		ps.setTimestamp(2, Timestamp.valueOf(p.getDataFine()));
		ps.setString(3, p.getStato().name());
		ps.setString(4, p.getTipoPrenotazione().name());
		ps.setInt(5, p.getIdUtente());
		ps.setString(6, p.getTarga());
	}

	// CRUD
	@Override
	public void save(Prenotazione p) {

		String sql = """
				    INSERT INTO Prenotazione
				    (dataInizio, dataFine, statoPrenotazione, tipoPrenotazione, idUtente, targa)
				    VALUES (?, ?, ?, ?, ?, ?)
				""";

		//stessa logica: transazione, insert, generated key, commit, print error
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

				// stessi parametri, stesso ordine
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

	@Override
	public Prenotazione getById(int id) {
		String sql = "SELECT * FROM Prenotazione WHERE idPrenotazione = ?";

		return queryOne("getById", sql, ps -> ps.setInt(1, id), PRENOTAZIONE_INESISTENTE);
	}

	// Query specifiche
	@Override
	public List<Prenotazione> findByDriver(int idUtente) {
		String sql = "SELECT * FROM Prenotazione WHERE idUtente = ?";

		return queryList("findByDriver", sql, ps -> ps.setInt(1, idUtente));
	}

	@Override
	public List<Prenotazione> findByVeicolo(String targa) {
		String sql = "SELECT * FROM Prenotazione WHERE targa = ?";

		return queryList("findByVeicolo", sql, ps -> ps.setString(1, targa));
	}

	@Override
	public List<Prenotazione> findByStato(StatoPrenotazione stato) {
		String sql = "SELECT * FROM Prenotazione WHERE statoPrenotazione = ?";

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

		return queryBoolean("existsOverlapping", sql,
				ps -> {
					ps.setString(1, targa);
					ps.setTimestamp(2, Timestamp.valueOf(dataFine));
					ps.setTimestamp(3, Timestamp.valueOf(dataInizio));
				},
				rs -> {
					if (rs.next())
						return rs.getInt(1) > 0;
					return false;
				}, false);
	}

	@Override
	public List<Prenotazione> findAll() {
		String sql = "SELECT * FROM Prenotazione";

		return queryList("findAll", sql, null);
	}
}
