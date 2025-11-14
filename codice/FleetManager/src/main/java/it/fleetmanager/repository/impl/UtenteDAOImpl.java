package it.fleetmanager.repository.impl;

import java.io.File;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean existsByEmail(String email) {
		// TODO Auto-generated method stub
		return false;
	}

}
