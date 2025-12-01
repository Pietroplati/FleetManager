package it.fleetmanager.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

	private static Stage primaryStage;

	public static void setPrimaryStage(Stage stage) {
		primaryStage = stage;
		System.out.println("[SceneManager] SET primaryStage = " + stage);

	}

	public static void changeScene(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
			Scene scene = new Scene(loader.load());

			primaryStage.setScene(scene);

			// 🔥 Forza la massimizzazione DOPO il cambio scena
			Platform.runLater(() -> primaryStage.setMaximized(true));

		} catch (Exception e) {
			System.err.println("[SceneManager] Errore changeScene: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static <T> T changeSceneWithController(String fxmlPath) {
	    try {
	        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
	        Scene scene = new Scene(loader.load());

	        primaryStage.setScene(scene);

	        // 🔵 RIMETTIAMO LA FINESTRA COME ERA PRIMA
	        primaryStage.setMaximized(true);   // <-- mantiene finestra “maximizada”
	        primaryStage.setResizable(true);   // <-- garantisce ridimensionamento

	        // 🔵 JavaFX a volte ignora il primo comando → runLater lo applica al frame già renderizzato
	        javafx.application.Platform.runLater(() -> {
	            primaryStage.setMaximized(true);
	        });

	        return loader.getController();

	    } catch (Exception e) {
	        System.err.println("[SceneManager] Errore changeSceneWithController: " + e.getMessage());
	        e.printStackTrace();
	        return null;
	    }
	}


}
