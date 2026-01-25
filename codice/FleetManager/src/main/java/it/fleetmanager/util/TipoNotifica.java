package it.fleetmanager.util;

/**
 * Rappresenta la tipologia di una notifica generata dal sistema FleetManager.
 * <p>
 * Il tipo di notifica identifica l'evento che ha causato la creazione della
 * notifica e ne determina il contesto funzionale.
 * </p>
 */
public enum TipoNotifica {

	/**
	 * Notifica relativa a una scadenza.
	 * <p>
	 * Utilizzata per segnalare scadenze imminenti o superate, come revisioni o
	 * altri adempimenti periodici.
	 * </p>
	 */
	SCADENZA,

	/**
	 * Notifica relativa a una manutenzione.
	 * <p>
	 * Indica l'inizio, la pianificazione o il completamento di un intervento di
	 * manutenzione su un veicolo.
	 * </p>
	 */
	MANUTENZIONE,

	/**
	 * Notifica relativa a una prenotazione.
	 * <p>
	 * Utilizzata per comunicare eventi come creazione, approvazione, annullamento o
	 * conclusione di una prenotazione.
	 * </p>
	 */
	PRENOTAZIONE,

	/**
	 * Notifica di segnalazione.
	 * <p>
	 * Indica una segnalazione generica, ad esempio un problema o un evento
	 * rilevante segnalato da un utente.
	 * </p>
	 */
	SEGNALAZIONE
}
