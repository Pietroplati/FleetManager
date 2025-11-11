package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTable {

	private static final String DB_REL_FILE = "./data/fleetdb";
	private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;

	public static void main(String[] args) throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
			String sql = """

					CREATE TYPE tipo_notifica AS ENUM('SCADENZA', 'MANUTENZIONE', 'PRENOTAZIONE', 'SEGNALAZIONE');

					CREATE TABLE Notifica(
						idNotifica INT PRIMARY KEY,
						tipoNotifica tipo_notifica NOT NULL,
						messaggio VARCHAR(255) NOT NULL,
						dataInvio TIMESTAMP NOT NULL,
						letta BOOLEAN NOT NULL DEFAULT FALSE
					);

											""";
			stmt.executeUpdate(sql);
			System.out.println("Tabella Notifica creata");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}