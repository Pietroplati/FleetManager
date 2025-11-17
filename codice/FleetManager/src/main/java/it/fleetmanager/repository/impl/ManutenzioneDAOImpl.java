package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.ManutenzioneDAO;
import it.fleetmanager.util.TipoManutenzione;

public class ManutenzioneDAOImpl implements ManutenzioneDAO {

	public static final Manutenzione MANUTENZIONE_INESISTENTE = new Manutenzione(-1, LocalDateTime.MIN,
			TipoManutenzione.ORDINARIA, "N/A", "N/A") {

		@Override
		public String toString() {
			return "Manutenzione inesistente";
		}
	};

	@Override
	public void save(Manutenzione manutenzione) {

		String sql = "INSERT INTO Manutenzione " + "(idManutenzione, data, tipoManutenzione, descrizione, targa) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, manutenzione.getIdManutenzione());
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(manutenzione.getData()));
			ps.setString(3, manutenzione.getTipoManutenzione().name());
			ps.setString(4, manutenzione.getDescrizione());
			ps.setString(5, manutenzione.getTarga());

			ps.executeUpdate();
			System.out.println("Manutenzione inserita correttamente nel database H2!");

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante save(manutenzione): " + e.getMessage());
		}
	}

	@Override
	public void update(Manutenzione manutenzione) {

		String sql = "UPDATE Manutenzione SET " + "data = ?, " + "tipoManutenzione = ?, " + "descrizione = ?, "
				+ "targa = ? " + "WHERE idManutenzione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, java.sql.Timestamp.valueOf(manutenzione.getData()));
			ps.setString(2, manutenzione.getTipoManutenzione().name());
			ps.setString(3, manutenzione.getDescrizione());
			ps.setString(4, manutenzione.getTarga());
			ps.setInt(5, manutenzione.getIdManutenzione());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println(
						"Manutenzione aggiornata correttamente (ID: " + manutenzione.getIdManutenzione() + ")");
			} else {
				System.err.println("Nessuna manutenzione trovata con ID " + manutenzione.getIdManutenzione());
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante update(manutenzione): " + e.getMessage());
		}
	}

	@Override
	public void delete(int idManutenzione) {

		String sql = "DELETE FROM Manutenzione WHERE idManutenzione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idManutenzione);

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out
						.println("Manutenzione con ID " + idManutenzione + " eliminata correttamente dal database H2!");
			} else {
				System.err.println(
						"Nessuna manutenzione trovata con ID " + idManutenzione + ". Nessuna eliminazione effettuata.");
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante delete(" + idManutenzione + "): " + e.getMessage());
		}
	}

	@Override
	public Manutenzione getManutenzioneById(int idManutenzione) {

		String sql = "SELECT idManutenzione, data, tipoManutenzione, descrizione, targa "
				+ "FROM Manutenzione WHERE idManutenzione = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, idManutenzione);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					System.err.println("Nessuna manutenzione trovata con ID " + idManutenzione);
					return MANUTENZIONE_INESISTENTE;
				}

				int id = rs.getInt("idManutenzione");
				LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
				TipoManutenzione tipo = TipoManutenzione.valueOf(rs.getString("tipoManutenzione"));
				String descrizione = rs.getString("descrizione");
				String targa = rs.getString("targa");

				return new Manutenzione(id, data, tipo, descrizione, targa);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante getManutenzioneById: " + e.getMessage());
			return MANUTENZIONE_INESISTENTE;
		}
	}

	@Override
	public List<Manutenzione> findByVeicolo(String targa) {

		String sql = "SELECT idManutenzione, data, tipoManutenzione, descrizione, targa "
				+ "FROM Manutenzione WHERE targa = ? ORDER BY data ASC";

		List<Manutenzione> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int id = rs.getInt("idManutenzione");
					LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
					TipoManutenzione tipo = TipoManutenzione.valueOf(rs.getString("tipoManutenzione"));
					String descrizione = rs.getString("descrizione");
					String targaDb = rs.getString("targa");

					Manutenzione m = new Manutenzione(id, data, tipo, descrizione, targaDb);
					lista.add(m);
				}
			}

			if (lista.isEmpty()) {
				System.out.println("Nessuna manutenzione trovata per il veicolo con targa " + targa);
			} else {
				System.out.println("Trovate " + lista.size() + " manutenzioni per il veicolo con targa " + targa);
			}

			return lista;

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByVeicolo(" + targa + "): " + e.getMessage());
			return lista;
		}
	}

	@Override
	public List<Manutenzione> findByTipo(TipoManutenzione tipoManutenzione) {

		String sql = "SELECT idManutenzione, data, tipoManutenzione, descrizione, targa "
				+ "FROM Manutenzione WHERE tipoManutenzione = ? ORDER BY data ASC";

		List<Manutenzione> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, tipoManutenzione.name());

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {

					int id = rs.getInt("idManutenzione");
					LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
					TipoManutenzione tipo = TipoManutenzione.valueOf(rs.getString("tipoManutenzione"));
					String descrizione = rs.getString("descrizione");
					String targa = rs.getString("targa");

					lista.add(new Manutenzione(id, data, tipo, descrizione, targa));
				}
			}

			if (lista.isEmpty()) {
				System.out.println("Nessuna manutenzione trovata per tipo " + tipoManutenzione);
			} else {
				System.out.println("Trovate " + lista.size() + " manutenzioni per tipo " + tipoManutenzione);
			}

			return lista;

		} catch (Exception e) {
			System.err.println("ERRORE SQL durante findByTipo(" + tipoManutenzione + "): " + e.getMessage());
			return lista;
		}
	}

}
