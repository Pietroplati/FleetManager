package it.fleetmanager.bootstrap.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import it.fleetmanager.repository.db.H2DatabaseManager;

/**
 * Classe creata per eseguire le operazioni sul database tramite codice SQL
 */
public class SQLoperator {


	public static void main(String[] args) throws SQLException {
		try (Connection conn = H2DatabaseManager.getInstance().getConnection(); Statement stmt = conn.createStatement()) {
			String sql = """
					DELETE FROM Utente
WHERE idUtente = 3;

		
					""";
			stmt.executeUpdate(sql);
			System.out.println("Codice eseguito correttamente");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}