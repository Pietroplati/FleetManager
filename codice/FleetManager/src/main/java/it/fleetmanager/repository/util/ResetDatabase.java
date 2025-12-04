package it.fleetmanager.repository.util;

import java.sql.Connection;
import java.sql.Statement;

public class ResetDatabase {

    public static void main(String[] args) {
        try (Connection conn = H2DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("DROP ALL OBJECTS;");
            System.out.println("Database H2 resettato completamente!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
