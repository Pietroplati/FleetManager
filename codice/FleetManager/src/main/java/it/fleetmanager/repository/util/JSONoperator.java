package it.fleetmanager.repository.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONoperator {

	public static void main(String[] args) throws Exception {
		System.out.println("Classe Avviata");
		System.out.println("Inserire id dell'utente da eliminare: ");

		try (Scanner scanner = new Scanner(System.in)) {
			int id = scanner.nextInt();

			rimuoviUtente(id);
		}

	}

	public static void rimuoviUtente(int idTarget) throws StreamWriteException, DatabindException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();

		File file = new File("./src/main/resources/data/fleet_data.json");

		ObjectNode root = (ObjectNode) mapper.readTree(file);

		ArrayNode utenti = (ArrayNode) root.get("utenti");
		ArrayNode prenotazioni = (ArrayNode) root.get("prenotazioni");
		ArrayNode notifiche = (ArrayNode) root.get("notifiche");

		// UTENTI
		ArrayNode nuoviUtenti = mapper.createArrayNode();
		for (JsonNode u : utenti) {
			if (u.get("idUtente").asInt() != idTarget) {
				nuoviUtenti.add(u);
			}
		}

		// PRENOTAZIONI
		ArrayNode nuovePrenotazioni = mapper.createArrayNode();
		for (JsonNode p : prenotazioni) {
			if (p.get("idUtente").asInt() != idTarget) {
				nuovePrenotazioni.add(p);
			}
		}

		// NOTIFICHE
		ArrayNode nuoveNotifiche = mapper.createArrayNode();
		for (JsonNode n : notifiche) {
			JsonNode idUt = n.get("idUtente");
			if (idUt == null || idUt.isNull() || idUt.asInt() != idTarget) {
				nuoveNotifiche.add(n);
			}
		}

		root.set("utenti", nuoviUtenti);
		root.set("prenotazioni", nuovePrenotazioni);
		root.set("notifiche", nuoveNotifiche);

		// Scrivi il file
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

		System.out.println("Rimozione utente " + idTarget + " completata.");
	}

}
