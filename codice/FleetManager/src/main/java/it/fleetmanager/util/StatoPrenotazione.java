package it.fleetmanager.util;

/**
 * Rappresenta lo stato di una prenotazione nel sistema FleetManager.
 * <p>
 * Lo stato descrive il ciclo di vita di una prenotazione, dalla sua creazione
 * fino alla conclusione o all'annullamento.
 * </p>
 */
public enum StatoPrenotazione {

	/**
	 * Prenotazione appena creata dal driver.
	 * <p>
	 * La richiesta è in attesa di valutazione da parte del manager.
	 * </p>
	 */
	RICHIESTA,

	/**
	 * Prenotazione approvata dal manager.
	 * <p>
	 * La prenotazione è valida ma non ancora iniziata.
	 * </p>
	 */
	CONFERMATA,

	/**
	 * Prenotazione attualmente in corso.
	 * <p>
	 * Il veicolo risulta occupato nell'intervallo temporale definito dalla
	 * prenotazione.
	 * </p>
	 */
	ATTIVA,

	/**
	 * Prenotazione annullata.
	 * <p>
	 * Può essere annullata dal driver o dal manager prima dell'inizio del periodo
	 * di utilizzo.
	 * </p>
	 */
	ANNULLATA,

	/**
	 * Prenotazione terminata correttamente.
	 * <p>
	 * Indica che il periodo di utilizzo del veicolo si è concluso.
	 * </p>
	 */
	COMPLETATA
}
