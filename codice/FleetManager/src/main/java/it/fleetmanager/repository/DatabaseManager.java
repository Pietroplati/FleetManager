package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

	private static final String DEFAULT_URL = "jdbc:h2:./data/fleetdb";

	private static String testUrl = null;

	private static DatabaseManager instance;

	private DatabaseManager() {
	}

	public static synchronized DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	public static void setTestUrl(String url) {
		testUrl = url;
	}

	public Connection getConnection() throws SQLException {

		if (testUrl != null) {
			return DriverManager.getConnection(testUrl);
		}

		return DriverManager.getConnection(DEFAULT_URL);
	}

	public static String getUrl() {
		return (testUrl != null) ? testUrl : DEFAULT_URL;
	}
}
