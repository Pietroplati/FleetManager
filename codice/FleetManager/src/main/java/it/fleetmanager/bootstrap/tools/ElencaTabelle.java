package it.fleetmanager.bootstrap.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.repository.db.H2DatabaseManager;

public class ElencaTabelle {

    private static final Logger logger = LogManager.getLogger(ElencaTabelle.class);

    public static void main(String[] args) {
        try (Connection conn = H2DatabaseManager.getInstance().getConnection()) {
            logger.info("SCHEMA DATABASE: {}", H2DatabaseManager.getUrl());
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rsTables = meta.getTables(null, "PUBLIC", "%", new String[] { "TABLE" })) {
                while (rsTables.next()) {
                    stampaTabella(meta, rsTables);
                }
            }

        } catch (SQLException e) {
            logger.error("Errore durante l'elenco delle tabelle", e);
        }
    }

    private static void stampaTabella(DatabaseMetaData meta, ResultSet rsTables) throws SQLException {
        String schema = rsTables.getString("TABLE_SCHEM");
        String table = rsTables.getString("TABLE_NAME");

        printLine();
        logger.info("→ {}.{}", schema, table);
        logger.info("   Colonne:");

        Map<String, String> pkCols = caricaPrimaryKeys(meta, schema, table);
        Map<String, String> fkRefs = caricaForeignKeys(meta, schema, table);

        stampaColonne(meta, schema, table, pkCols, fkRefs);
    }

    private static Map<String, String> caricaPrimaryKeys(DatabaseMetaData meta, String schema, String table)
            throws SQLException {

        Map<String, String> pkCols = new HashMap<>();

        try (ResultSet rsPK = meta.getPrimaryKeys(null, schema, table)) {
            while (rsPK.next()) {
                pkCols.put(rsPK.getString("COLUMN_NAME"), "PK");
            }
        }

        return pkCols;
    }

    private static Map<String, String> caricaForeignKeys(DatabaseMetaData meta, String schema, String table)
            throws SQLException {

        Map<String, String> fkRefs = new HashMap<>();

        try (ResultSet rsFK = meta.getImportedKeys(null, schema, table)) {
            while (rsFK.next()) {
                String fkCol = rsFK.getString("FKCOLUMN_NAME");
                String refTable = rsFK.getString("PKTABLE_NAME");
                String refCol = rsFK.getString("PKCOLUMN_NAME");
                fkRefs.put(fkCol, refTable + "." + refCol);
            }
        }

        return fkRefs;
    }

    private static void stampaColonne(DatabaseMetaData meta, String schema, String table, Map<String, String> pkCols,
            Map<String, String> fkRefs) throws SQLException {

        try (ResultSet rsCols = meta.getColumns(null, schema, table, "%")) {
            while (rsCols.next()) {

                String col = rsCols.getString("COLUMN_NAME");
                String type = rsCols.getString("TYPE_NAME");
                int size = rsCols.getInt("COLUMN_SIZE");
                String nullable = rsCols.getString("IS_NULLABLE");
                String autoinc = rsCols.getString("IS_AUTOINCREMENT");

                String info = descriviColonna(col, autoinc, nullable, pkCols, fkRefs);

                logger.info("   - {} ({ }[{}]) {}", padRight(col, 20), type, size, info);
            }
        }
    }

    private static String descriviColonna(String col, String autoinc, String nullable, Map<String, String> pkCols,
            Map<String, String> fkRefs) {

        if (pkCols.containsKey(col)) {
            return "Primary Key";
        }

        if (fkRefs.containsKey(col)) {
            return "Foreign Key → " + fkRefs.get(col);
        }

        if ("YES".equalsIgnoreCase(autoinc)) {
            return "AutoIncrement field";
        }

        if ("YES".equalsIgnoreCase(nullable)) {
            return "Nullable field";
        }

        return "Standard field";
    }

    private static void printLine() {
        logger.info(
                "────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);

        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        for (int i = s.length(); i < n; i++) sb.append(' ');
        return sb.toString();
    }
}
