package it.fleetmanager.repository;

import java.time.LocalDate;
import java.util.List;

import it.fleetmanager.model.Veicolo;
import it.fleetmanager.util.StatoVeicolo;

public interface VeicoloDAO {

    Veicolo getVeicoloByTarga(String targa);

    List<Veicolo> getTuttiVeicoli();

    List<Veicolo> getDisponibili(LocalDate dataInizio, LocalDate dataFine);

    void save(Veicolo veicolo);

    void update(Veicolo veicolo);

    void delete(String targa);
}
