package it.fleetmanager.ui.notifiche;

import java.util.List;

import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Utente;
import it.fleetmanager.model.Veicolo;
import it.fleetmanager.repository.dao.NotificaDAO;
import it.fleetmanager.repository.dao.UtenteDAO;
import it.fleetmanager.repository.dao.VeicoloDAO;
import it.fleetmanager.repository.impl.NotificaDAOImpl;
import it.fleetmanager.repository.impl.UtenteDAOImpl;
import it.fleetmanager.repository.impl.VeicoloDAOImpl;
import it.fleetmanager.repository.util.H2DatabaseManager;
import it.fleetmanager.util.TipoNotifica;
import it.fleetmanager.util.RuoloUtente;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SegnalazioneStraordinariaController {

	// =================== UI ===================
	@FXML
	private ComboBox<Veicolo> comboVeicoli;
	@FXML
	private TextArea txtDescrizione;

	// =================== DAO ===================
	private final VeicoloDAO veicoloDAO = new VeicoloDAOImpl(H2DatabaseManager.getInstance());
	private final NotificaDAO notificaDAO = new NotificaDAOImpl(H2DatabaseManager.getInstance());
	private final UtenteDAO utenteDAO = new UtenteDAOImpl(H2DatabaseManager.getInstance());

	// =================== UTENTE ===================
	private Utente driver;

	public void setUtente(Utente utente) {
		this.driver = utente;
		caricaVeicoli();
	}

	// =================== CARICA VEICOLI ===================
	private void caricaVeicoli() {

		List<Veicolo> list = veicoloDAO.getTuttiVeicoli();
		comboVeicoli.getItems().setAll(list);

		comboVeicoli.setCellFactory(lv -> new ListCell<>() {
			@Override
			protected void updateItem(Veicolo v, boolean empty) {
				super.updateItem(v, empty);
				setText(empty || v == null ? "" : v.getTarga() + " - " + v.getMarca() + " " + v.getModello());
			}
		});

		comboVeicoli.setButtonCell(new ListCell<>() {
			@Override
			protected void updateItem(Veicolo v, boolean empty) {
				super.updateItem(v, empty);
				setText(empty || v == null ? "" : v.getTarga() + " - " + v.getMarca() + " " + v.getModello());
			}
		});
	}

	// =================== INVIA SEGNALAZIONE ===================
	@FXML
	private void onInvia() {

		Veicolo selezionato = comboVeicoli.getValue();
		String descrizione = txtDescrizione.getText().trim();

		if (selezionato == null) {
			mostraErrore("Seleziona un veicolo.");
			return;
		}

		if (descrizione.isEmpty()) {
			mostraErrore("Inserisci una descrizione del problema.");
			return;
		}

		// Trova manager
		Utente manager = utenteDAO.getTuttiUtenti().stream().filter(u -> u.getRuoloUtente() == RuoloUtente.MANAGER)
				.findFirst().orElse(null);

		if (manager == null) {
			mostraErrore("Nessun manager presente nel sistema.");
			return;
		}

		// Crea messaggio
		String messaggio = """
				Segnalazione straordinaria da %s %s
				Veicolo: %s (%s %s)
				Problema: %s
				""".formatted(driver.getNome(), driver.getCognome(), selezionato.getTarga(), selezionato.getMarca(),
				selezionato.getModello(), descrizione);

		Notifica n = new Notifica(null, TipoNotifica.SEGNALAZIONE, messaggio, false, manager.getIdUtente(), null);

		notificaDAO.save(n);

		mostraInfo("Segnalazione inviata correttamente.");

		chiudiFinestra();
	}

	// =================== ANNULLA ===================
	@FXML
	private void onAnnulla() {
		chiudiFinestra();
	}

	private void chiudiFinestra() {
		Stage stage = (Stage) txtDescrizione.getScene().getWindow();
		stage.close();
	}

	// =================== ALERTS ===================
	private void mostraErrore(String msg) {
		Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
		alert.showAndWait();
	}

	private void mostraInfo(String msg) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
		alert.showAndWait();
	}
}
