package it.fleetmanager.bootstrap.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.fleetmanager.repository.db.H2DatabaseManager;

/**
 * Classe creata per eseguire operazioni SQL manuali sul database
 */
public class SQLoperator {

    private static final Logger LOGGER =
            Logger.getLogger(SQLoperator.class.getName());

    public static void main(String[] args) {

        try (Connection conn = H2DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = """
                DELETE FROM Utente
                WHERE idUtente = 3
                """;

            stmt.executeUpdate(sql);

            LOGGER.info("Codice eseguito correttamente.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore SQL durante l'esecuzione", e);
        }
    }
}
