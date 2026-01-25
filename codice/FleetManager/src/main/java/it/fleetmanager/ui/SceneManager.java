package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestisce il cambio delle scene JavaFX dell'applicazione FleetManager.
 * <p>
 * Questa classe funge da utility centralizzata per:
 * <ul>
 * <li>inizializzare le dimensioni dello schermo</li>
 * <li>impostare lo stage principale</li>
 * <li>caricare e cambiare scene FXML</li>
 * <li>passare l'utente ai controller che lo supportano</li>
 * </ul>
 * </p>
 *
 * <p>
 * La classe è implementata come utility class con soli membri statici e
 * costruttore privato per impedire l'istanziazione.
 * </p>
 */
public final class SceneManager {

	private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

	private static Stage primaryStage;

	private static double screenWidth;
	private static double screenHeight;

	// COSTRUTTORE PRIVATO (utility class)
	private SceneManager() {
		// impedisce istanziazione
	}

	// INIT SCHERMO
	public static void initializeScreenSize() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		screenWidth = bounds.getWidth();
		screenHeight = bounds.getHeight();

		LOGGER.info(() -> "[SceneManager] Dimensioni schermo: " + screenWidth + " x " + screenHeight);
	}

	public static void setPrimaryStage(Stage stage) {
		primaryStage = stage;
		LOGGER.info("[SceneManager] primaryStage impostato.");
	}

	// API PUBBLICHE
	public static void changeScene(String fxmlPath) {
		switchScene(fxmlPath, null, false);
	}

	public static void changeScene(String fxmlPath, Utente utente) {
		switchScene(fxmlPath, utente, false);
	}

	public static <T> T changeSceneWithController(String fxmlPath) {
		return switchScene(fxmlPath, null, true);
	}

	public static <T> T changeSceneWithController(String fxmlPath, Utente utente) {
		return switchScene(fxmlPath, utente, true);
	}

	// IMPLEMENTAZIONE UNICA (no duplicazioni)
	@SuppressWarnings("unchecked")
	private static <T> T switchScene(String fxmlPath, Utente utente, boolean returnController) {
		try {
			ensureInitialized();

			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
			Parent root = loader.load();

			Object controller = loader.getController();
			if (utente != null && controller instanceof UserAwareController uac) {
				uac.setUtente(utente);
			}

			Scene scene = new Scene(root, screenWidth, screenHeight);
			primaryStage.setScene(scene);

			return returnController ? (T) controller : null;

		} catch (Exception e) {

			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "[SceneManager] Errore cambio scena (" + fxmlPath + ")", e);
			}

			return null;
		}
	}

	private static void ensureInitialized() {
		if (primaryStage == null) {
			throw new IllegalStateException("SceneManager: primaryStage non impostato. Chiama setPrimaryStage(stage).");
		}

		if (screenWidth <= 0 || screenHeight <= 0) {
			Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
			screenWidth = bounds.getWidth();
			screenHeight = bounds.getHeight();
		}
	}

	// GETTERS
	public static double getScreenWidth() {
		return screenWidth;
	}

	public static double getScreenHeight() {
		return screenHeight;
	}
}
