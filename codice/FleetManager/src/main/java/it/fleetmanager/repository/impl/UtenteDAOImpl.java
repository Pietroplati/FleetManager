package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.util.DatabaseManager;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.RuoloUtente;

public class UtenteDAOImpl implements UtenteDAO {

	private final H2DatabaseManager db;

	public static final Utente UTENTE_INESISTENTE = new Utente(-1, "N/A", "N/A", "N/A", "N/A", RuoloUtente.MANAGER) {
		@Override
		public String toString() {
			return "Utente inesistente";
		}
	};

	public UtenteDAOImpl(H2DatabaseManager db) {
		this.db = db;
	}

	private Utente map(ResultSet rs) throws Exception {
		int idUtente = rs.getInt("idUtente");
		String nome = rs.getString("nome");
		String cognome = rs.getString("cognome");
		String email = rs.getString("email");
		String password = rs.getString("password");
		RuoloUtente ruolo = RuoloUtente.valueOf(rs.getString("ruoloUtente"));
		String patente = rs.getString("patente");

		if (patente == null)
			return new Utente(idUtente, nome, cognome, email, password, ruolo);
		else
			return new Utente(idUtente, nome, cognome, email, password, ruolo, patente);
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
			System.err.println("ERRORE SQL save: " + e.getMessage());
		}
	}

	@Override
	public void update(Utente u) {
		String sql = """
				UPDATE Utente SET
				    nome = ?, cognome = ?, email = ?, password = ?,
				    ruoloUtente = ?, patente = ?
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
			System.err.println("ERRORE SQL update: " + e.getMessage());
		}
	}

	@Override
	public void delete(int id) {
		String sql = "DELETE FROM Utente WHERE idUtente = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);
			ps.executeUpdate();

		} catch (SQLException e) {
			System.err.println("ERRORE SQL delete: " + e.getMessage());
		}
	}

	@Override
	public Utente getUtenteById(int id) {
		String sql = "SELECT * FROM Utente WHERE idUtente = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return UTENTE_INESISTENTE;
				return map(rs);
			}

		} catch (Exception e) {
			return UTENTE_INESISTENTE;
		}
	}

	@Override
	public Utente getUtenteByEmail(String email) {
		String sql = "SELECT * FROM Utente WHERE email = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return UTENTE_INESISTENTE;
				return map(rs);
			}

		} catch (Exception e) {
			return UTENTE_INESISTENTE;
		}
	}

	@Override
	public boolean existsByEmail(String email) {
		String sql = "SELECT 1 FROM Utente WHERE email = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}

		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<Utente> getTuttiUtenti() {
		String sql = "SELECT * FROM Utente ORDER BY idUtente ASC";
		List<Utente> lista = new ArrayList<>();

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next())
				lista.add(map(rs));

		} catch (Exception e) {
			System.err.println("ERRORE SQL getTuttiUtenti: " + e.getMessage());
		}

		return lista;
	}
}
