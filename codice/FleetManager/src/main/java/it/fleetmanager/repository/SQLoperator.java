package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe creata per eseguire le operazioni sul database tramite codice SQL
 */
public class SQLoperator {


	public static void main(String[] args) throws SQLException {
		try (Connection conn = DatabaseManager.getInstance().getConnection(); Statement stmt = conn.createStatement()) {
			String sql = """
					DELETE FROM utente
					WHERE idutente = 4;
		
					""";
			stmt.executeUpdate(sql);
			System.out.println("Codice eseguito correttamente");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}