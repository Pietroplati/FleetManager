package it.fleetmanager.service.interfaces;

import java.util.List;

import it.fleetmanager.model.Utente;

/**
 * Interfaccia di servizio per la gestione delle funzionalità di autenticazione
 * e di gestione degli {@link Utente} nel sistema FleetManager.
 * <p>
 * Questa interfaccia definisce il contratto delle operazioni di business legate
 * agli utenti (login e gestione profilo). Le implementazioni concrete si
 * occupano della logica applicativa e delegano la persistenza allo strato
 * repository/DAO.
 * </p>
 */
public interface GestoreLogin {

	/**
	 * Esegue l'autenticazione di un utente a partire da email e password.
	 *
	 * @param email    email dell'utente
	 * @param password password dell'utente
	 * @return l'utente autenticato se le credenziali sono valide; {@code null} in
	 *         caso di credenziali non valide o utente inesistente
	 */
	Utente login(String email, String password);

	/**
	 * Crea un nuovo utente nel sistema.
	 *
	 * @param nuovoUtente utente da creare
	 * @return {@code true} se la creazione è andata a buon fine; {@code false}
	 *         altrimenti (ad esempio email già presente o dati non validi)
	 */
	boolean createUtente(Utente nuovoUtente);

	/**
	 * Aggiorna i dati del profilo di un utente esistente.
	 *
	 * @param utenteAggiornato utente con i dati aggiornati
	 * @return {@code true} se l'aggiornamento è andato a buon fine; {@code false}
	 *         altrimenti
	 */
	boolean aggiornaProfilo(Utente utenteAggiornato);

	/**
	 * Elimina un utente identificato dal suo id.
	 *
	 * @param idUtente identificativo dell'utente
	 * @return {@code true} se l'eliminazione è andata a buon fine; {@code false} se
	 *         l'utente non esiste o l'operazione non è riuscita
	 */
	boolean eliminaUtente(int idUtente);

	/**
	 * Restituisce un utente a partire dalla sua email.
	 *
	 * @param email email dell'utente
	 * @return l'utente trovato; {@code null} se non esiste
	 */
	Utente getUtenteByEmail(String email);

	/**
	 * Restituisce tutti gli utenti presenti nel sistema.
	 *
	 * @return lista completa degli utenti (eventualmente vuota, mai {@code null})
	 */
	List<Utente> getTuttiUtenti();

}
