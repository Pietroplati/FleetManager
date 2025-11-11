package it.fleetmanager.repository;


import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CreateDB {

    private static final String DB_REL_FILE = "./data/fleetdb";
    private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("Driver: " + meta.getDriverName());
        System.out.println("Nuovo database creato o aperto.");

        File dbFile = new File(DB_REL_FILE + ".mv.db");
        System.out.println("Il file esiste? " + dbFile.exists());
        conn.close();
    }
}