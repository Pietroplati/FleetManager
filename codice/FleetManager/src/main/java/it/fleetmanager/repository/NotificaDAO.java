package it.fleetmanager.repository;

import it.fleetmanager.model.Notifica;

public interface NotificaDAO {

	void save(Notifica notifica);

	void update(Notifica notifica);

	void delete(int idNotifica);

	Notifica getNotificaById(int idNotifica);

	Notifica findByUtente(int idUtente);

	Notifica findNonLette(int idUtente);

	Notifica findByScadenza(int idScadenza);

}
