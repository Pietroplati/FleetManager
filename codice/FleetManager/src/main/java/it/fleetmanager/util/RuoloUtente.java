package it.fleetmanager.util;

/**
 * Rappresenta il ruolo assegnato a un utente del sistema FleetManager.
 * <p>
 * Il ruolo determina i permessi e le funzionalità accessibili all'interno
 * dell'applicazione.
 * </p>
 */
public enum RuoloUtente {

	/**
	 * Utente con ruolo di driver.
	 * <p>
	 * Può effettuare prenotazioni, visualizzare le proprie notifiche
	 * </p>
	 */
	DRIVER,

	/**
	 * Utente con ruolo di manager.
	 * <p>
	 * Ha accesso alle funzionalità di gestione del sistema, inclusa la supervisione
	 * di utenti, veicoli, prenotazioni, manutenzioni e scadenze.
	 * </p>
	 */
	MANAGER
}
