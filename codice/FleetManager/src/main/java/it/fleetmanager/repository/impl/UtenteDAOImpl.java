package it.fleetmanager.repository.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.fleetmanager.model.Utente;
import it.fleetmanager.repository.DatabaseManager;

import it.fleetmanager.repository.UtenteDAO;

public class UtenteDAOImpl implements UtenteDAO {

	@Override
	public Optional<Utente> getUtenteByEmail(String email) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();

			File file = new File("./src/main/resources/data/fleet_data.json");

			// 1) Leggo il JSON come albero
			ObjectNode root = (ObjectNode) mapper.readTree(file);

			// 2) Ottengo il nodo "utenti"
			ArrayNode utentiNode = (ArrayNode) root.get("utenti");

			if (utentiNode == null) {
				System.err.println("ERRORE: nel JSON manca il nodo 'utenti'");
				return Optional.empty();
			}

			// 3) Scorro tutti gli utenti e cerco l'email
			for (JsonNode u : utentiNode) {
				if (u.get("email").asText().equalsIgnoreCase(email)) {

					// 4) Converto il JsonNode → Utente
					Utente trovato = mapper.treeToValue(u, Utente.class);

					return Optional.of(trovato);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 5) Se non trovato
		return Optional.empty();
	}

	@Override
	public Optional<Utente> getById(Integer id) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public void save(Utente utente) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();

			File file = new File("./src/main/resources/data/fleet_data.json");

			// 1) Carico tutto il JSON come albero
			ObjectNode root = (ObjectNode) mapper.readTree(file);

			// 2) Ottengo il nodo utenti (è un ArrayNode)
			ArrayNode utentiNode = (ArrayNode) root.get("utenti");

			if (utentiNode == null) {
				System.err.println("ERRORE: nel JSON manca il nodo 'utenti'");
				return;
			}

			// 3) Controllo se esiste già un utente con quell'ID
			boolean idEsistente = false;

			for (JsonNode u : utentiNode) {
				if (u.get("idUtente").asInt() == utente.getIdUtente()) {
					idEsistente = true;
					break;
				}
			}

			if (idEsistente) {
				System.err.println("ERRORE: l'ID " + utente.getIdUtente() + " è già presente!");
				return;
			}

			// 4) Converto Utente Java -> JsonNode
			JsonNode nuovoUtenteNode = mapper.convertValue(utente, JsonNode.class);

			// 5) Aggiungo al JSON
			utentiNode.add(nuovoUtenteNode);

			// 6) Riscrivo il file
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

			System.out.println("✔ Utente aggiunto correttamente al JSON!");

