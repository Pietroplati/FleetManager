package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

	private Utente mapUtente(ResultSet rs) throws Exception {

		int idUtente = rs.getInt("idUtente");
		String nome = rs.getString("nome");
		String cognome = rs.getString("cognome");
		String email = rs.getString("email");
		String password = rs.getString("password");
		RuoloUtente ruolo = RuoloUtente.valueOf(rs.getString("ruoloUtente"));

		String patente = rs.getString("patente"); // può essere null

		if (patente == null) {
			return new Utente(idUtente, nome, cognome, email, password, ruolo);
		} else {
			return new Utente(idUtente, nome, cognome, email, password, ruolo, patente);
		}
	}

	@Override
	public void save(Utente utente) {

		String sql = """
				INSERT INTO Utente
				(idUtente, nome, cognome, email, password, ruoloUtente, patente)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

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
			System.out.println("Utente inserito correttamente!");

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante save(utente): " + e.getMessage());
		}
	}

	@Override
	public void update(Utente utente) {

		String sql = """
				UPDATE Utente SET
				    nome = ?, cognome = ?, email = ?, password = ?,
				    ruoloUtente = ?, patente = ?
				WHERE idUtente = ?
				""";

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
				System.out.println("Utente aggiornato correttamente!");
			} else {
				System.err.println("Utente con ID " + utente.getIdUtente() + " non trovato.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante update(utente): " + e.getMessage());
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
				System.out.println("Utente con ID " + id + " eliminato.");
			} else {
				System.err.println("Nessun utente con ID " + id + " trovato.");
			}

		} catch (SQLException e) {
			System.err.println("ERRORE SQL durante delete(utente): " + e.getMessage());
		}
	}

	@Override
	public Utente getUtenteById(int id) {

		String sql = "SELECT * FROM Utente WHERE idUtente = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next())
					return UTENTE_INESISTENTE;

				return mapUtente(rs);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return UTENTE_INESISTENTE;
		}
	}

	@Override
	public Utente getUtenteByEmail(String email) {

		String sql = "SELECT * FROM Utente WHERE email = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next())
					return UTENTE_INESISTENTE;

				return mapUtente(rs);
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

	@Override
	public List<Utente> getTuttiUtenti() {

		String sql = """
				SELECT *
				FROM Utente
				ORDER BY idUtente ASC
				""";

		List<Utente> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				lista.add(mapUtente(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante getTuttiUtenti(): " + e.getMessage());
		}

		return lista;
	}

}
