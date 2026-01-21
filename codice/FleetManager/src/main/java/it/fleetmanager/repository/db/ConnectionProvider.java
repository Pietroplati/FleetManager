package it.fleetmanager.repository.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Astrazione minimale per ottenere connessioni JDBC.
 * Serve a disaccoppiare i DAO dall'implementazione concreta (H2, test, ecc.).
 */
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
