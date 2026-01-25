package it.fleetmanager.util;

/**
 * Rappresenta la tipologia di una prenotazione nel sistema FleetManager.
 * <p>
 * La tipologia distingue le prenotazioni effettuate per l'utilizzo del veicolo
 * da quelle create per esigenze di manutenzione.
 * </p>
 */
public enum TipoPrenotazione {

	/**
	 * Prenotazione effettuata da un utente.
	 * <p>
	 * Indica una prenotazione finalizzata all'utilizzo operativo del veicolo da
	 * parte di un driver.
	 * </p>
	 */
	UTENTE,

	/**
	 * Prenotazione per manutenzione.
	 * <p>
	 * Utilizzata per bloccare la disponibilità di un veicolo durante un intervento
	 * di manutenzione programmata o straordinaria.
	 * </p>
	 */
	MANUTENZIONE
}
