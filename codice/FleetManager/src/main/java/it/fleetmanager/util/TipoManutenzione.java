package it.fleetmanager.util;

/**
 * Rappresenta la tipologia di manutenzione associata a un veicolo.
 * <p>
 * La tipologia consente di classificare gli interventi di manutenzione in base
 * alla loro natura e finalità.
 * </p>
 */
public enum TipoManutenzione {

	/**
	 * Manutenzione ordinaria.
	 * <p>
	 * Include gli interventi periodici programmati, come controlli di routine e
	 * operazioni di normale gestione del veicolo.
	 * </p>
	 */
	ORDINARIA,

	/**
	 * Manutenzione straordinaria.
	 * <p>
	 * Riguarda interventi non programmati dovuti a guasti o malfunzionamenti
	 * imprevisti.
	 * </p>
	 */
	STRAORDINARIA,

	/**
	 * Revisione del veicolo.
	 * <p>
	 * Indica gli interventi di revisione obbligatoria previsti dalla normativa
	 * vigente.
	 * </p>
	 */
	REVISIONE
}
