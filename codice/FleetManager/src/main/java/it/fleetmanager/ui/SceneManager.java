package it.fleetmanager.ui;

import it.fleetmanager.model.Utente;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage primaryStage;

    private static double screenWidth;
    private static double screenHeight;

    // ============================================================
    // INIT SCHERMO
    public static void initializeScreenSize() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        screenWidth = bounds.getWidth();
        screenHeight = bounds.getHeight();

        System.out.println("[SceneManager] Dimensioni schermo: " + screenWidth + " x " + screenHeight);
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        System.out.println("[SceneManager] primaryStage impostato.");
    }

    // ============================================================
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

    // ============================================================
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
            System.err.println("[SceneManager] Errore cambio scena (" + fxmlPath + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void ensureInitialized() {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager: primaryStage non impostato. Chiama setPrimaryStage(stage).");
        }
        if (screenWidth <= 0 || screenHeight <= 0) {
            // fallback: se ti sei dimenticato initializeScreenSize()
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            screenWidth = bounds.getWidth();
            screenHeight = bounds.getHeight();
        }
    }

    // opzionali
    public static double getScreenWidth() { return screenWidth; }
    public static double getScreenHeight() { return screenHeight; }
}
