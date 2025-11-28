package it.fleetmanager.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import it.fleetmanager.repository.H2DatabaseManager;

public class DatabaseTestUtils {

	private static final String TEST_URL = "jdbc:h2:mem:fleetdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

	/**
	 * Attiva il DB in memoria per i test.
	 */
	public static void activateInMemoryDatabase() {
		H2DatabaseManager.setTestUrl(TEST_URL);
	}

	/**
	 * Crea lo schema identico a quello reale
	 */
	public static void createSchema() throws SQLException {
		activateInMemoryDatabase();

		try (Connection conn = H2DatabaseManager.getInstance().getConnection(); Statement stmt = conn.createStatement()) {

			// --- DROP in ordine corretto ---
			stmt.execute("DROP TABLE IF EXISTS Notifica;");
			stmt.execute("DROP TABLE IF EXISTS Scadenza;");
			stmt.execute("DROP TABLE IF EXISTS Prenotazione;");
			stmt.execute("DROP TABLE IF EXISTS Manutenzione;");
			stmt.execute("DROP TABLE IF EXISTS Utente;");
			stmt.execute("DROP TABLE IF EXISTS Veicolo;");

			stmt.execute("DROP TYPE IF EXISTS tipo_notifica;");
			stmt.execute("DROP TYPE IF EXISTS tipo_scadenza;");
			stmt.execute("DROP TYPE IF EXISTS stato_prenotazione;");
			stmt.execute("DROP TYPE IF EXISTS tipo_prenotazione;");
			stmt.execute("DROP TYPE IF EXISTS tipo_manutenzione;");
			stmt.execute("DROP TYPE IF EXISTS ruolo_utente;");
			stmt.execute("DROP TYPE IF EXISTS tipo_veicolo;");
			stmt.execute("DROP TYPE IF EXISTS stato_veicolo;");

			// --- SCHEMA CREATION ---
			String sql = """
					    CREATE TYPE IF NOT EXISTS tipo_veicolo AS ENUM('AUTO', 'MOTO', 'FURGONE', 'CAMION');
					    CREATE TYPE IF NOT EXISTS stato_veicolo AS ENUM('DISPONIBILE', 'PRENOTATO', 'IN_MANUTENZIONE', 'NON_DISPONIBILE');

					    CREATE TABLE IF NOT EXISTS Veicolo (
					        targa VARCHAR(10) PRIMARY KEY,
					        tipoVeicolo tipo_veicolo NOT NULL,
					        marca VARCHAR(50) NOT NULL,
					        modello VARCHAR(50) NOT NULL,
					        annoImmatricolazione INT CHECK (annoImmatricolazione > 1900) NOT NULL,
					        statoVeicolo stato_veicolo NOT NULL,
					        km INT CHECK (km >= 0) NOT NULL
					    );

					    CREATE TYPE IF NOT EXISTS ruolo_utente AS ENUM('MANAGER', 'DRIVER');

					    CREATE TABLE IF NOT EXISTS Utente (
					        idUtente INT PRIMARY KEY,
					        nome VARCHAR(50) NOT NULL,
					        cognome VARCHAR(50) NOT NULL,
					        email VARCHAR(100) NOT NULL,
					        password VARCHAR(100) NOT NULL,
					        ruoloUtente ruolo_utente NOT NULL,
					        patente VARCHAR(20)
					    );

					    CREATE TYPE IF NOT EXISTS tipo_manutenzione AS ENUM('ORDINARIA', 'STRAORDINARIA', 'REVISIONE');

					    CREATE TABLE IF NOT EXISTS Manutenzione (
					        idManutenzione INT PRIMARY KEY,
					        data TIMESTAMP NOT NULL,
					        tipoManutenzione tipo_manutenzione NOT NULL,
					        descrizione VARCHAR(255) NOT NULL,
					        targa VARCHAR(10) NOT NULL,
					        FOREIGN KEY (targa) REFERENCES Veicolo(targa) ON DELETE CASCADE
					    );

					    CREATE TYPE IF NOT EXISTS stato_prenotazione AS ENUM('RICHIESTA', 'CONFERMATA', 'ATTIVA', 'ANNULLATA', 'COMPLETATA');
					    CREATE TYPE IF NOT EXISTS tipo_prenotazione AS ENUM('UTENTE','MANUTENZIONE');

					    CREATE TABLE IF NOT EXISTS Prenotazione (
					        idPrenotazione INT PRIMARY KEY,
					        dataInizio TIMESTAMP NOT NULL,
					        dataFine TIMESTAMP NOT NULL,
					        statoPrenotazione stato_prenotazione NOT NULL,
					        tipoPrenotazione tipo_prenotazione NOT NULL,
					        idUtente INT NOT NULL,
					        targa VARCHAR(10) NOT NULL,
					        FOREIGN KEY (idUtente) REFERENCES Utente(idUtente) ON DELETE CASCADE,
					        FOREIGN KEY (targa) REFERENCES Veicolo(targa) ON DELETE CASCADE
					    );

					    CREATE TYPE IF NOT EXISTS tipo_scadenza AS ENUM('BOLLO', 'ASSICURAZIONE', 'REVISIONE', 'TAGLIANDO');

					    CREATE TABLE IF NOT EXISTS Scadenza (
					        idScadenza INT PRIMARY KEY,
					        tipoScadenza tipo_scadenza NOT NULL,
					        data DATE NOT NULL,
					        notificata BOOLEAN NOT NULL DEFAULT FALSE,
					        targa VARCHAR(10) NOT NULL,
					        FOREIGN KEY (targa) REFERENCES Veicolo(targa) ON DELETE CASCADE
					    );

					    CREATE TYPE IF NOT EXISTS tipo_notifica AS ENUM('SCADENZA', 'MANUTENZIONE', 'PRENOTAZIONE', 'SEGNALAZIONE');

					    CREATE TABLE IF NOT EXISTS Notifica(
					        idNotifica INT PRIMARY KEY,
					        tipoNotifica tipo_notifica NOT NULL,
					        messaggio VARCHAR(255) NOT NULL,
					        dataInvio TIMESTAMP NOT NULL,
					        letta BOOLEAN NOT NULL DEFAULT FALSE,
					        idUtente INT NOT NULL,
					        idScadenza INT,
					        FOREIGN KEY (idUtente) REFERENCES Utente(idUtente) ON DELETE CASCADE,
					        FOREIGN KEY (idScadenza) REFERENCES Scadenza(idScadenza) ON DELETE CASCADE
					    );
					""";

			stmt.execute(sql);

			stmt.execute("""
					    INSERT INTO Utente (idUtente, nome, cognome, email, password, ruoloUtente, patente)
					    VALUES
					    (1, 'Mario', 'Rossi', 'manager@example.com', 'pwd', 'MANAGER', NULL),
					    (2, 'Luca', 'Verdi', 'driver@example.com', 'pwd', 'DRIVER', 'XYZ123');
					""");

			stmt.execute("""
					    INSERT INTO Veicolo VALUES
					    ('AB123CD','AUTO','Fiat','Panda',2018,'DISPONIBILE',10000),
					    ('GH819RJ','AUTO','Ford','Focus',2019,'DISPONIBILE',20000),
					    ('ZZ000AA','AUTO','BMW','X1',2018,'DISPONIBILE',30000),
					    ('T1','AUTO','Audi','A1',2021,'DISPONIBILE',15000),
					    ('T2','AUTO','Audi','A3',2022,'DISPONIBILE',5000),
					    ('V1','AUTO','Fiat','500',2020,'DISPONIBILE',8000),
					    ('V2','AUTO','Volkswagen','Golf',2019,'DISPONIBILE',12000),
					    ('V3','AUTO','Tesla','Model 3',2021,'DISPONIBILE',4000);
					""");

			stmt.execute("""
					    INSERT INTO Scadenza (idScadenza, tipoScadenza, data, notificata, targa)
					    VALUES
					    (1, 'BOLLO', '2025-01-10', FALSE, 'AB123CD'),
					    (2, 'ASSICURAZIONE', '2025-03-15', FALSE, 'T1'),
					    (3, 'REVISIONE', '2025-06-20', FALSE, 'V1'),
					    (4, 'TAGLIANDO', '2025-02-05', FALSE, 'T2');
					""");
		}
	}

	public static void resetDatabase() throws SQLException {
		createSchema();
	}
}
