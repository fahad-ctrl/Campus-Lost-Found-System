package com.campus.lostfound;

import com.campus.lostfound.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point. Loads the Login view first; all further
 * navigation is handled by SceneManager from within the controllers.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.init(primaryStage);
        SceneManager.switchTo("login.fxml", "Login");
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(560);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
