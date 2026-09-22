package com.campus.lostfound.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Centralizes switching the root of the main stage between the app's
 * FXML views, and applying the shared stylesheet.
 */
public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/com/campus/lostfound/fxml/" + fxmlFile));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        SceneManager.class.getResource("/com/campus/lostfound/css/style.css")
                ).toExternalForm());

        primaryStage.setTitle("Campus Lost & Found - " + title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
}
