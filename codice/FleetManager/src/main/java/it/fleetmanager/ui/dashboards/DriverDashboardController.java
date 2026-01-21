package it.fleetmanager.ui.dashboards;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import it.fleetmanager.app.AppContext;
import it.fleetmanager.model.Notifica;
import it.fleetmanager.model.Prenotazione;
import it.fleetmanager.model.Utente;
import it.fleetmanager.service.interfaces.UiFacade;
import it.fleetmanager.ui.SceneManager;
import it.fleetmanager.ui.UserAwareController;
import it.fleetmanager.ui.notifiche.SegnalazioneStraordinariaController;
import it.fleetmanager.util.StatoPrenotazione;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DriverDashboardController implements UserAwareController {

	// ===== UI =====
	@FXML private Label lblNome;
	@FXML private Label lblEmail;
	@FXML private Label lblPatente;

	@FXML private Label lblPrenotazioniAttive;
	@FXML private Label lblProssimaPrenotazione;
	@FXML private Label lblVeicoloAssegnato;

	@FXML private Button btnNotifiche;
	@FXML private Circle badgeNotifiche;

	//SOLO facade (niente DAO in UI)
	private final UiFacade ui = AppContext.getInstance().getUiFacade();

	// ===== UTENTE =====
	private Utente utente;

	@Override
	public void setUtente(Utente utente) {
		this.utente = utente;

		aggiornaDatiUtente();
		aggiornaWidgets();
		aggiornaBadgeNotifiche();
	}

	private void aggiornaDatiUtente() {
		lblNome.setText(utente.getNome() + " " + utente.getCognome());
		lblEmail.setText(utente.getEmail());
		lblPatente.setText("Patente: " + utente.getPatente());
	}

	private void aggiornaWidgets() {
		aggiornaPrenotazioniAttive();
		aggiornaProssimaPrenotazione();
		aggiornaVeicoloAssegnato();
	}

	private void aggiornaPrenotazioniAttive() {
		long count = ui.getPrenotazioniDriver(utente.getIdUtente()).stream()
				.filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.count();

		lblPrenotazioniAttive.setText(String.valueOf(count));
	}

	private void aggiornaProssimaPrenotazione() {

		List<Prenotazione> prenList = ui.getPrenotazioniDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM HH:mm", new Locale("it", "IT"));

		var attiva = prenList.stream()
				.filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> p.getDataFine().isAfter(now))
				.min(Comparator.comparing(Prenotazione::getDataFine));

		if (attiva.isPresent()) {
			lblProssimaPrenotazione.setText(attiva.get().getDataFine().format(fmt));
			return;
		}

		var confermata = prenList.stream()
				.filter(p -> p.getStato() == StatoPrenotazione.CONFERMATA)
				.filter(p -> p.getDataInizio().isAfter(now))
				.min(Comparator.comparing(Prenotazione::getDataInizio));

		if (confermata.isPresent()) {
			lblProssimaPrenotazione.setText(confermata.get().getDataInizio().format(fmt));
			return;
		}

		lblProssimaPrenotazione.setText("Nessuna");
	}

	private void aggiornaVeicoloAssegnato() {

		List<Prenotazione> prenList = ui.getPrenotazioniDriver(utente.getIdUtente());
		LocalDateTime now = LocalDateTime.now();

		var attuale = prenList.stream()
				.filter(p -> p.getStato() == StatoPrenotazione.ATTIVA)
				.filter(p -> !p.getDataInizio().isAfter(now))
				.filter(p -> p.getDataFine().isAfter(now))
				.findFirst();

		attuale.ifPresentOrElse(
				p -> lblVeicoloAssegnato.setText(p.getTarga()),
				() -> lblVeicoloAssegnato.setText("Nessuno")
		);
	}

	private void aggiornaBadgeNotifiche() {
		List<Notifica> nonLette = ui.getNotificheNonLette(utente.getIdUtente());
		badgeNotifiche.setVisible(!nonLette.isEmpty());
	}

	@FXML
	private void onApriPrenotazioni() {
		SceneManager.changeScene("/ui/views/prenotazioni/PrenotazioniView.fxml", utente);
	}

	@FXML
	private void onNuovaPrenotazione() {
		SceneManager.changeScene("/ui/views/prenotazioni/NuovaPrenotazioneView.fxml", utente);
	}

	@FXML
	private void onSegnalazioneStraordinaria() {
		try {
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/ui/views/notifiche/SegnalazioneStraordinariaView.fxml"));
			Parent root = loader.load();

			SegnalazioneStraordinariaController ctrl = loader.getController();
			ctrl.setUtente(utente);

			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setTitle("Segnalazione Straordinaria");
			dialog.setScene(new Scene(root));
			dialog.setResizable(false);
			dialog.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onApriNotifiche() {
		SceneManager.changeScene("/ui/views/notifiche/NotificheView.fxml", utente);
	}

	@FXML
	private void onLogout() {
		SceneManager.changeScene("/ui/views/LoginView.fxml");
	}
}
