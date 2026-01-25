package it.fleetmanager.repository.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestore delle connessioni H2.
 * 
 * LOGICA :
 * - singleton
 * - supporto testUrl
 * - autoCommit = true
 * - stessa JDBC URL
 *
 * Nota: la Connection NON viene chiusa qui perché questo
 * è un provider; la responsabilità della chiusura è del chiamante.
 */

public final class H2DatabaseManager implements ConnectionProvider {

    private static final String DEFAULT_URL =
            "jdbc:h2:./data/fleetdb;AUTO_RECONNECT=TRUE;MODE=PostgreSQL";

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
    @SuppressWarnings("resource")
    public Connection getConnection() throws SQLException {
        String url = (testUrl != null) ? testUrl : DEFAULT_URL;

        Connection conn = DriverManager.getConnection(url);
        conn.setAutoCommit(true);

        return conn;
    }

    public static String getUrl() {
        return (testUrl != null) ? testUrl : DEFAULT_URL;
    }
}
