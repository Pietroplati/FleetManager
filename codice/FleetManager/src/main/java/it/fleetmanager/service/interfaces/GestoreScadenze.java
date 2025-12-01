package it.fleetmanager.service.interfaces;

import java.time.LocalDate;

import it.fleetmanager.model.Scadenza;
import it.fleetmanager.model.Veicolo;

public interface GestoreScadenze {

	Scadenza controllaScadenzeEntro(LocalDate dataLimite);

	void marcaComeNotificata(Integer idScadenza);

	void bloccaVeicoloSeScaduta(Veicolo veicolo);

	void eseguiControlloPeriodico();

}
