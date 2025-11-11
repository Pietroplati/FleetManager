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

					
					ALTER TABLE Prenotazione
ADD CONSTRAINT FK_Prenotazione_Veicolo
FOREIGN KEY (targa)
REFERENCES Veicolo(targa);


																""";
			stmt.executeUpdate(sql);
			System.out.println("FK Aggiunta");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}