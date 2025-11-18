package it.fleetmanager.repository;

import it.fleetmanager.model.Notifica;
import java.util.List;

public interface NotificaDAO {

	void save(Notifica notifica);

	void update(Notifica notifica);

	void delete(int idNotifica);

	Notifica getNotificaById(int idNotifica);

	List<Notifica> findByUtente(int idUtente);

	List<Notifica> findNonLette(int idUtente);

	List<Notifica> findByScadenza(Integer idScadenza);

}
