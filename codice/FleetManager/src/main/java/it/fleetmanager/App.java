package it.fleetmanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.ui.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point dell'applicazione JavaFX FleetManager.
 * <p>
 * Inizializza lo stage principale e carica la prima view (login),
 * delegando al {@link SceneManager} la gestione delle dimensioni schermo
 * e del cambio scena.
 * </p>
 */
@SuppressWarnings("PMD.ShortClassName")
public class App extends Application {

	private static final Logger LOGGER = LogManager.getLogger(App.class);

	/**
	 * Costruttore pubblico richiesto da JavaFX e dichiarato esplicitamente
	 * per conformità alle regole PMD.
	 */
	public App() {
		super();
	}

	/**
	 * Metodo JavaFX invocato all'avvio dell'applicazione.
	 *
	 * @param primaryStage stage principale JavaFX
	 * @throws Exception in caso di errori durante il caricamento della view
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {

		SceneManager.initializeScreenSize();
		SceneManager.setPrimaryStage(primaryStage);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/LoginView.fxml"));
		Scene scene = new Scene(loader.load(), SceneManager.getScreenWidth(), SceneManager.getScreenHeight());

		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.show();
	}

	/**
	 * Main di avvio dell'applicazione.
	 *
	 * @param args argomenti da linea di comando
	 */
	public static void main(String[] args) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Lanciatore JavaFX avviato...");
		}
		launch(args);
	}
}
