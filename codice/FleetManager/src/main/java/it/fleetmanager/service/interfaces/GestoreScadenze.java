package it.fleetmanager.service.interfaces;

import java.time.LocalDate;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Veicolo;

/**
 * Interfaccia di servizio per la gestione delle {@link Scadenza} nel sistema
 * FleetManager.
 * <p>
 * Questa interfaccia definisce il contratto delle operazioni di business
 * relative al controllo delle scadenze dei veicoli (assicurazione, bollo,
 * revisione, ecc.). Le implementazioni concrete si occupano della logica
 * applicativa e delegano la persistenza allo strato repository/DAO.
 * </p>
 */
public interface GestoreScadenze {

	/**
	 * Controlla le scadenze entro una determinata data limite.
	 *
	 * @param dataLimite data entro la quale verificare le scadenze
	 * @return una scadenza rilevante entro la data indicata, oppure {@code null}
	 *         se non sono presenti scadenze critiche
	 */
	Scadenza controllaScadenzeEntro(LocalDate dataLimite);

	/**
	 * Marca una scadenza come notificata.
	 * <p>
	 * Questa operazione indica che per la scadenza specificata è già stata
	 * inviata una notifica, evitando notifiche duplicate.
	 * </p>
	 *
	 * @param idScadenza identificativo della scadenza da marcare come notificata
	 */
	void marcaComeNotificata(Integer idScadenza);

	/**
	 * Blocca un veicolo se risulta associato a una scadenza superata.
	 *
	 * @param veicolo veicolo da verificare e, se necessario, bloccare
	 */
	void bloccaVeicoloSeScaduta(Veicolo veicolo);

	/**
	 * Esegue un controllo periodico delle scadenze del sistema.
	 * <p>
	 * Questo metodo coordina le operazioni di verifica delle scadenze,
	 * invio notifiche e blocco dei veicoli quando necessario.
	 * </p>
	 */
	void eseguiControlloPeriodico();

}
