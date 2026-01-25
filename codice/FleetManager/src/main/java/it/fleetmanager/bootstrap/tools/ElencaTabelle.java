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

/**
 * Utility di bootstrap che elenca dinamicamente tutte le tabelle presenti
 * nello schema {@code PUBLIC} del database H2, stampando per ciascuna tabella
 * le colonne, il tipo, la dimensione e le eventuali chiavi primarie o esterne.
 *
 * <p>
 * La classe utilizza {@link DatabaseMetaData} per interrogare il catalogo del
 * database ed è pensata come strumento di supporto per il debug e la verifica
 * della struttura del database durante le fasi di sviluppo.
 * </p>
 */
public class ElencaTabelle {

    /**
     * Logger di classe.
     */
    private static final Logger LOGGER = LogManager.getLogger(ElencaTabelle.class);

    /**
     * Punto di ingresso dell'applicazione.
     * <p>
     * Stabilisce una connessione al database e avvia la procedura di lettura
     * delle tabelle presenti nello schema.
     * </p>
     *
     * @param args argomenti da linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        try (Connection conn = H2DatabaseManager.getInstance().getConnection()) {
            LOGGER.info("SCHEMA DATABASE: {}", H2DatabaseManager.getUrl());
            DatabaseMetaData meta = conn.getMetaData();

            try (ResultSet rsTables = meta.getTables(null, "PUBLIC", "%", new String[] { "TABLE" })) {
                while (rsTables.next()) {
                    stampaTabella(meta, rsTables);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Errore durante l'elenco delle tabelle", e);
        }
    }

    /**
     * Stampa le informazioni principali di una tabella e delle sue colonne.
     *
     * @param meta     metadati del database
     * @param rsTables result set posizionato sulla tabella corrente
     * @throws SQLException in caso di errore di accesso ai metadati
     */
    private static void stampaTabella(DatabaseMetaData meta, ResultSet rsTables) throws SQLException {
        String schema = rsTables.getString("TABLE_SCHEM");
        String table = rsTables.getString("TABLE_NAME");

        printLine();
        LOGGER.info("→ {}.{}", schema, table);
        LOGGER.info("   Colonne:");

        Map<String, String> pkCols = caricaPrimaryKeys(meta, schema, table);
        Map<String, String> fkRefs = caricaForeignKeys(meta, schema, table);

        stampaColonne(meta, schema, table, pkCols, fkRefs);
    }

    /**
     * Recupera le colonne che costituiscono la chiave primaria di una tabella.
     *
     * @param meta   metadati del database
     * @param schema schema della tabella
     * @param table  nome della tabella
     * @return mappa contenente le colonne di chiave primaria
     * @throws SQLException in caso di errore di accesso ai metadati
     */
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

    /**
     * Recupera le chiavi esterne di una tabella e le relative referenze.
     *
     * @param meta   metadati del database
     * @param schema schema della tabella
     * @param table  nome della tabella
     * @return mappa colonna → tabella.colonna referenziata
     * @throws SQLException in caso di errore di accesso ai metadati
     */
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

    /**
     * Stampa tutte le colonne di una tabella con le relative informazioni.
     *
     * @param meta    metadati del database
     * @param schema  schema della tabella
     * @param table   nome della tabella
     * @param pkCols  colonne di chiave primaria
     * @param fkRefs  colonne di chiave esterna
     * @throws SQLException in caso di errore di accesso ai metadati
     */
    private static void stampaColonne(DatabaseMetaData meta, String schema, String table,
                                      Map<String, String> pkCols, Map<String, String> fkRefs)
            throws SQLException {

        try (ResultSet rsCols = meta.getColumns(null, schema, table, "%")) {
            while (rsCols.next()) {

                String col = rsCols.getString("COLUMN_NAME");
                String type = rsCols.getString("TYPE_NAME");
                int size = rsCols.getInt("COLUMN_SIZE");
                String nullable = rsCols.getString("IS_NULLABLE");
                String autoinc = rsCols.getString("IS_AUTOINCREMENT");

                String info = descriviColonna(col, autoinc, nullable, pkCols, fkRefs);

                LOGGER.info("   - {} ({ }[{}]) {}", padRight(col, 20), type, size, info);
            }
        }
    }

    /**
     * Determina la descrizione semantica di una colonna.
     *
     * @param col      nome della colonna
     * @param autoinc  indicatore di autoincremento
     * @param nullable indicatore di nullabilità
     * @param pkCols   colonne di chiave primaria
     * @param fkRefs   colonne di chiave esterna
     * @return descrizione della colonna
     */
    private static String descriviColonna(String col, String autoinc, String nullable,
                                          Map<String, String> pkCols, Map<String, String> fkRefs) {

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

    /**
     * Stampa una linea separatrice per migliorare la leggibilità dell'output.
     */
    private static void printLine() {
        LOGGER.info(
                "────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    /**
     * Effettua il padding a destra di una stringa fino alla lunghezza indicata.
     *
     * @param s stringa di input
     * @param n lunghezza desiderata
     * @return stringa con padding a destra
     */
    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);

        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        for (int i = s.length(); i < n; i++) sb.append(' ');
        return sb.toString();
    }
}
