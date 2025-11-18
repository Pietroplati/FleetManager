package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.DatabaseManager;
import it.fleetmanager.repository.VeicoloDAO;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

public class VeicoloDAOImpl implements VeicoloDAO {

	public static final Veicolo VEICOLO_INESISTENTE = new Veicolo("N/A", null, "N/A", "N/A", -1,
			StatoVeicolo.NON_DISPONIBILE, -1) {
		@Override
		public String toString() {
			return "Veicolo inesistente";
		}
	};

	private Veicolo mapVeicolo(ResultSet rs) throws Exception {

		String targa = rs.getString("targa");
		TipoVeicolo tipo = TipoVeicolo.valueOf(rs.getString("tipoVeicolo"));
		String marca = rs.getString("marca");
		String modello = rs.getString("modello");

		Integer anno = rs.getObject("annoImmatricolazione", Integer.class);
		Integer km = rs.getObject("km", Integer.class);

		StatoVeicolo stato = StatoVeicolo.valueOf(rs.getString("statoVeicolo"));

		return new Veicolo(targa, tipo, marca, modello, anno, stato, km);
	}

	@Override
	public void save(Veicolo veicolo) {

		String sql = """
				INSERT INTO Veicolo
				(targa, tipoVeicolo, marca, modello, annoImmatricolazione, statoVeicolo, km)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, veicolo.getTarga());
			ps.setString(2, veicolo.getTipoVeicolo().name());
			ps.setString(3, veicolo.getMarca());
			ps.setString(4, veicolo.getModello());
			ps.setInt(5, veicolo.getAnnoImmatricolazione());
			ps.setString(6, veicolo.getStatoVeicolo().name());
			ps.setInt(7, veicolo.getKm());

			ps.executeUpdate();

			System.out.println("Veicolo inserito: " + veicolo.getTarga());

		} catch (Exception e) {
			System.err.println("Errore durante save(veicolo): " + e.getMessage());
		}
	}

	@Override
	public void update(Veicolo veicolo) {

		String sql = """
				UPDATE Veicolo SET
				    tipoVeicolo = ?, marca = ?, modello = ?,
				    annoImmatricolazione = ?, statoVeicolo = ?, km = ?
				WHERE targa = ?
				""";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, veicolo.getTipoVeicolo().name());
			ps.setString(2, veicolo.getMarca());
			ps.setString(3, veicolo.getModello());
			ps.setInt(4, veicolo.getAnnoImmatricolazione());
			ps.setString(5, veicolo.getStatoVeicolo().name());
			ps.setInt(6, veicolo.getKm());
			ps.setString(7, veicolo.getTarga());

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Veicolo aggiornato: " + veicolo.getTarga());
			} else {
				System.out.println("Veicolo non trovato: " + veicolo.getTarga());
			}

		} catch (Exception e) {
			System.err.println("Errore durante update(veicolo): " + e.getMessage());
		}
	}

	@Override
	public void delete(String targa) {

		String sql = "DELETE FROM Veicolo WHERE targa = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			int rows = ps.executeUpdate();

			if (rows > 0) {
				System.out.println("Veicolo eliminato: " + targa);
			} else {
				System.out.println("Veicolo non trovato: " + targa);
			}

		} catch (Exception e) {
			System.err.println("Errore durante delete(veicolo): " + e.getMessage());
		}
	}

	@Override
	public Veicolo getVeicoloByTarga(String targa) {

		String sql = "SELECT * FROM Veicolo WHERE targa = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next())
					return VEICOLO_INESISTENTE;

				return mapVeicolo(rs);
			}

		} catch (Exception e) {
			System.err.println("Errore durante getVeicoloByTarga: " + e.getMessage());
			return VEICOLO_INESISTENTE;
		}
	}

	@Override
	public List<Veicolo> getTuttiVeicoli() {

		String sql = "SELECT * FROM Veicolo";

		List<Veicolo> lista = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				lista.add(mapVeicolo(rs));
			}

		} catch (Exception e) {
			System.err.println("Errore durante getTuttiVeicoli: " + e.getMessage());
		}

		return lista;
	}

	@Override
	public List<Veicolo> getDisponibili(LocalDateTime dataInizio, LocalDateTime dataFine) {

		String sql = """
				SELECT v.*
				FROM Veicolo v
				WHERE NOT EXISTS (
				    SELECT 1
				    FROM Prenotazione p
				    WHERE p.targa = v.targa
				      AND p.dataInizio < ?
				      AND p.dataFine > ?
				)
				""";

		List<Veicolo> disponibili = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, java.sql.Timestamp.valueOf(dataFine));
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(dataInizio));

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					disponibili.add(mapVeicolo(rs));
				}
			}

		} catch (Exception e) {
			System.err.println("Errore durante getDisponibili: " + e.getMessage());
		}

		return disponibili;
	}
}