			// ======================================
			// 7) Aggiunta anche nel DB H2 (via JDBC)
			// ======================================
			try (Connection conn = DatabaseManager.getInstance().getConnection()) {

				String sql = "INSERT INTO Utente " + "(idUtente, nome, cognome, email, password, ruoloUtente, patente) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

				try (PreparedStatement ps = conn.prepareStatement(sql)) {

					ps.setInt(1, utente.getIdUtente());
					ps.setString(2, utente.getNome());
					ps.setString(3, utente.getCognome());
					ps.setString(4, utente.getEmail());
					ps.setString(5, utente.getPassword());
					ps.setString(6, utente.getRuoloUtente().name());
					ps.setString(7, utente.getPatente());

					ps.executeUpdate();

					System.out.println("Utente inserito correttamente anche nel database H2!");
				}

			} catch (SQLException sqlEx) {
				System.err.println("ERRORE durante l'INSERT SQL: " + sqlEx.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Utente utente) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();

			File file = new File("./src/main/resources/data/fleet_data.json");

			// 1) Carico il JSON come albero
			ObjectNode root = (ObjectNode) mapper.readTree(file);

			// 2) Ottengo il nodo utenti
			ArrayNode utentiNode = (ArrayNode) root.get("utenti");

			if (utentiNode == null) {
				System.err.println("ERRORE: nel JSON manca il nodo 'utenti'");
				return;
			}

			boolean aggiornato = false;

			// 3) Scorro tutti gli utenti e cerco quello da aggiornare
			for (int i = 0; i < utentiNode.size(); i++) {

				JsonNode u = utentiNode.get(i);

				if (u.get("idUtente").asInt() == utente.getIdUtente()) {

					// 4) converto il nuovo utente in JsonNode
					JsonNode nuovoNodo = mapper.convertValue(utente, JsonNode.class);

					// 5) sostituisco il nodo vecchio con quello nuovo
					utentiNode.set(i, nuovoNodo);

					aggiornato = true;
					break;
				}
			}

			if (!aggiornato) {
				System.err.println(
						"ERRORE: impossibile aggiornare, utente con ID " + utente.getIdUtente() + " non trovato.");
				return;
			}

			// 6) Riscrivo il file JSON aggiornato
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

			System.out.println("✔ Utente aggiornato correttamente nel JSON!");

			// ======================================
			// 7) Aggiornamento anche nel DB H2
			// ======================================

			try (Connection conn = DatabaseManager.getInstance().getConnection()) {

				String sql = "UPDATE Utente SET " + "nome=?, cognome=?, email=?, password=?, ruoloUtente=?, patente=? "
						+ "WHERE idUtente=?";

				try (PreparedStatement ps = conn.prepareStatement(sql)) {

					ps.setString(1, utente.getNome());
					ps.setString(2, utente.getCognome());
					ps.setString(3, utente.getEmail());
					ps.setString(4, utente.getPassword());
					ps.setString(5, utente.getRuoloUtente().name());
					ps.setString(6, utente.getPatente());
					ps.setInt(7, utente.getIdUtente());

					int rows = ps.executeUpdate();

					if (rows > 0) {
						System.out.println("✔ Utente aggiornato correttamente anche nel database H2!");
					} else {
						System.err.println("ERRORE DB: utente non trovato nel database H2.");
					}
				}

			} catch (SQLException sqlEx) {
				System.err.println("ERRORE durante l'UPDATE SQL: " + sqlEx.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Integer id) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.findAndRegisterModules();

			File file = new File("./src/main/resources/data/fleet_data.json");

			// 1) Leggo il JSON
			ObjectNode root = (ObjectNode) mapper.readTree(file);
			ArrayNode utentiNode = (ArrayNode) root.get("utenti");

			if (utentiNode == null) {
				System.err.println("ERRORE: nel JSON manca il nodo 'utenti'");
				return;
			}

			boolean rimosso = false;

			// 2) Cerco e rimuovo l'utente per ID
			for (int i = 0; i < utentiNode.size(); i++) {
				JsonNode u = utentiNode.get(i);

				if (u.get("idUtente").asInt() == id) {
					utentiNode.remove(i);
					rimosso = true;
					break;
				}
			}

			if (!rimosso) {
				System.err.println("ERRORE: utente con ID " + id + " non trovato. Nessuna rimozione eseguita.");
				return;
			}

			// 3) Riscrivo il file aggiornato
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
			System.out.println("✔ Utente rimosso correttamente dal JSON!");

			// ==========================================
			// 4) Eliminazione anche nel DB H2
			// ==========================================
			try (Connection conn = DatabaseManager.getInstance().getConnection()) {

				String sql = "DELETE FROM Utente WHERE idUtente = ?";

				try (PreparedStatement ps = conn.prepareStatement(sql)) {
					ps.setInt(1, id);
					int rows = ps.executeUpdate();

					if (rows > 0) {
						System.out.println("✔ Utente eliminato correttamente anche dal database H2!");
					} else {
						System.err.println("ERRORE DB: utente non presente nel database H2.");
					}
				}

			} catch (SQLException sqlEx) {
				System.err.println("ERRORE SQL durante il DELETE: " + sqlEx.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean existsByEmail(String email) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.findAndRegisterModules();

	        File file = new File("./src/main/resources/data/fleet_data.json");

	        // 1) Leggo il JSON
	        ObjectNode root = (ObjectNode) mapper.readTree(file);

	        ArrayNode utentiNode = (ArrayNode) root.get("utenti");
	        if (utentiNode == null) {
	            System.err.println("ERRORE: manca il nodo 'utenti'");
	            return false;
	        }

	        // 2) Scorro tutti gli utenti correttamente
	        for (JsonNode u : utentiNode) {

	            if (u.get("email").asText().equalsIgnoreCase(email)) {
	                return true;
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // 3) Default: non trovato
	    return false;
	}


}
