Tutti i cambiamenti rilevanti del progetto FleetManager sono documentati in questo file.

---

## Gennaio 2026

### [0.7.0] - 2026-01-24

#### Added

* Aggiunta JavaDoc estesa a interfacce e classi del Service layer.
* Completamento dei diagrammi UML richiesti: Activity Diagram, Communication Diagram e Component Diagram.
* Introduzione e completamento della gestione utenti nella GUI.

#### Changed

* Refactoring esteso del codice a seguito di analisi con SonarLint.
* Miglioramento e revisione dei Sequence Diagram.
* Revisione del Class Diagram per completo allineamento con l’implementazione.
* Consolidamento dei test di Service e DAO.
* Migliorata coerenza architetturale tra UI, Service e Repository.

#### Fixed

* Correzioni ai controller JavaFX (prenotazioni, notifiche, gestione scadenze).
* Risolte incongruenze negli stati di prenotazioni, veicoli e notifiche.
* Fix minori emersi durante merge e revisioni incrociate del branch GUI.

---

## Dicembre 2025

### [0.6.0] - 2025-12-10

#### Added

* Implementazione completa della GUI JavaFX per veicoli, prenotazioni e manutenzioni.
* Creazione dei controller principali: VeicoliController, VeicoloFormController, PrenotazioniController, NuovaManutenzioneController.
* Implementazione dei metodi di business attivaPrenotazione e completaPrenotazione.
* Generazione State Machine Diagram e Sequence Diagram principali.
* Aggiunta test JUnit per GestoreLogin, GestorePrenotazioni, GestoreScadenze, GestoreManutenzioni e DAO principali.
* Introduzione della classe DatabaseTestUtils per test su database H2 in-memory.

#### Changed

* Riorganizzazione complessiva dei package del progetto.
* Miglioramento dei flussi UI per prenotazioni e manutenzioni.
* Refactoring architetturale per allineamento tra UML e codice.

#### Fixed

* Correzioni nella gestione degli stati dei veicoli.
* Bugfix nei controller GUI.
* Sistemazione test falliti e anomalie nelle DAO.

---

## Novembre 2025

### [0.5.0] - 2025-11-28

#### Added

* Creazione DatabaseManager e interfaccia Database.
* Introduzione ManutenzioneDAO e NotificaDAO con relative implementazioni.
* Implementazione del SistemaNotifiche.
* Aggiunta GestoreManutenzioni con test dedicati.
* Inserimento Seeder e caricamento dati da JSON su H2.

#### Changed

* Refactoring esteso delle classi DAO.
* Miglioramento e ampliamento del Class Diagram.
* Aggiornamento del documento dei requisiti.

---

### [0.4.0] - 2025-11-20

#### Added

* Implementazione GestorePrenotazioni e GestoreLogin.
* Aggiunta test di integrazione per il Service layer.
* Completamento CRUD per Utente, Veicolo e Prenotazione.

#### Fixed

* Bugfix nei metodi CRUD delle DAO.
* Migliorata gestione delle connessioni H2.

---

## Ottobre 2025

### [0.3.0] - 2025-10-25

#### Added

* Creazione delle implementazioni DAO nel package repository.impl.
* Aggiunta DatabaseManager.
* Inserimento Foreign Key nello schema H2.

#### Changed

* Revisione struttura del database.
* Aggiornamenti incrementali al modello UML.

---

### [0.2.0] - 2025-10-21

#### Added

* Creazione del Service layer (interfacce e implementazioni).
* Configurazione iniziale del progetto Maven.

---

### [0.1.0] - 2025-10-14

#### Added

* Creazione repository GitHub.
* Inizializzazione Project Plan.
* Creazione progetto Papyrus UML.
* Definizione dei primi casi d’uso e attori.

---
