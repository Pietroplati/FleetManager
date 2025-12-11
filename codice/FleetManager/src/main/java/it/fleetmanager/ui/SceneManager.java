package it.fleetmanager.ui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SceneManager {

	private static Stage primaryStage;

	// Dimensioni dello schermo
	private static double screenWidth;
	private static double screenHeight;

	// ============================================================
	// INIZIALIZZAZIONE DIMENSIONI SCHERMO
	// ============================================================
	public static void initializeScreenSize() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		screenWidth = bounds.getWidth();
		screenHeight = bounds.getHeight();

		System.out.println("[SceneManager] Dimensioni schermo: " + screenWidth + " x " + screenHeight);
	}

	public static double getScreenWidth() {
		return screenWidth;
	}

	public static double getScreenHeight() {
		return screenHeight;
	}

	// ============================================================
	// SET STAGE PRIMARIO
	// ============================================================
	public static void setPrimaryStage(Stage stage) {
		primaryStage = stage;
		System.out.println("[SceneManager] primaryStage impostato.");
	}

	// ============================================================
	// CAMBIO SCENA (SOLO VIEW)
	// ============================================================
	public static void changeScene(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));

			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);

			primaryStage.setScene(scene);

		} catch (Exception e) {
			System.err.println("[SceneManager] Errore changeScene: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// ============================================================
	// CAMBIO SCENA CON CONTROLLER
	// ============================================================
	public static <T> T changeSceneWithController(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));

			Scene scene = new Scene(loader.load(), screenWidth, screenHeight);

			primaryStage.setScene(scene);

			return loader.getController();

		} catch (Exception e) {
			System.err.println("[SceneManager] Errore changeSceneWithController: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
