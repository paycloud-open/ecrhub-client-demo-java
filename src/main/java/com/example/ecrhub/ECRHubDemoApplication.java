package com.example.ecrhub;

import com.example.ecrhub.manager.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ECRHubDemoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.getInstance().setStage(stage);
        SceneManager.getInstance().loadScene("home", "/fxml/home.fxml");

        FXMLLoader fxmlLoader = new FXMLLoader(ECRHubDemoApplication.class.getResource("/fxml/home.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/fxml/shopping.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("ECR-Hub Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}