package it.fleetmanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.fleetmanager.ui.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	private static final Logger log = LogManager.getLogger(App.class);

	@Override
	public void start(Stage primaryStage) throws Exception {

		// 1️⃣ Calcolare le dimensioni dello schermo una volta sola
		SceneManager.initializeScreenSize();

		// 2️⃣ Registrare lo stage nel SceneManager
		SceneManager.setPrimaryStage(primaryStage);

		// 3️⃣ Caricare la login già con dimensioni schermo
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/LoginView.fxml"));

		Scene scene = new Scene(loader.load(), SceneManager.getScreenWidth(), SceneManager.getScreenHeight());

		// 4️⃣ Applicazione finestra fullscreen senza flash
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.show();
	}

	public static void main(String[] args) {
		log.info("Lanciatore JavaFX avviato...");
		launch(args);
	}
}
