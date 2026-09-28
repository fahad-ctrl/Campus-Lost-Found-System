package com.campus.lostfound;

import com.campus.lostfound.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main {

    public static void main(String[] args) {
        Application.launch(CampusLostFoundApp.class, args);
    }

    public static class CampusLostFoundApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            SceneManager.init(primaryStage);
            SceneManager.switchTo("login.fxml", "Login");
            primaryStage.setMinWidth(760);
            primaryStage.setMinHeight(560);
            primaryStage.show();
        }
    }
}
