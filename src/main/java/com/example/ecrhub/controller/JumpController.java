package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:08
 * @description:
 */
public class JumpController {

    public BorderPane container;

    public ChoiceBox<String> choiceBox;

    @FXML
    private Button nextButton;

    private Map<String, Parent> centerMap;

    public void initialize() {
        centerMap = new HashMap<>();
        choiceBox.getItems().addAll("USB", "LAN/WLAN");
        choiceBox.setValue("USB");
        setCenter("/fxml/usb.fxml");
    }

    @FXML
    protected void onChoiceBoxChange() {
        String value = choiceBox.getValue();
        if ("LAN/WLAN".equals(value)) {
            toConnect();
        } else {
            toUsb();
        }
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        if ("USB".equals(choiceBox.getValue())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Please connect to ECR-Hub!");
            try {
                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                if (!client.isConnected()) {
                    alert.showAndWait();
                    return;
                }
            } catch (Exception e) {
                alert.showAndWait();
                return;
            }
        }

        SceneManager.getInstance().loadScene("shopping", "/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    private void setCenter(String path) {
        if (centerMap.containsKey(path)) {
            container.setCenter(centerMap.get(path));
            return;
        }

        URL resource = getClass().getResource(path);
        try {
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            Parent root = loader.getRoot();
            container.setCenter(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toConnect() {
        setCenter("/fxml/connect.fxml");
    }

    public void toUsb() {
        setCenter("/fxml/usb.fxml");
    }

}
