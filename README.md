# FleetManager

FleetManager è un progetto accademico sviluppato per il corso di **Ingegneria del Software (A.A. 2025/26)** presso l’Università degli Studi di Bergamo.

L’obiettivo del progetto è applicare in modo pratico le metodologie e le tecniche dell’ingegneria del software alla realizzazione di un sistema completo per la gestione di flotte aziendali, coprendo l’intero ciclo di vita del software: analisi dei requisiti, progettazione UML, implementazione, testing e documentazione.

---

## Obiettivi del sistema

FleetManager consente di:

* gestire l’anagrafica dei veicoli aziendali;
* permettere ai driver di prenotare e utilizzare i veicoli;
* consentire al manager di approvare e monitorare le prenotazioni;
* gestire le scadenze (bollo, assicurazione, revisione);
* pianificare e tracciare le manutenzioni;
* generare notifiche automatiche sugli eventi rilevanti.
* * consentire al manager di aggiungere, modificare o eliminare driver.

---

## Architettura

Il sistema adotta un’architettura a livelli, coerente con i principi di progettazione trattati nel corso:

* **Model**: classi di dominio (Utente, Veicolo, Prenotazione, Scadenza, Manutenzione, Notifica);
* **Repository (DAO)**: accesso ai dati e persistenza su database H2 embedded;
* **Service**: logica applicativa e regole di business;
* **UI**: interfaccia grafica JavaFX 21 (dashboard Manager e Driver);
* **Test**: test di unità e integrazione realizzati con JUnit 5.

L’architettura è documentata tramite diagrammi UML (Use Case, Class, State Machine, Sequence, Activity, Communication, Component e Package Diagram).

---

## Tecnologie utilizzate

* Java 17
* Eclipse IDE
* Maven
* JavaFX 21
* H2 Database (embedded)
* JUnit 5
* Papyrus UML
* GitHub
* SonarLint, PMD, Stan4J

---

## Avvio del progetto

Il progetto utilizza Maven e il plugin JavaFX.

### Requisiti

* JDK 17 installato
* Maven configurato

### Avvio dell’applicazione

Dalla root del progetto eseguire:

```bash
mvn javafx:run
```

L’applicazione JavaFX viene avviata utilizzando un database H2 embedded già configurato.

---

## Esecuzione dei test

I test sono realizzati utilizzando **JUnit 5**.

Durante lo sviluppo e la verifica del progetto, i test vengono eseguiti principalmente direttamente dall’IDE **Eclipse**, utilizzando l’opzione:

* **Run As → JUnit Test**

L’esecuzione mostra l’esito dei test tramite l’interfaccia grafica di Eclipse.

In alternativa, è possibile eseguire l’intera suite di test anche tramite Maven:

```bash
mvn test
```

I test utilizzano un database **H2 in-memory** e non richiedono configurazioni esterne.

---

## Struttura del repository

```
/codice        -> sorgenti Java e progetto Maven
/documenti    -> documentazione PDF (Project Plan, Requisiti, Design, Testing, Maintenance)
/uml           -> progetto Papyrus e diagrammi UML
README.md
CHANGELOG.md
```

---

## Documentazione

La documentazione del progetto include:

* Project Plan
* Gestione del progetto
* Specifica dei requisiti
* Design e architettura
* Testing
* Maintenance

Tutta la documentazione è disponibile nella cartella `/documenti`.

---

## Versionamento

Il progetto è stato sviluppato in modo incrementale utilizzando GitHub per il controllo di versione, con l’uso di branch e merge.

L’evoluzione del progetto è tracciata nel file `CHANGELOG.md`, organizzato per mesi e milestone.

---

## Autori

* Pietro Plati – 1091949
* Francesco Basis – 1092076

---

## Contesto accademico

Progetto realizzato per il corso di Ingegneria del Software.

Università degli Studi di Bergamo.

Prof. Angelo Gargantini.

Dott.ssa Silvia Bonfanti.
