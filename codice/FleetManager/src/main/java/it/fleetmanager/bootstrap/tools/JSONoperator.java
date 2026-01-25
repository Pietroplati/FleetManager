package it.fleetmanager.bootstrap.tools;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings({ "PMD.CommentRequired" })
public class JSONoperator {

    private static final Logger LOGGER = LogManager.getLogger(JSONoperator.class);

    private static final String JSON_ID_UTENTE = "idUtente";

    public static void main(String[] args) throws Exception {
        LOGGER.info("Classe Avviata");
        LOGGER.info("Inserire id dell'utente da eliminare: ");

        try (Scanner scanner = new Scanner(System.in)) {
            int id = scanner.nextInt();
            rimuoviUtente(id);
        }
    }

    public static void rimuoviUtente(int idTarget) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        File file = new File("./src/main/resources/data/fleet_data.json");

        ObjectNode root = (ObjectNode) mapper.readTree(file);

        ArrayNode utenti = (ArrayNode) root.get("utenti");
        ArrayNode prenotazioni = (ArrayNode) root.get("prenotazioni");
        ArrayNode notifiche = (ArrayNode) root.get("notifiche");

        ArrayNode nuoviUtenti = mapper.createArrayNode();
        for (JsonNode u : utenti) {
            if (u.get(JSON_ID_UTENTE).asInt() != idTarget) {
                nuoviUtenti.add(u);
            }
        }

        ArrayNode nuovePrenotazioni = mapper.createArrayNode();
        for (JsonNode p : prenotazioni) {
            if (p.get(JSON_ID_UTENTE).asInt() != idTarget) {
                nuovePrenotazioni.add(p);
            }
        }

        ArrayNode nuoveNotifiche = mapper.createArrayNode();
        for (JsonNode n : notifiche) {
            JsonNode idUt = n.get(JSON_ID_UTENTE);
            if (idUt == null || idUt.isNull() || idUt.asInt() != idTarget) {
                nuoveNotifiche.add(n);
            }
        }

        root.set("utenti", nuoviUtenti);
        root.set("prenotazioni", nuovePrenotazioni);
        root.set("notifiche", nuoveNotifiche);

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

        LOGGER.info("Rimozione utente {} completata.", idTarget);
    }
}
