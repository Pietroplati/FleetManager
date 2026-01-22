package it.fleetmanager.bootstrap.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connessione standalone usata SOLO dai tool di bootstrap/debug.
 * Evita dipendenze verso repository/db (riduce tangled).
 */
public final class BootstrapDb {

    private BootstrapDb() {}

    // Metti qui gli stessi valori che usi in H2DatabaseManager
    // (se usi file-based, metti il path; se usi in-memory, metti quello).
    private static final String URL  = "jdbc:h2:./fleetmanager;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    // H2 embedded DB senza password: uso locale / didattico
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
