package it.fleetmanager.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
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

	@Override
	public Veicolo getVeicoloByTarga(String targa) {

		String sql = "SELECT targa, tipoVeicolo, marca, modello, annoImmatricolazione, "
				+ "statoVeicolo, km FROM Veicolo WHERE targa = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return VEICOLO_INESISTENTE;
				}

				String targaDb = rs.getString("targa");
				String tipoDb = rs.getString("tipoVeicolo");
				String marca = rs.getString("marca");
				String modello = rs.getString("modello");

				Integer anno = rs.getObject("annoImmatricolazione", Integer.class);
				Integer km = rs.getObject("km", Integer.class);

				String statoDb = rs.getString("statoVeicolo");

				TipoVeicolo tipo = TipoVeicolo.valueOf(tipoDb);
				StatoVeicolo stato = StatoVeicolo.valueOf(statoDb);

				return new Veicolo(targaDb, tipo, marca, modello, anno, stato, km);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return VEICOLO_INESISTENTE;
		}
	}

	@Override
	public List<Veicolo> getTuttiVeicoli() {

		String sql = "SELECT targa, tipoVeicolo, marca, modello, annoImmatricolazione, "
				+ "statoVeicolo, km FROM Veicolo";

		List<Veicolo> veicoli = new ArrayList<>();

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {

				String targa = rs.getString("targa");
				String tipoDb = rs.getString("tipoVeicolo");
				String marca = rs.getString("marca");
				String modello = rs.getString("modello");

				int anno = rs.getInt("annoImmatricolazione");
				int km = rs.getInt("km");

				String statoDb = rs.getString("statoVeicolo");

				TipoVeicolo tipo = TipoVeicolo.valueOf(tipoDb);
				StatoVeicolo stato = StatoVeicolo.valueOf(statoDb);

				Veicolo v = new Veicolo(targa, tipo, marca, modello, anno, stato, km);

				veicoli.add(v);
			}

			return veicoli;

		} catch (Exception e) {
			e.printStackTrace();
			return veicoli; // restituiamo comunque la lista, anche se vuota
		}
	}

	@Override
	public List<Veicolo> getDisponibili(LocalDate dataInizio, LocalDate dataFine) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Veicolo veicolo) {

		String sql = "INSERT INTO Veicolo "
				+ "(targa, tipoVeicolo, marca, modello, annoImmatricolazione, statoVeicolo, km) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

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

			System.out.println("Veicolo inserito correttamente: " + veicolo.getTarga());

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Errore durante l'inserimento del veicolo: " + veicolo.getTarga());
		}
	}

	@Override
	public void update(Veicolo veicolo) {

		String sql = "UPDATE Veicolo SET " + "tipoVeicolo = ?, " + "marca = ?, " + "modello = ?, "
				+ "annoImmatricolazione = ?, " + "statoVeicolo = ?, " + "km = ? " + "WHERE targa = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, veicolo.getTipoVeicolo().name());
			ps.setString(2, veicolo.getMarca());
			ps.setString(3, veicolo.getModello());
			ps.setInt(4, veicolo.getAnnoImmatricolazione());
			ps.setString(5, veicolo.getStatoVeicolo().name());
			ps.setInt(6, veicolo.getKm());

			ps.setString(7, veicolo.getTarga());

			int righe = ps.executeUpdate();

			if (righe > 0) {
				System.out.println("Veicolo aggiornato correttamente: " + veicolo.getTarga());
			} else {
				System.out.println("Nessun veicolo trovato da aggiornare: " + veicolo.getTarga());
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Errore durante l'aggiornamento del veicolo: " + veicolo.getTarga());
		}
	}

	@Override
	public void delete(String targa) {

		String sql = "DELETE FROM Veicolo WHERE targa = ?";

		try (Connection conn = DatabaseManager.getInstance().getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, targa);

			int righe = ps.executeUpdate();

			if (righe > 0) {
				System.out.println("Veicolo eliminato correttamente: " + targa);
			} else {
				System.out.println("Nessun veicolo trovato da eliminare con targa: " + targa);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Errore durante l'eliminazione del veicolo: " + targa);
		}
	}

}
