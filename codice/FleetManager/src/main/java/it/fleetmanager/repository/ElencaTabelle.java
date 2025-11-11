package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ElencaTabelle {
	private static final String DB_REL_FILE = "./data/fleetdb";
	private static final String DB_URL = "jdbc:h2:" + DB_REL_FILE;

	public static void main(String[] args) throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL);) {
			DatabaseMetaData meta = conn.getMetaData();

			try (ResultSet rsTables = meta.getTables(null, "PUBLIC", "%", new String[] { "TABLE" })) {
				System.out.println("🔹 Tabelle trovate nel database:");
				while (rsTables.next()) {
					String schema = rsTables.getString("TABLE_SCHEM");
					String table = rsTables.getString("TABLE_NAME");
					System.out.println("\n→ " + schema + "." + table);

					try (ResultSet rsCols = meta.getColumns(null, schema, table, "%")) {
						System.out.println("   Colonne:");
						while (rsCols.next()) {
							String colName = rsCols.getString("COLUMN_NAME");
							String typeName = rsCols.getString("TYPE_NAME");
							int size = rsCols.getInt("COLUMN_SIZE");
							String isNullable = rsCols.getString("IS_NULLABLE");
							String isAutoInc = rsCols.getString("IS_AUTOINCREMENT");

							System.out.printf("     - %s (%s[%d]) Nullable: %s AutoIncrement: %s%n", colName, typeName,
									size, isNullable, isAutoInc);
						}
					}

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
