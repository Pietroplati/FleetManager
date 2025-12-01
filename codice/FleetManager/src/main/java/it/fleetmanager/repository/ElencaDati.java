/*package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class ElencaDati {

	private static final String SCHEMA = "PUBLIC";

	private static final int MAX_ROWS_PER_TABLE = 0;

	private static final int DEFAULT_COL_WIDTH = 25;
	private static final int MESSAGE_COL_WIDTH = 60;
	private static final int DESCRIZIONE_COL_WIDTH = 80;

	public static void main(String[] args) {
		try (Connection conn = H2DatabaseManager.getInstance().getConnection()) {
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

		try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

			ResultSetMetaData md = rs.getMetaData();
			int colCount = md.getColumnCount();

			if (!rs.isBeforeFirst()) {
				System.out.println("(tabella vuota)");
				return;
			}

			// Intestazioni di colonna
			StringBuilder header = new StringBuilder();
			for (int i = 1; i <= colCount; i++) {
				String colName = md.getColumnName(i);
				header.append(padRight(colName, getWidthForColumn(colName)));
			}
			System.out.println(header.toString());

			// Righe dati
			while (rs.next()) {
				StringBuilder row = new StringBuilder();
				for (int i = 1; i <= colCount; i++) {
					String colName = md.getColumnName(i);
					Object val = rs.getObject(i);
					row.append(padRight(val != null ? String.valueOf(val) : "NULL", getWidthForColumn(colName)));
				}
				System.out.println(row.toString());
			}
		} catch (SQLException e) {
			System.out.println("Errore leggendo i dati da " + table + ": " + e.getMessage());
		}
	}

	private static int getWidthForColumn(String colName) {
		if (colName.equalsIgnoreCase("MESSAGGIO")) {
			return MESSAGE_COL_WIDTH;
		}
		if (colName.equalsIgnoreCase("DESCRIZIONE")) {
			return DESCRIZIONE_COL_WIDTH;
		}
		return DEFAULT_COL_WIDTH;
	}

	private static String padRight(String s, int n) {
		if (s == null)
			s = "";
		if (s.length() >= n)
			return s.substring(0, n);
		StringBuilder sb = new StringBuilder(n);
		sb.append(s);
		for (int i = s.length(); i < n; i++)
			sb.append(' ');
		return sb.toString();
	}

	private static void printLine() {
		System.out.println(
				"──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────");
	}
}


*/
