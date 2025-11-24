package it.fleetmanager.repository;

import java.time.LocalDate;
import java.util.List;
import it.fleetmanager.model.Scadenza;


public interface ScadenzaDAO {

	void save(Scadenza scadenza);

	void update(Scadenza scadenza);

	void delete(int idScadenza);

	Scadenza getScadenzaById(int idScadenza);

	List<Scadenza> findProssimeScadenze(LocalDate finoA);

	List<Scadenza> findByVeicolo(String targa);
}
