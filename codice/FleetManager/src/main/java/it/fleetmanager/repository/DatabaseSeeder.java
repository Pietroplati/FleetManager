package it.fleetmanager.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Classe per popolare il database H2 con i dati iniziali presenti nel file
 * JSON. Il database è salvato nel percorso ./data/fleetdb.mv.db e il file JSON
 * è in src/main/resources/data/fleet_data.json
 */
public class DatabaseSeeder {

	// Percorso del database (cartella "data" visibile nel progetto)
	private static final String JDBC_URL = "jdbc:h2:./data/fleetdb";

	// Percorso del file JSON nel classpath
	private static final String JSON_PATH = "/data/fleet_data.json";

	public static void main(String[] args) {
		System.out.println("Avvio del popolamento del database da " + JSON_PATH);
		try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
			seedFromJson(conn);
			System.out.println("Database popolato correttamente!");
		} catch (Exception e) {
			System.err.println("Errore durante il seeding del database: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void seedFromJson(Connection conn) throws Exception {
		conn.setAutoCommit(false);
		try (InputStream is = DatabaseSeeder.class.getResourceAsStream(JSON_PATH)) {
			if (is == null) {
				throw new IllegalStateException("File JSON non trovato nel classpath: " + JSON_PATH);
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(is);

			// UTENTI
			if (root.has("utenti")) {
				try (PreparedStatement ps = conn.prepareStatement(
						"MERGE INTO utente (idutente, nome, cognome, email, password, ruoloutente, patente) "
								+ "KEY(idutente) VALUES (?,?,?,?,?,?,?)")) {
					for (JsonNode u : root.get("utenti")) {
						ps.setInt(1, u.get("idUtente").asInt());
						ps.setString(2, u.get("nome").asText());
						ps.setString(3, u.get("cognome").asText());
						ps.setString(4, u.get("email").asText());
						ps.setString(5, u.hasNonNull("password") ? u.get("password").asText() : null);
						ps.setString(6, u.get("ruoloUtente").asText());
						ps.setString(7, u.path("patente").isNull() ? null : u.get("patente").asText());
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			// VEICOLI
			if (root.has("veicoli")) {
				try (PreparedStatement ps = conn.prepareStatement(
						"MERGE INTO veicolo (targa, tipoveicolo, marca, modello, annoimmatricolazione, statoveicolo, km) "
								+ "KEY(targa) VALUES (?,?,?,?,?,?,?)")) {
					for (JsonNode v : root.get("veicoli")) {
						ps.setString(1, v.get("targa").asText());
						ps.setString(2, v.get("tipoVeicolo").asText());
						ps.setString(3, v.get("marca").asText());
						ps.setString(4, v.get("modello").asText());
						ps.setInt(5, v.get("annoImmatricolazione").asInt());
						ps.setString(6, v.get("statoVeicolo").asText());
						ps.setObject(7, v.path("km").isMissingNode() ? null : v.get("km").asInt());
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			// SCADENZE
			if (root.has("scadenze")) {
				try (PreparedStatement ps = conn
						.prepareStatement("MERGE INTO scadenza (idscadenza, tiposcadenza, data, notificata, targa) "
								+ "KEY(idscadenza) VALUES (?,?,?,?,?)")) {
					for (JsonNode s : root.get("scadenze")) {
						ps.setInt(1, s.get("idScadenza").asInt());
						ps.setString(2, s.get("tipoScadenza").asText());
						ps.setString(3, s.get("data").asText()); // YYYY-MM-DD
						ps.setBoolean(4, s.get("notificata").asBoolean());
						ps.setString(5, s.get("targa").asText());
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			// MANUTENZIONI
			if (root.has("manutenzioni")) {
				try (PreparedStatement ps = conn.prepareStatement(
						"MERGE INTO manutenzione (idmanutenzione, data, tipomanutenzione, descrizione, targa) "
								+ "KEY(idmanutenzione) VALUES (?,?,?,?,?)")) {
					for (JsonNode m : root.get("manutenzioni")) {
						ps.setInt(1, m.get("idManutenzione").asInt());
						ps.setString(2, m.get("data").asText());
						ps.setString(3, m.get("tipoManutenzione").asText());
						ps.setString(4, m.hasNonNull("descrizione") ? m.get("descrizione").asText() : null);
						ps.setString(5, m.get("targa").asText());
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			// PRENOTAZIONI
			if (root.has("prenotazioni")) {
				try (PreparedStatement ps = conn.prepareStatement(
						"MERGE INTO prenotazione (idprenotazione, datainizio, datafine, statoprenotazione, idutente, targa) "
								+ "KEY(idprenotazione) VALUES (?,?,?,?,?,?)")) {
					for (JsonNode p : root.get("prenotazioni")) {
						ps.setInt(1, p.get("idPrenotazione").asInt());
						ps.setString(2, p.get("dataInizio").asText());
						ps.setString(3, p.get("dataFine").asText());
						ps.setString(4, p.get("statoPrenotazione").asText());
						ps.setInt(5, p.get("idUtente").asInt());
						ps.setString(6, p.get("targa").asText());
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			// NOTIFICHE
			if (root.has("notifiche")) {
				try (PreparedStatement ps = conn.prepareStatement(
						"MERGE INTO notifica (idnotifica, tiponotifica, messaggio, datainvio, letta, idscadenza, idutente) "
								+ "KEY(idnotifica) VALUES (?,?,?,?,?,?,?)")) {
					for (JsonNode n : root.get("notifiche")) {
						ps.setInt(1, n.get("idNotifica").asInt());
						ps.setString(2, n.get("tipoNotifica").asText());
						ps.setString(3, n.get("messaggio").asText());
						ps.setString(4, n.get("dataInvio").asText());
						ps.setBoolean(5, n.get("letta").asBoolean());
						ps.setObject(6, n.hasNonNull("idScadenza") ? n.get("idScadenza").asInt() : null);
						ps.setObject(7, n.hasNonNull("idUtente") ? n.get("idUtente").asInt() : null);
						ps.addBatch();
					}
					ps.executeBatch();
				}
			}

			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			conn.setAutoCommit(true);
		}
	}
}
