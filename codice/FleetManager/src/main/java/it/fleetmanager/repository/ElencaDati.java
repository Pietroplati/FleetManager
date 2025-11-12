package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ElencaDati {

    private static final String DB_REL_FILE = "./data/fleetdb";
    private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;
    private static final String SCHEMA = "PUBLIC";

    // Se vuoi limitare le righe stampate per tabella, imposta un valore > 0
    private static final int MAX_ROWS_PER_TABLE = 0; // 0 = tutte

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rsTables = meta.getTables(null, SCHEMA, "%", new String[] { "TABLE" })) {
                while (rsTables.next()) {
                    String table = rsTables.getString("TABLE_NAME");
                    printLine();
                    System.out.println("TABELLA: " + table);
                    printTableData(conn, table, MAX_ROWS_PER_TABLE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        printLine();
    }

    private static void printTableData(Connection conn, String table, int maxRows) {
        String sql = "SELECT * FROM " + table;
        if (maxRows > 0) {
            sql += " LIMIT " + maxRows;
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData md = rs.getMetaData();
            int colCount = md.getColumnCount();

            // Se non ci sono righe, esci silenziosamente
            if (!rs.isBeforeFirst()) {
                System.out.println("(tabella vuota)");
                return;
            }

            // Intestazioni di colonna
            StringBuilder header = new StringBuilder();
            for (int i = 1; i <= colCount; i++) {
                header.append(padRight(md.getColumnName(i), 25));
            }
            System.out.println(header.toString());

            // Righe dati
            while (rs.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    Object val = rs.getObject(i);
                    row.append(padRight(val != null ? String.valueOf(val) : "NULL", 25));
                }
                System.out.println(row.toString());
            }
        } catch (SQLException e) {
            System.out.println("Errore leggendo i dati da " + table + ": " + e.getMessage());
        }
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        for (int i = s.length(); i < n; i++) sb.append(' ');
        return sb.toString();
    }

    private static void printLine() {
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }
}
