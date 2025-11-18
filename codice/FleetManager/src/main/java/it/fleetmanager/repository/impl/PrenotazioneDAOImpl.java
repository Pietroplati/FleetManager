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
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.PrenotazioneDAO;
import it.fleetmanager.util.StatoPrenotazione;
import it.fleetmanager.util.TipoPrenotazione;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

	public static final Prenotazione PRENOTAZIONE_INESISTENTE = new Prenotazione(-1, LocalDateTime.MIN,
			LocalDateTime.MIN, StatoPrenotazione.ANNULLATA, TipoPrenotazione.UTENTE, -1, "N/A") {
		@Override
		public String toString() {
			return "Prenotazione inesistente";
		}
	};

	private Prenotazione mapPrenotazione(ResultSet rs) throws Exception {

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
	public void save(Prenotazione prenotazione) {

		String sql = """
				INSERT INTO Prenotazione
				(idPrenotazione, dataInizio, dataFine,
				 statoPrenotazione, tipoPrenotazione,
				 idUtente, targa)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, prenotazione.getIdPrenotazione());
			ps.setTimestamp(2, Timestamp.valueOf(prenotazione.getDataInizio()));
			ps.setTimestamp(3, Timestamp.valueOf(prenotazione.getDataFine()));
			ps.setString(4, prenotazione.getStato().name());
			ps.setString(5, prenotazione.getTipoPrenotazione().name());
			ps.setInt(6, prenotazione.getIdUtente());
			ps.setString(7, prenotazione.getTarga());

			ps.executeUpdate();
			System.out.println("Prenotazione inserita correttamente nel database H2");

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante save(prenotazione): " + e.getMessage());
		}
	}

	@Override
	public void update(Prenotazione prenotazione) {

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

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, Timestamp.valueOf(prenotazione.getDataInizio()));
			ps.setTimestamp(2, Timestamp.valueOf(prenotazione.getDataFine()));
			ps.setString(3, prenotazione.getStato().name());
			ps.setString(4, prenotazione.getTipoPrenotazione().name());
			ps.setInt(5, prenotazione.getIdUtente());
			ps.setString(6, prenotazione.getTarga());
			ps.setInt(7, prenotazione.getIdPrenotazione());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Prenotazione aggiornata correttamente");
			} else {
				System.err.println("ERRORE: prenotazione con ID " + prenotazione.getIdPrenotazione() + " non trovata.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante update(prenotazione): " + e.getMessage());
		}
	}

	@Override
	public void delete(int id) {

		String sql = "DELETE FROM Prenotazione WHERE idPrenotazione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Prenotazione con ID " + id + " eliminata.");
			} else {
				System.err.println("Nessuna prenotazione trovata con ID " + id);
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante delete(prenotazione): " + e.getMessage());
		}
	}

	@Override
	public Prenotazione getById(int id) {

		String sql = "SELECT * FROM Prenotazione WHERE idPrenotazione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next())
					return PRENOTAZIONE_INESISTENTE;

				return mapPrenotazione(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante getById: " + e.getMessage());
			return PRENOTAZIONE_INESISTENTE;
		}
	}

	@Override
	public List<Prenotazione> findByDriver(int idUtente) {

		String sql = "SELECT * FROM Prenotazione WHERE idUtente = ?";

		List<Prenotazione> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapPrenotazione(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByDriver: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Prenotazione> findByVeicolo(String targa) {

		String sql = "SELECT * FROM Prenotazione WHERE targa = ?";

		List<Prenotazione> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapPrenotazione(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByVeicolo: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Prenotazione> findByStato(StatoPrenotazione statoRichiesto) {

		String sql = "SELECT * FROM Prenotazione WHERE statoPrenotazione = ?";

		List<Prenotazione> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, statoRichiesto.name());

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(mapPrenotazione(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByStato: " + e.getMessage());
		}

		return lista;
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

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);
			ps.setTimestamp(2, Timestamp.valueOf(dataFine));
			ps.setTimestamp(3, Timestamp.valueOf(dataInizio));

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante existsOverlapping: " + e.getMessage());
		}

		return false;
	}

}
