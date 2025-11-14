package it.fleetmanager.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTable {

	public static void main(String[] args) {
		try (Connection conn = DatabaseManager.getInstance().getConnection(); Statement stmt = conn.createStatement()) {

			String sql = """
					CREATE TYPE IF NOT EXISTS tipo_veicolo AS ENUM('AUTO', 'MOTO' , 'FURGONE', 'CAMION');
					CREATE TYPE IF NOT EXISTS stato_veicolo AS ENUM('DISPONIBILE', 'PRENOTATO', 'IN_MANUTENZIONE', 'NON_DISPONIBILE');

					CREATE TABLE IF NOT EXISTS Veicolo (
					    targa VARCHAR(10) PRIMARY KEY,
					    tipoVeicolo tipo_veicolo NOT NULL,
					    marca VARCHAR(50) NOT NULL,
					    modello VARCHAR(50) NOT NULL,
					    annoImmatricolazione INT CHECK (annoImmatricolazione > 1900),
					    statoVeicolo stato_veicolo NOT NULL,
					    km INT CHECK (km >= 0)
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

					CREATE TYPE IF NOT EXISTS tipo_manutenzione AS ENUM('ORDINARIA', 'STRAORDINARIA' , 'REVISIONE');

					CREATE TABLE IF NOT EXISTS Manutenzione (
					    idManutenzione INT PRIMARY KEY,
					    data TIMESTAMP NOT NULL,
					    tipoManutenzione tipo_manutenzione NOT NULL,
					    descrizione VARCHAR(255) NOT NULL,
					    targa VARCHAR(10) NOT NULL,
					    FOREIGN KEY (targa) REFERENCES Veicolo(targa) ON DELETE CASCADE
					);

					CREATE TYPE IF NOT EXISTS stato_prenotazione AS ENUM('RICHIESTA', 'CONFERMATA', 'ATTIVA', 'ANNULLATA', 'COMPLETATA');

					CREATE TABLE IF NOT EXISTS Prenotazione (
					    idPrenotazione INT PRIMARY KEY,
					    dataInizio TIMESTAMP NOT NULL,
					    dataFine TIMESTAMP NOT NULL,
					    statoPrenotazione stato_prenotazione NOT NULL,
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
					    targa VARCHAR(10),
					    FOREIGN KEY (idUtente) REFERENCES Utente(idUtente) ON DELETE CASCADE,
					    FOREIGN KEY (targa) REFERENCES Veicolo(targa) ON DELETE CASCADE
					);
					""";

			stmt.executeUpdate(sql);
			System.out.println("Tabelle create correttamente!");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
