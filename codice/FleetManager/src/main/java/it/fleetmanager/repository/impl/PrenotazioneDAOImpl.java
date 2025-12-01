package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.dao.PrenotazioneDAO;
import it.fleetmanager.repository.util.DatabaseManager;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

	private final DatabaseManager db;

	public static final Prenotazione PRENOTAZIONE_INESISTENTE = new Prenotazione(-1, LocalDateTime.MIN,
			LocalDateTime.MIN, StatoPrenotazione.ANNULLATA, TipoPrenotazione.UTENTE, -1, "N/A") {
		@Override
		public String toString() {
			return "Prenotazione inesistente";
		}
	};

	public PrenotazioneDAOImpl(DatabaseManager db) {
		this.db = db;
	}

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

	@Override
	public void save(Prenotazione p) {
		String sql = """
				INSERT INTO Prenotazione
				(idPrenotazione, dataInizio, dataFine,
				 statoPrenotazione, tipoPrenotazione,
				 idUtente, targa)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, p.getIdPrenotazione());
			ps.setTimestamp(2, Timestamp.valueOf(p.getDataInizio()));
			ps.setTimestamp(3, Timestamp.valueOf(p.getDataFine()));
			ps.setString(4, p.getStato().name());
			ps.setString(5, p.getTipoPrenotazione().name());
			ps.setInt(6, p.getIdUtente());
			ps.setString(7, p.getTarga());

			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERRORE SQL save: " + e.getMessage());
		}
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(p.getDataInizio()));
			ps.setTimestamp(2, Timestamp.valueOf(p.getDataFine()));
			ps.setString(3, p.getStato().name());
			ps.setString(4, p.getTipoPrenotazione().name());
			ps.setInt(5, p.getIdUtente());
			ps.setString(6, p.getTarga());
			ps.setInt(7, p.getIdPrenotazione());

			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERRORE SQL update: " + e.getMessage());
		}
	}

	@Override
	public void delete(int id) {
		String sql = "DELETE FROM Prenotazione WHERE idPrenotazione = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERRORE SQL delete: " + e.getMessage());
		}
	}

	@Override
	public Prenotazione getById(int id) {
		String sql = "SELECT * FROM Prenotazione WHERE idPrenotazione = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return PRENOTAZIONE_INESISTENTE;
				return map(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL getById: " + e.getMessage());
			return PRENOTAZIONE_INESISTENTE;
		}
	}

	@Override
	public List<Prenotazione> findByDriver(int idUtente) {
		String sql = "SELECT * FROM Prenotazione WHERE idUtente = ?";
		List<Prenotazione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL findByDriver: " + e.getMessage());
		}

		return list;
	}

	@Override
	public List<Prenotazione> findByVeicolo(String targa) {
		String sql = "SELECT * FROM Prenotazione WHERE targa = ?";
		List<Prenotazione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL findByVeicolo: " + e.getMessage());
		}

		return list;
	}

	@Override
	public List<Prenotazione> findByStato(StatoPrenotazione stato) {
		String sql = "SELECT * FROM Prenotazione WHERE statoPrenotazione = ?";
		List<Prenotazione> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, stato.name());

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL findByStato: " + e.getMessage());
		}

		return list;
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

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);
			ps.setTimestamp(2, Timestamp.valueOf(dataFine));
			ps.setTimestamp(3, Timestamp.valueOf(dataInizio));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1) > 0;
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL existsOverlapping: " + e.getMessage());
		}

		return false;
	}
}
