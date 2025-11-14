package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

	private static final String URL = "jdbc:h2:./data/fleetdb";

	private static DatabaseManager instance;

	private DatabaseManager() {
	}

	public static synchronized DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL);
	}
	public static String getUrl() {
		return URL;
	}
}
