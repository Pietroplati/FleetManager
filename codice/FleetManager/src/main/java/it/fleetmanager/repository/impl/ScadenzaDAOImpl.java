package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import it.fleetmanager.model.Scadenza;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.ScadenzaDAO;
import it.fleetmanager.util.TipoScadenza;

public class ScadenzaDAOImpl implements ScadenzaDAO {

	public static final Scadenza SCADENZA_INESISTENTE = new Scadenza(-1, TipoScadenza.BOLLO, LocalDate.MIN, false,
			"N/A") {
		@Override
		public String toString() {
			return "Scadenza inesistente";
		}
	};

	@Override
	public void save(Scadenza scadenza) {

		String sql = "INSERT INTO Scadenza " + "(idScadenza, tipoScadenza, data, notificata, targa) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, scadenza.getIdScadenza());
			ps.setString(2, scadenza.getTipoScadenza().name());
			ps.setDate(3, java.sql.Date.valueOf(scadenza.getData()));
			ps.setBoolean(4, scadenza.getNotificata());
			ps.setString(5, scadenza.getTarga());

			ps.executeUpdate();
			System.out.println("Scadenza inserita correttamente nel database H2");

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante save(scadenza): " + e.getMessage());
		}
	}

	@Override
	public void update(Scadenza scadenza) {

		String sql = "UPDATE Scadenza SET " + "tipoScadenza = ?, " + "data = ?, " + "notificata = ?, " + "targa = ? "
				+ "WHERE idScadenza = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, scadenza.getTipoScadenza().name());
			ps.setDate(2, java.sql.Date.valueOf(scadenza.getData()));
			ps.setBoolean(3, scadenza.getNotificata());
			ps.setString(4, scadenza.getTarga());
			ps.setInt(5, scadenza.getIdScadenza());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Scadenza aggiornata correttamente (ID: " + scadenza.getIdScadenza() + ")");
			} else {
				System.err.println("Nessuna scadenza trovata per ID " + scadenza.getIdScadenza());
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante update(scadenza): " + e.getMessage());
		}
	}

	@Override
	public void delete(int idScadenza) {

	    String sql = "DELETE FROM Scadenza WHERE idScadenza = ?";

	    try (Connection conn = DatabaseManager.getInstance().getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setInt(1, idScadenza);

	        int rows = ps.executeUpdate();

	        if (rows > 0) {
	            System.out.println("Scadenza con ID " + idScadenza + " eliminata correttamente dal database H2!");
	        } else {
	            System.err.println("Nessuna scadenza trovata con ID " + idScadenza + ". Nessuna eliminazione effettuata.");
	        }

	    } catch (Exception e) {
	        System.err.println("ERRORE SQL durante delete(" + idScadenza + "): " + e.getMessage());
	    }
	}


	@Override
	public Scadenza getScadenzaById(int idScadenza) {

		String sql = "SELECT idScadenza, tipoScadenza, data, notificata, targa " + "FROM Scadenza WHERE idScadenza = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idScadenza);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					System.err.println("⚠ Nessuna scadenza trovata con ID " + idScadenza);
					return SCADENZA_INESISTENTE;
				}

				int id = rs.getInt("idScadenza");
				String tipoDb = rs.getString("tipoScadenza");
				TipoScadenza tipo = TipoScadenza.valueOf(tipoDb);

				LocalDate data = rs.getDate("data").toLocalDate();

				boolean notificata = rs.getBoolean("notificata");
				String targa = rs.getString("targa");

				return new Scadenza(id, tipo, data, notificata, targa);
			}

		} catch (Exception e) {
			System.err.println("❌ ERRORE SQL durante getScadenzaById: " + e.getMessage());
			return SCADENZA_INESISTENTE;
		}
	}

	@Override
	public List<Scadenza> findProssimeScadenze(LocalDate finoA) {

		String sql = "SELECT idScadenza, tipoScadenza, data, notificata, targa " + "FROM Scadenza "
				+ "WHERE data >= CURRENT_DATE AND data <= ? " + "ORDER BY data ASC";

		List<Scadenza> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, java.sql.Date.valueOf(finoA));

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int id = rs.getInt("idScadenza");
					TipoScadenza tipo = TipoScadenza.valueOf(rs.getString("tipoScadenza"));
					LocalDate data = rs.getDate("data").toLocalDate();
					boolean notificata = rs.getBoolean("notificata");
					String targa = rs.getString("targa");

					lista.add(new Scadenza(id, tipo, data, notificata, targa));
				}
			}

			if (lista.isEmpty()) {
				System.out.println("Nessuna scadenza trovata da oggi fino al " + finoA);
			} else {
				System.out.println("Trovate " + lista.size() + " scadenze da oggi fino al " + finoA);
			}

			return lista;

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findProssimeScadenze: " + e.getMessage());
			return lista;
		}
	}

	@Override
	public List<Scadenza> findByVeicolo(String targa) {

		String sql = "SELECT idScadenza, tipoScadenza, data, notificata, targa "
				+ "FROM Scadenza WHERE targa = ? ORDER BY data ASC";

		List<Scadenza> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int id = rs.getInt("idScadenza");
					TipoScadenza tipo = TipoScadenza.valueOf(rs.getString("tipoScadenza"));
					LocalDate data = rs.getDate("data").toLocalDate();
					boolean notificata = rs.getBoolean("notificata");
					String targaDb = rs.getString("targa");

					Scadenza s = new Scadenza(id, tipo, data, notificata, targaDb);
					lista.add(s);
				}
			}

			if (lista.isEmpty()) {
				System.out.println("Nessuna scadenza trovata per il veicolo con targa " + targa);
			} else {
				System.out.println("Trovate " + lista.size() + " scadenze per il veicolo con targa " + targa);
			}

			return lista;

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByVeicolo: " + e.getMessage());
			return lista;
		}
	}

}
