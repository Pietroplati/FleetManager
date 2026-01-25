package it.fleetmanager.util;

/**
 * Rappresenta le diverse tipologie di scadenze associate a un veicolo nel
 * sistema FleetManager.
 * <p>
 * Le scadenze vengono monitorate per garantire la conformità normativa e la
 * corretta manutenzione della flotta, e possono generare notifiche automatiche
 * in prossimità della data di scadenza.
 * </p>
 */
public enum TipoScadenza {

	/**
	 * Scadenza del bollo auto.
	 */
	BOLLO,

	/**
	 * Scadenza dell'assicurazione del veicolo.
	 */
	ASSICURAZIONE,

	/**
	 * Scadenza della revisione periodica obbligatoria.
	 */
	REVISIONE,

	/**
	 * Scadenza del tagliando di manutenzione programmata.
	 */
	TAGLIANDO
}
