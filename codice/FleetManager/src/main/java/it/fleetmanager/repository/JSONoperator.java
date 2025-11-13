package it.fleetmanager.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

import it.fleetmanager.model.Veicolo;

public class JSONoperator {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/resources/data/fleet_data.json"); 

        // Leggi tutto l'oggetto JSON radice
        JsonNode root = mapper.readTree(file);

        // Prendi il nodo "veicoli"
        JsonNode veicoliNode = root.get("veicoli");

        // Converti il nodo in una lista di oggetti Veicolo
        List<Veicolo> veicoli = mapper.convertValue(veicoliNode, new TypeReference<>() {});

        // Rimuovi il veicolo con targa GH819RJ
        boolean removed = veicoli.removeIf(v -> v.getTarga().equals("GH819RJ"));

        if (removed) {
            System.out.println("✅ Veicolo GH819RJ rimosso dalla lista.");
        } else {
            System.out.println("ℹ️ Nessun veicolo GH819RJ trovato.");
        }

        //️Aggiorna il nodo "veicoli" dentro al root
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("veicoli", mapper.valueToTree(veicoli));

        //️Sovrascrivi il file con il JSON aggiornato (mantenendo anche "scadenze")
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

        System.out.println("💾 File JSON aggiornato correttamente.");
    }
}
