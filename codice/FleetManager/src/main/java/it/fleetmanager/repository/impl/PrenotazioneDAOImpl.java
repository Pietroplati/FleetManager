package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.PrenotazioneDAO;
import it.fleetmanager.util.StatoPrenotazione;

public class PrenotazioneDAOImpl implements PrenotazioneDAO {

	@Override
	public void save(Prenotazione prenotazione) {

		String sql = "INSERT INTO Prenotazione " + "(idPrenotazione, dataInizio, dataFine, stato, idUtente, targa) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, prenotazione.getIdPrenotazione());
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(prenotazione.getDataInizio()));
			ps.setTimestamp(3, java.sql.Timestamp.valueOf(prenotazione.getDataFine()));
			ps.setString(4, prenotazione.getStato().name());
			ps.setInt(5, prenotazione.getIdUtente());
			ps.setString(6, prenotazione.getTarga());

			ps.executeUpdate();
			System.out.println("✔ Prenotazione inserita correttamente nel database H2!");

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'inserimento della prenotazione: " + e.getMessage());
		}
	}

	@Override
	public void update(Prenotazione prenotazione) {

		String sql = "UPDATE Prenotazione SET " + "dataInizio=?, dataFine=?, stato=?, idUtente=?, targa=? "
				+ "WHERE idPrenotazione=?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, java.sql.Timestamp.valueOf(prenotazione.getDataInizio()));
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(prenotazione.getDataFine()));
			ps.setString(3, prenotazione.getStato().name());
			ps.setInt(4, prenotazione.getIdUtente());
			ps.setString(5, prenotazione.getTarga());
			ps.setInt(6, prenotazione.getIdPrenotazione());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("✔ Prenotazione aggiornata correttamente nel database H2!");
			} else {
				System.err.println("ERRORE: prenotazione con ID " + prenotazione.getIdPrenotazione()
						+ " non trovata nel database H2.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'UPDATE della prenotazione: " + e.getMessage());
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
				System.out.println("✔ Prenotazione eliminata correttamente dal database H2!");
			} else {
				System.err.println("ERRORE: prenotazione con ID " + id + " non trovata nel database H2.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'eliminazione della prenotazione: " + e.getMessage());
		}
	}

	@Override
	public Prenotazione getById(int id) {
		String sql = "SELECT idPrenotazione, dataInizio, dataFine, stato, idUtente, targa "
				+ "FROM Prenotazione WHERE idPrenotazione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					return null; // oppure PRENOTAZIONE_INESISTENTE se vuoi una costante come per UTENTE
				}

				int idPren = rs.getInt("idPrenotazione");
				LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
				LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();

				String statoDb = rs.getString("stato");
				StatoPrenotazione stato = StatoPrenotazione.valueOf(statoDb);

				int idUtente = rs.getInt("idUtente");
				String targa = rs.getString("targa");

				return new Prenotazione(idPren, dataInizio, dataFine, stato, idUtente, targa);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null; // oppure PRENOTAZIONE_INESISTENTE
		}
	}

	@Override
	public List<Prenotazione> findByDriver(int idUtente) {

		String sql = "SELECT idPrenotazione, dataInizio, dataFine, stato, idUtente, targa "
				+ "FROM Prenotazione WHERE idUtente = ?";

		List<Prenotazione> prenotazioni = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idUtente);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int idPren = rs.getInt("idPrenotazione");
					LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
					LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();

					String statoDb = rs.getString("stato");
					StatoPrenotazione stato = StatoPrenotazione.valueOf(statoDb);

					String targa = rs.getString("targa");

					Prenotazione p = new Prenotazione(idPren, dataInizio, dataFine, stato, idUtente, targa);

					prenotazioni.add(p);
				}
			}

			if (prenotazioni.isEmpty()) {
				System.err.println("Nessuna prenotazione trovata per l’utente con ID " + idUtente);
			} else {
				System.out
						.println("✔ Trovate " + prenotazioni.size() + " prenotazioni per l’utente con ID " + idUtente);
			}

			return prenotazioni;

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante findByDriver: " + e.getMessage());
			return prenotazioni;
		}
	}

	@Override
	public List<Prenotazione> findByVeicolo(String targa) {

		String sql = "SELECT idPrenotazione, dataInizio, dataFine, stato, idUtente, targa "
				+ "FROM Prenotazione WHERE targa = ?";

		List<Prenotazione> prenotazioni = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int idPren = rs.getInt("idPrenotazione");
					LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
					LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();

					String statoDb = rs.getString("stato");
					StatoPrenotazione stato = StatoPrenotazione.valueOf(statoDb);

					int idUtente = rs.getInt("idUtente");
					String targaDb = rs.getString("targa");

					Prenotazione p = new Prenotazione(idPren, dataInizio, dataFine, stato, idUtente, targaDb);

					prenotazioni.add(p);
				}
			}

			if (prenotazioni.isEmpty()) {
				System.err.println("Nessuna prenotazione trovata per il veicolo con targa " + targa);
			} else {
				System.out.println(
						"✔ Trovate " + prenotazioni.size() + " prenotazioni per il veicolo con targa " + targa);
			}

			return prenotazioni;

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante findByVeicolo: " + e.getMessage());
			return prenotazioni;
		}
	}

	@Override
	public List<Prenotazione> findAttive() {

		String sql = "SELECT idPrenotazione, dataInizio, dataFine, stato, idUtente, targa "
				+ "FROM Prenotazione WHERE stato = 'ATTIVA'";

		List<Prenotazione> prenotazioni = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				int idPren = rs.getInt("idPrenotazione");
				LocalDateTime dataInizio = rs.getTimestamp("dataInizio").toLocalDateTime();
				LocalDateTime dataFine = rs.getTimestamp("dataFine").toLocalDateTime();

				String statoDb = rs.getString("stato");
				StatoPrenotazione stato = StatoPrenotazione.valueOf(statoDb);

				int idUtente = rs.getInt("idUtente");
				String targa = rs.getString("targa");

				Prenotazione p = new Prenotazione(idPren, dataInizio, dataFine, stato, idUtente, targa);

				prenotazioni.add(p);
			}

			if (prenotazioni.isEmpty()) {
				System.err.println("Nessuna prenotazione attiva trovata nel database H2.");
			} else {
				System.out.println("✔ Trovate " + prenotazioni.size() + " prenotazioni attive.");
			}

			return prenotazioni;

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante findAttive: " + e.getMessage());
			return prenotazioni;
		}
	}

	@Override
	public boolean existsOverlapping(String targa, LocalDateTime dataInizio, LocalDateTime dataFine) {

	    String sql = "SELECT COUNT(*) AS cnt "
	               + "FROM Prenotazione "
	               + "WHERE targa = ? "
	               + "AND dataInizio < ? "      // inizio esistente prima che finisca la nuova
	               + "AND dataFine > ?";       // fine esistente dopo che inizia la nuova

	    try (Connection conn = DatabaseManager.getInstance().getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setString(1, targa);
	        ps.setTimestamp(2, java.sql.Timestamp.valueOf(dataFine));
	        ps.setTimestamp(3, java.sql.Timestamp.valueOf(dataInizio));

	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                int count = rs.getInt("cnt");
	                return count > 0; // true = sovrapposizione presente
	            }
	        }

	    } catch (SQLException e) {
	        System.err.println("ERRORE SQL durante existsOverlapping: " + e.getMessage());
	    }

	    return false; // nessuna sovrapposizione o errore
	}


}
