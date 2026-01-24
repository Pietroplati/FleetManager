package it.fleetmanager.service.interfaces;

import java.time.LocalDateTime;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.util.TipoManutenzione;

public interface GestoreManutenzioni {

	Manutenzione programmareManutenzione(
			Veicolo veicolo,
			LocalDateTime dataInizio,
			TipoManutenzione tipo,
			String descrizione
	);

	Manutenzione segnalareInterventoStraordinario(
			Veicolo veicolo,
			String descrizione
	);

	void chiudiManutenzione(int idManutenzione);
}
