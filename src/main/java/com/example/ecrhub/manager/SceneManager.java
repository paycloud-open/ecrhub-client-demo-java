package com.example.ecrhub.manager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: yanzx
 * @date: 2023/10/11 12:25
 * @description:
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage stage;
    private Map<String, Scene> sceneMap;

    private SceneManager() {
        sceneMap = new HashMap<>();
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loadScene(String name, String fxmlFile) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 600);
            sceneMap.put(name, scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unloadScene(String name) {
        sceneMap.remove(name);
    }

    public void switchScene(String name) {
        Scene scene = sceneMap.get(name);
        if (scene != null) {
            stage.setScene(scene);
        }
    }

}
