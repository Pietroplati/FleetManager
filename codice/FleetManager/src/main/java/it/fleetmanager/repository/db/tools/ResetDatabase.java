package it.fleetmanager.repository.db.tools;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.repository.db.H2DatabaseManager;

public class ResetDatabase {

    private static final Logger LOGGER = LogManager.getLogger(ResetDatabase.class);

    public static void main(String[] args) {
        try (Connection conn = H2DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP ALL OBJECTS;");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Database H2 resettato completamente!");
            }

        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Errore durante il reset completo del database H2", e);
            }
        }
    }
}
