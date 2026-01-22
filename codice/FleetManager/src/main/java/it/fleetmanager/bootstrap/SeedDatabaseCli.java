package it.fleetmanager.bootstrap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tool da riga di comando per popolare il database.
 * Esegue il seeding sul DB H2 salvato in ./data/fleetdb.mv.db (URL: jdbc:h2:./data/fleetdb)
 */
public final class SeedDatabaseCli {

    private static final Logger LOGGER =
            Logger.getLogger(SeedDatabaseCli.class.getName());

    private static final String JDBC_URL = "jdbc:h2:./data/fleetdb";

    private SeedDatabaseCli() {
        // utility class
    }

    public static void main(String[] args) {

        LOGGER.info("Avvio del popolamento del database...");

        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {

            DatabaseSeeder seeder = new DatabaseSeeder();
            seeder.seedFromJson(conn);

            LOGGER.info("Database popolato correttamente!");

        } catch (Exception e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Errore durante il seeding del database",
                    e
            );
        }
    }
}
