package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.UtenteDAO;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImpl implements UtenteDAO {

	public static final Utente UTENTE_INESISTENTE = new Utente(-1, "N/A", "N/A", "N/A", "N/A", RuoloUtente.MANAGER) {
		@Override
		public String toString() {
			return "Utente inesistente";
		}
	};

	@Override
	public void save(Utente utente) {

		String sql = "INSERT INTO Utente " + "(idUtente, nome, cognome, email, password, ruoloUtente, patente) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, utente.getIdUtente());
			ps.setString(2, utente.getNome());
			ps.setString(3, utente.getCognome());
			ps.setString(4, utente.getEmail());
			ps.setString(5, utente.getPassword());
			ps.setString(6, utente.getRuoloUtente().name());
			ps.setString(7, utente.getPatente());

			ps.executeUpdate();
			System.out.println("Utente inserito correttamente nel database H2");

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'inserimento dell'utente: " + e.getMessage());
		}
	}

	@Override
	public void update(Utente utente) {

		String sql = "UPDATE Utente SET " + "nome=?, cognome=?, email=?, password=?, ruoloUtente=?, patente=? "
				+ "WHERE idUtente=?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, utente.getNome());
			ps.setString(2, utente.getCognome());
			ps.setString(3, utente.getEmail());
			ps.setString(4, utente.getPassword());
			ps.setString(5, utente.getRuoloUtente().name());
			ps.setString(6, utente.getPatente());
			ps.setInt(7, utente.getIdUtente());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("✔ Utente aggiornato correttamente nel database H2!");
			} else {
				System.err.println("ERRORE: utente con ID " + utente.getIdUtente() + " non trovato nel database H2.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'UPDATE dell'utente: " + e.getMessage());
		}
	}

	@Override
	public void delete(int id) {

		String sql = "DELETE FROM Utente WHERE idUtente = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Utente eliminato correttamente dal database H2");
			} else {
				System.err.println("ERRORE: utente con ID " + id + " non trovato nel database H2.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante l'eliminazione dell'utente: " + e.getMessage());
		}
	}

	@Override
	public Utente getUtenteById(int id) {
		String sql = "SELECT idUtente, nome, cognome, email, password, ruoloUtente, patente "
				+ "FROM Utente WHERE idUtente = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return UTENTE_INESISTENTE;
				}

				int idUtente = rs.getInt("idUtente");
				String nome = rs.getString("nome");
				String cognome = rs.getString("cognome");
				String email = rs.getString("email");
				String password = rs.getString("password");
				String ruoloDb = rs.getString("ruoloUtente");
				String patente = rs.getString("patente");

				RuoloUtente ruolo = RuoloUtente.valueOf(ruoloDb);

				return (patente == null) ? new Utente(idUtente, nome, cognome, email, password, ruolo)
						: new Utente(idUtente, nome, cognome, email, password, ruolo, patente);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return UTENTE_INESISTENTE;
		}
	}

	@Override
	public Utente getUtenteByEmail(String email) {
		String sql = "SELECT idUtente, nome, cognome, email, password, ruoloUtente, patente "
				+ "FROM Utente WHERE email = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return UTENTE_INESISTENTE;
				}

				int idUtente = rs.getInt("idUtente");
				String nome = rs.getString("nome");
				String cognome = rs.getString("cognome");
				String password = rs.getString("password");
				String ruoloDb = rs.getString("ruoloUtente");
				String patente = rs.getString("patente");

				RuoloUtente ruolo = RuoloUtente.valueOf(ruoloDb);

				return (patente == null) ? new Utente(idUtente, nome, cognome, email, password, ruolo)
						: new Utente(idUtente, nome, cognome, email, password, ruolo, patente);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return UTENTE_INESISTENTE;
		}
	}

	@Override
	public boolean existsByEmail(String email) {
		String sql = "SELECT 1 FROM Utente WHERE email = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
