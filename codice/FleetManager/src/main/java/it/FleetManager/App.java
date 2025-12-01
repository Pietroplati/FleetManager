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

		SceneManager.setPrimaryStage(primaryStage);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/views/LoginView.fxml"));
		Scene scene = new Scene(loader.load());

		primaryStage.setTitle("FleetManager - Login");
		primaryStage.setScene(scene);

		primaryStage.setResizable(true);
		primaryStage.setMaximized(true); // <-- massimizza all'avvio
		primaryStage.show();
	}

	public static void main(String[] args) {
		log.info("Lanciatore JavaFX avviato...");
		launch(args);
	}
}
