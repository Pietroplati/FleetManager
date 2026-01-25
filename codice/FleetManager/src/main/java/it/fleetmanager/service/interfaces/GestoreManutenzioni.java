package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.util.TipoManutenzione;

/**
 * Interfaccia di servizio per la gestione delle {@link Manutenzione} nel
 * sistema FleetManager.
 * <p>
 * Questa interfaccia definisce il contratto delle operazioni di business
 * relative alle manutenzioni dei veicoli. Le implementazioni concrete si
 * occupano della logica applicativa e delegano la persistenza allo strato
 * repository/DAO.
 * </p>
 */
public interface GestoreManutenzioni {

	/**
	 * Programma una nuova manutenzione ordinaria per un veicolo.
	 *
	 * @param veicolo     veicolo per il quale viene programmata la manutenzione
	 * @param dataInizio  data e ora di inizio della manutenzione
	 * @param tipo        tipo di manutenzione da programmare
	 * @param descrizione descrizione dell'intervento di manutenzione
	 * @return la manutenzione creata e registrata nel sistema
	 */
	Manutenzione programmareManutenzione(Veicolo veicolo, LocalDateTime dataInizio, TipoManutenzione tipo,
			String descrizione);

	/**
	 * Segnala un intervento di manutenzione straordinaria per un veicolo.
	 * <p>
	 * Questo tipo di manutenzione viene utilizzato per gestire guasti o eventi
	 * imprevisti che richiedono un intervento non pianificato.
	 * </p>
	 *
	 * @param veicolo     veicolo per il quale viene segnalato l'intervento
	 * @param descrizione descrizione del problema riscontrato
	 * @return la manutenzione straordinaria creata e registrata nel sistema
	 */
	Manutenzione segnalareInterventoStraordinario(Veicolo veicolo, String descrizione);

	/**
	 * Chiude una manutenzione esistente.
	 * <p>
	 * L'operazione comporta l'aggiornamento dello stato della manutenzione e può
	 * influire sullo stato del veicolo associato.
	 * </p>
	 *
	 * @param idManutenzione identificativo della manutenzione da chiudere
	 */
	void chiudiManutenzione(int idManutenzione);

}
