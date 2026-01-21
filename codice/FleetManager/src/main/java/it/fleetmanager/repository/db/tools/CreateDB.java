package it.fleetmanager.repository.db.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import it.fleetmanager.repository.db.H2DatabaseManager;

public class CreateDB {

    private static final String DB_REL_FILE = "./data/fleetdb";

    public static void main(String[] args) throws SQLException {
    	
    	try {
    		Connection conn = H2DatabaseManager.getInstance().getConnection();

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Driver: " + meta.getDriverName());
            System.out.println("Nuovo database creato o aperto.");

            File dbFile = new File(DB_REL_FILE + ".mv.db");
            System.out.println("Il file esiste? " + dbFile.exists());
            conn.close();
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	}
    	
    	
    }
}