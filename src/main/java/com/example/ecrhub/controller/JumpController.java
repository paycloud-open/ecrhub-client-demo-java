package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

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

    public void initialize() {
        choiceBox.getItems().addAll("USB", "LAN/WLAN");
        choiceBox.setValue("USB");
        URL resource = this.getClass().getResource("/fxml/usb.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setCenter(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.load();
        Parent root = loader.getRoot();
        container.setCenter(root);
    }

    public void toConnect() {
        URL resource = getClass().getResource("/fxml/connect.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toUsb() {
        URL resource = getClass().getResource("/fxml/usb.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
