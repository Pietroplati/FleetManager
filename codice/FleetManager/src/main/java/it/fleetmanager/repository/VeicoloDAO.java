package it.fleetmanager.repository;

import java.time.LocalDateTime;
import java.util.List;

import it.fleetmanager.model.Veicolo;

public interface VeicoloDAO {

	void save(Veicolo veicolo);

	void update(Veicolo veicolo);

	void delete(String targa);

	Veicolo getVeicoloByTarga(String targa);

	List<Veicolo> getTuttiVeicoli();

	List<Veicolo> getDisponibili(LocalDateTime dataInizio, LocalDateTime dataFine);

}
