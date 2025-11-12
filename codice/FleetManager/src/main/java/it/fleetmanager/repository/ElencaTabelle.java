package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ElencaTabelle {
	private static final String DB_REL_FILE = "./data/fleetdb";
	private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;

	public static void main(String[] args) throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL);) {
			DatabaseMetaData meta = conn.getMetaData();

			try (ResultSet rsTables = meta.getTables(null, "PUBLIC", "%", new String[] { "TABLE" })) {
				System.out.println("SCHEMA DATABASE: " + DB_URL);

				while (rsTables.next()) {
					String schema = rsTables.getString("TABLE_SCHEM");
					String table = rsTables.getString("TABLE_NAME");
					printLine();
					System.out.printf("→ %s.%s%n", schema, table);
					System.out.println("   Colonne:");
					

					// Raccolta PK e FK
					Map<String, String> pkCols = new HashMap<>();
					Map<String, String> fkRefs = new HashMap<>();

					try (ResultSet rsPK = meta.getPrimaryKeys(null, schema, table)) {
						while (rsPK.next()) {
							pkCols.put(rsPK.getString("COLUMN_NAME"), "PK");
						}
					}

					try (ResultSet rsFK = meta.getImportedKeys(null, schema, table)) {
						while (rsFK.next()) {
							String fkCol = rsFK.getString("FKCOLUMN_NAME");
							String refTable = rsFK.getString("PKTABLE_NAME");
							String refCol = rsFK.getString("PKCOLUMN_NAME");
							fkRefs.put(fkCol, refTable + "." + refCol);
						}
					}

					try (ResultSet rsCols = meta.getColumns(null, schema, table, "%")) {
						while (rsCols.next()) {
							String col = rsCols.getString("COLUMN_NAME");
							String type = rsCols.getString("TYPE_NAME");
							int size = rsCols.getInt("COLUMN_SIZE");
							String nullable = rsCols.getString("IS_NULLABLE");
							String autoinc = rsCols.getString("IS_AUTOINCREMENT");

							// Costruisce descrizione
							String info;
							if (pkCols.containsKey(col))
								info = "Primary Key";
							else if (fkRefs.containsKey(col))
								info = "Foreign Key → " + fkRefs.get(col);
							else if ("YES".equalsIgnoreCase(autoinc))
								info = "AutoIncrement field";
							else if ("YES".equalsIgnoreCase(nullable))
								info = "Nullable field";
							else
								info = "Standard field";

							System.out.printf("   - %-20s (%s[%d]) %s%n", col, type, size, info);
						}
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void printLine() {
		System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
	}
}
