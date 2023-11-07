package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

/**
 * @author: yanzx
 * @date: 2023/11/7 09:59
 * @description:
 */
public class HomeController {



    @FXML
    private void handleSettingButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("setting", "/com/example/ecrhub/fxml/setting.fxml");
        SceneManager.getInstance().switchScene("setting");
    }

    @FXML
    private void handleDemoButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        if (0 == ECRHubClientManager.getInstance().getConnectType()) {
            alert.setContentText("Please click \"Settings\" to connect the device first");
            alert.showAndWait();
            return;
        }
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleDebugButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        if (0 == ECRHubClientManager.getInstance().getConnectType()) {
            alert.setContentText("Please click \"Settings\" to connect the device first");
            alert.showAndWait();
            return;
        }
    }
}
