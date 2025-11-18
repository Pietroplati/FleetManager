package it.fleetmanager.repository;

import java.util.List;
import it.fleetmanager.model.Manutenzione;
import it.fleetmanager.util.TipoManutenzione;

public interface ManutenzioneDAO {

	void save(Manutenzione manutenzione);

	void update(Manutenzione manutenzione);

	void delete(int idManutenzione);

	Manutenzione getManutenzioneById(int idManutenzione);

	List<Manutenzione> findByVeicolo(String targa);

	List<Manutenzione> findByTipo(TipoManutenzione tipoManutenzione);
}
