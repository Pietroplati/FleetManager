package it.fleetmanager.bootstrap.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tool di bootstrap/debug: stampa i contenuti delle tabelle.
 * NON dipende più da it.fleetmanager.repository.* (riduce Tangled)
 * Logica invariata: legge tutte le tabelle e stampa le righe.
 */
public class ElencaDati {

    private static final String SCHEMA = "PUBLIC";
    private static final int MAX_ROWS_PER_TABLE = 0;

    private static final int DEFAULT_COL_WIDTH = 25;
    private static final int MESSAGE_COL_WIDTH = 60;
    private static final int DESCRIZIONE_COL_WIDTH = 80;

    // ==========================
    // CONFIG DB (BOOTSTRAP ONLY)
    // ==========================
    //Metti qui gli stessi valori usati dal tuo H2DatabaseManager
    // (Se usi in-memory, usa jdbc:h2:mem:...; se usi file-based, usa jdbc:h2:./...)
    private static final String DB_URL  = "jdbc:h2:./fleetmanager;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
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

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static void printTableData(Connection conn, String table, int maxRows) {
        String sql = buildSql(table, maxRows);

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData md = rs.getMetaData();

            // Controllo tabella vuota + stampa prima riga (robusto e lineare)
            if (!rs.next()) {
                System.out.println("(tabella vuota)");
                return;
            }

            printHeader(md);
            // prima riga già “posizionata”
            printRow(rs, md);

            while (rs.next()) {
                printRow(rs, md);
            }

        } catch (SQLException e) {
            System.out.println("Errore leggendo i dati da " + table + ": " + e.getMessage());
        }
    }

    private static String buildSql(String table, int maxRows) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(table);
        if (maxRows > 0) {
            sb.append(" LIMIT ").append(maxRows);
        }
        return sb.toString();
    }

    private static void printHeader(ResultSetMetaData md) throws SQLException {
        int colCount = md.getColumnCount();
        StringBuilder header = new StringBuilder();

        for (int i = 1; i <= colCount; i++) {
            String colName = md.getColumnName(i);
            header.append(padRight(colName, getWidthForColumn(colName)));
        }

        System.out.println(header);
    }

    private static void printRow(ResultSet rs, ResultSetMetaData md) throws SQLException {
        int colCount = md.getColumnCount();
        StringBuilder row = new StringBuilder();

        for (int i = 1; i <= colCount; i++) {
            String colName = md.getColumnName(i);
            Object val = rs.getObject(i);
            row.append(padRight(val != null ? String.valueOf(val) : "NULL", getWidthForColumn(colName)));
        }

        System.out.println(row);
    }

    private static int getWidthForColumn(String colName) {
        if ("MESSAGGIO".equalsIgnoreCase(colName)) {
            return MESSAGE_COL_WIDTH;
        }
        if ("DESCRIZIONE".equalsIgnoreCase(colName)) {
            return DESCRIZIONE_COL_WIDTH;
        }
        return DEFAULT_COL_WIDTH;
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
        System.out.println(
            "──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }
}
