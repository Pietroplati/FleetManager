package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.util.DatabaseManager;
import it.fleetmanager.util.StatoVeicolo;
import it.fleetmanager.util.TipoVeicolo;

public class VeicoloDAOImpl implements VeicoloDAO {

	private final DatabaseManager db;

	public static final Veicolo VEICOLO_INESISTENTE = new Veicolo("N/A", null, "N/A", "N/A", -1,
			StatoVeicolo.NON_DISPONIBILE, -1) {
		@Override
		public String toString() {
			return "Veicolo inesistente";
		}
	};

	public VeicoloDAOImpl(DatabaseManager db) {
		this.db = db;
	}

	private Veicolo map(ResultSet rs) throws Exception {
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
	public void save(Veicolo v) {
		String sql = """
				INSERT INTO Veicolo
				(targa, tipoVeicolo, marca, modello, annoImmatricolazione, statoVeicolo, km)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, v.getTarga());
			ps.setString(2, v.getTipoVeicolo().name());
			ps.setString(3, v.getMarca());
			ps.setString(4, v.getModello());
			ps.setInt(5, v.getAnnoImmatricolazione());
			ps.setString(6, v.getStatoVeicolo().name());
			ps.setInt(7, v.getKm());

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL save: " + e.getMessage());
		}
	}

	@Override
	public void update(Veicolo v) {
		String sql = """
				UPDATE Veicolo SET
				    tipoVeicolo = ?, marca = ?, modello = ?,
				    annoImmatricolazione = ?, statoVeicolo = ?, km = ?
				WHERE targa = ?
				""";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, v.getTipoVeicolo().name());
			ps.setString(2, v.getMarca());
			ps.setString(3, v.getModello());
			ps.setInt(4, v.getAnnoImmatricolazione());
			ps.setString(5, v.getStatoVeicolo().name());
			ps.setInt(6, v.getKm());
			ps.setString(7, v.getTarga());

			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL update: " + e.getMessage());
		}
	}

	@Override
	public void delete(String targa) {
		String sql = "DELETE FROM Veicolo WHERE targa = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);
			ps.executeUpdate();

		} catch (Exception e) {
			System.err.println("ERRORE SQL delete: " + e.getMessage());
		}
	}

	@Override
	public Veicolo getVeicoloByTarga(String targa) {
		String sql = "SELECT * FROM Veicolo WHERE targa = ?";

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					return VEICOLO_INESISTENTE;
				return map(rs);
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL getVeicoloByTarga: " + e.getMessage());
			return VEICOLO_INESISTENTE;
		}
	}

	@Override
	public List<Veicolo> getTuttiVeicoli() {
		String sql = "SELECT * FROM Veicolo";
		List<Veicolo> list = new ArrayList<>();

		try (Connection conn = db.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next())
				list.add(map(rs));

		} catch (Exception e) {
			System.err.println("ERRORE SQL getTuttiVeicoli: " + e.getMessage());
		}

		return list;
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

		List<Veicolo> list = new ArrayList<>();

		try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, java.sql.Timestamp.valueOf(dataFine));
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(dataInizio));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					list.add(map(rs));
			}

		} catch (Exception e) {
			System.err.println("ERRORE SQL getDisponibili: " + e.getMessage());
		}

		return list;
	}
}
