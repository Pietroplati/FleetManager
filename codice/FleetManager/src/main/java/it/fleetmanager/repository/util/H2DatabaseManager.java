package it.fleetmanager.repository.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2DatabaseManager implements DatabaseManager {

	private static final String DEFAULT_URL = "jdbc:h2:./data/fleetdb;AUTO_RECONNECT=TRUE;MODE=PostgreSQL";
	private static String testUrl = null;

	private static H2DatabaseManager instance;

	private H2DatabaseManager() {
	}

	public static synchronized H2DatabaseManager getInstance() {
		if (instance == null) {
			instance = new H2DatabaseManager();
		}
		return instance;
	}

	public static void setTestUrl(String url) {
		testUrl = url;
	}

	@Override
	public Connection getConnection() throws SQLException {
		String url = (testUrl != null) ? testUrl : DEFAULT_URL;

		Connection conn = DriverManager.getConnection(url);

		// ⭐ ASSICURA CHE OGNI UPDATE SIA IMMEDIATO
		conn.setAutoCommit(true);

		return conn;
	}

	public static String getUrl() {
		return (testUrl != null) ? testUrl : DEFAULT_URL;
	}
}
