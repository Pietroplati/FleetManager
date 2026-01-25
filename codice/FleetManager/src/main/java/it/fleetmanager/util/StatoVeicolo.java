package it.fleetmanager.util;

/**
 * Rappresenta lo stato operativo di un veicolo nel sistema FleetManager.
 * <p>
 * Lo stato del veicolo indica la sua disponibilità e il suo utilizzo corrente
 * all'interno del sistema.
 * </p>
 */
public enum StatoVeicolo {

	/**
	 * Veicolo disponibile per nuove prenotazioni.
	 * <p>
	 * Non è attualmente coinvolto in prenotazioni o manutenzioni.
	 * </p>
	 */
	DISPONIBILE,

	/**
	 * Veicolo associato a una prenotazione attiva o futura.
	 * <p>
	 * Non può essere prenotato nuovamente nello stesso intervallo temporale.
	 * </p>
	 */
	PRENOTATO,

	/**
	 * Veicolo attualmente sottoposto a manutenzione.
	 * <p>
	 * Non è disponibile per la prenotazione fino al termine dell'intervento di
	 * manutenzione.
	 * </p>
	 */
	IN_MANUTENZIONE,

	/**
	 * Veicolo temporaneamente non utilizzabile.
	 * <p>
	 * Indica uno stato di indisponibilità non legato a prenotazioni o manutenzioni
	 * programmate.
	 * </p>
	 */
	NON_DISPONIBILE
}
