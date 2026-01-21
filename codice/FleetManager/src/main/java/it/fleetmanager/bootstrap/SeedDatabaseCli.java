package it.fleetmanager.bootstrap;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Tool da riga di comando per popolare il database.
 * Esegue il seeding sul DB H2 salvato in ./data/fleetdb.mv.db (URL: jdbc:h2:./data/fleetdb)
 */
public final class SeedDatabaseCli {

    private static final String JDBC_URL = "jdbc:h2:./data/fleetdb";

    private SeedDatabaseCli() {
        // utility class
    }

    public static void main(String[] args) {
        System.out.println("Avvio del popolamento del database...");

        try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
            DatabaseSeeder seeder = new DatabaseSeeder();
            seeder.seedFromJson(conn);
            System.out.println("Database popolato correttamente!");
        } catch (Exception e) {
            System.err.println("Errore durante il seeding del database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
