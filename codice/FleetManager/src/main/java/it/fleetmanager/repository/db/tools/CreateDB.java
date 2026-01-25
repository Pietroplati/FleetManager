package it.fleetmanager.repository.db.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.repository.db.H2DatabaseManager;

public class CreateDB {

    private static final Logger logger = LogManager.getLogger(CreateDB.class);

    private static final String DB_REL_FILE = "./data/fleetdb";

    public static void main(String[] args) {

        try (Connection conn = H2DatabaseManager.getInstance().getConnection()) {

            DatabaseMetaData meta = conn.getMetaData();
            logger.info("Driver DB: {}", meta.getDriverName());
            logger.info("Nuovo database creato o aperto.");

            File dbFile = new File(DB_REL_FILE + ".mv.db");
            logger.debug("Il file esiste? {}", dbFile.exists());

        } catch (SQLException e) {
            logger.error("Errore durante la creazione/apertura del database H2.", e);
        }
    }
}
