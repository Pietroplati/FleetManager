package it.fleetmanager.bootstrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Classe per popolare il database H2 con i dati iniziali presenti nel file JSON.
 * Il file JSON è in src/main/resources/data/fleet_data.json
 *
 * Nota: questa classe NON apre connessioni e NON ha main.
 * Si usa passando una Connection già aperta.
 */
public class DatabaseSeeder {

    // Percorso del file JSON nel classpath
    private static final String JSON_PATH = "/data/fleet_data.json";


    private static final String JSON_ID_UTENTE = "idUtente";
    private static final String JSON_TARGA = "targa";
    private static final String JSON_ID_SCADENZA = "idScadenza";

    public void seedFromJson(Connection conn) throws SQLException, IOException {
        conn.setAutoCommit(false);

        try (InputStream is = DatabaseSeeder.class.getResourceAsStream(JSON_PATH)) {
            if (is == null) {
                throw new IllegalStateException("File JSON non trovato nel classpath: " + JSON_PATH);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            seedUtenti(conn, root);
            seedVeicoli(conn, root);
            seedScadenze(conn, root);
            seedManutenzioni(conn, root);
            seedPrenotazioni(conn, root);
            seedNotifiche(conn, root);

            conn.commit();
        } catch (IOException | SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void seedUtenti(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("utenti")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO utente (idutente, nome, cognome, email, password, ruoloutente, patente) " +
                        "KEY(idutente) VALUES (?,?,?,?,?,?,?)")) {

            for (JsonNode u : root.get("utenti")) {
                ps.setInt(1, u.get(JSON_ID_UTENTE).asInt());
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

    private void seedVeicoli(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("veicoli")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO veicolo (targa, tipoveicolo, marca, modello, annoimmatricolazione, statoveicolo, km) " +
                        "KEY(targa) VALUES (?,?,?,?,?,?,?)")) {

            for (JsonNode v : root.get("veicoli")) {
                ps.setString(1, v.get(JSON_TARGA).asText());
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

    private void seedScadenze(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("scadenze")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO scadenza (idscadenza, tiposcadenza, data, notificata, targa) " +
                        "KEY(idscadenza) VALUES (?,?,?,?,?)")) {

            for (JsonNode s : root.get("scadenze")) {
                ps.setInt(1, s.get(JSON_ID_SCADENZA).asInt());
                ps.setString(2, s.get("tipoScadenza").asText());

         
                LocalDate data = LocalDate.parse(s.get("data").asText());
                ps.setObject(3, data);

                ps.setBoolean(4, s.get("notificata").asBoolean());
                ps.setString(5, s.get(JSON_TARGA).asText());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedManutenzioni(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("manutenzioni")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO manutenzione (idmanutenzione, data, tipomanutenzione, descrizione, targa) " +
                        "KEY(idmanutenzione) VALUES (?,?,?,?,?)")) {

            for (JsonNode m : root.get("manutenzioni")) {
                ps.setInt(1, m.get("idManutenzione").asInt());

             
                LocalDate data = LocalDate.parse(m.get("data").asText());
                ps.setObject(2, data);

                ps.setString(3, m.get("tipoManutenzione").asText());
                ps.setString(4, m.hasNonNull("descrizione") ? m.get("descrizione").asText() : null);
                ps.setString(5, m.get(JSON_TARGA).asText());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedPrenotazioni(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("prenotazioni")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO prenotazione (idprenotazione, datainizio, datafine, statoprenotazione, tipoprenotazione, idutente, targa) " +
                        "KEY(idprenotazione) VALUES (?,?,?,?,?,?,?)")) {

            for (JsonNode p : root.get("prenotazioni")) {
                ps.setInt(1, p.get("idPrenotazione").asInt());

            
                LocalDateTime inizio = LocalDateTime.parse(p.get("dataInizio").asText());
                LocalDateTime fine = LocalDateTime.parse(p.get("dataFine").asText());
                ps.setObject(2, inizio);
                ps.setObject(3, fine);

                ps.setString(4, p.get("statoPrenotazione").asText());
                ps.setString(5, p.get("tipoPrenotazione").asText());
                ps.setInt(6, p.get(JSON_ID_UTENTE).asInt());
                ps.setString(7, p.get(JSON_TARGA).asText());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void seedNotifiche(Connection conn, JsonNode root) throws SQLException {
        if (!root.has("notifiche")) return;

        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO notifica (idnotifica, tiponotifica, messaggio, datainvio, letta, idscadenza, idutente) " +
                        "KEY(idnotifica) VALUES (?,?,?,?,?,?,?)")) {

            for (JsonNode n : root.get("notifiche")) {
                ps.setInt(1, n.get("idNotifica").asInt());
                ps.setString(2, n.get("tipoNotifica").asText());
                ps.setString(3, n.get("messaggio").asText());

               
                LocalDateTime invio = LocalDateTime.parse(n.get("dataInvio").asText());
                ps.setObject(4, invio);

                ps.setBoolean(5, n.get("letta").asBoolean());
                ps.setObject(6, n.hasNonNull(JSON_ID_SCADENZA) ? n.get(JSON_ID_SCADENZA).asInt() : null);
                ps.setObject(7, n.hasNonNull(JSON_ID_UTENTE) ? n.get(JSON_ID_UTENTE).asInt() : null);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
