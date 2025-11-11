package it.fleetmanager.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTable {

	// File del database H2 (verrà creato in /db/fleetmanager.mv.db)
	private static final String DB_REL_FILE = "./data/fleetdb";
	private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;

	public static void main(String[] args) throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
			String sql = """
					CREATE TYPE tipo_veicolo AS ENUM('AUTO', 'MOTO' , 'FURGONE', 'CAMION');
					CREATE TYPE stato_veicolo AS ENUM('DISPONIBILE', 'PRENOTATO', 'IN_MANUTENZIONE', 'NON_DISPONIBILE');
					
					CREATE TABLE Veicolo (
					    targa VARCHAR(10) PRIMARY KEY,
					    tipoVeicolo tipo_veicolo NOT NULL,
					    marca VARCHAR(50) NOT NULL,
					    modello VARCHAR(50) NOT NULL,
					    annoImmatricolazione INT CHECK (annoImmatricolazione > 1900),
					    statoVeicolo stato_veicolo NOT NULL, 
					    km INT CHECK (km >= 0)
					);
					˙
					      """;
			stmt.executeUpdate(sql);
			System.out.println("Tabella Veicolo creata");
		}
			catch(SQLException e) {
				e.printStackTrace();
			}
	}
}