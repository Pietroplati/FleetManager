package it.fleetmanager.service.interfaces;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.util.TipoManutenzione;

public interface GestoreManutenzioni {

	Manutenzione programmareManutenzione(Veicolo veicolo, LocalDate data, TipoManutenzione tipo, String descrizione);

	Manutenzione segnalareInterventoStraordinario(Veicolo veicolo, String descrizione);

	void chiudiManutenzione(int idManutenzione);

	List<Manutenzione> getManutenzioniVeicolo(Veicolo veicolo);
}
