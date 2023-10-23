package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:08
 * @description:
 */
public class JumpController {

    public BorderPane container;

    public ChoiceBox<String> choiceBox;

    public void initialize() {
        choiceBox.getItems().addAll("USB", "LAN/WLAN");
        choiceBox.setValue("USB");
        setCenter("/com/example/ecrhub/fxml/usb.fxml");
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
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        int connect_type = 1;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setContentText("Please connect to ECR-Hub!");
        if ("USB".equals(choiceBox.getValue())) {
            // 串口连接
            try {
                ECRHubClient client = instance.getClient();
                if (!client.isConnected()) {
                    alert.showAndWait();
                    return;
                }
            } catch (Exception e) {
                alert.showAndWait();
                return;
            }
        } else {
            // WLAN连接
            if (instance.getClient_list().isEmpty()) {
                alert.showAndWait();
                return;
            }
            boolean all_disconnect = true;
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            for (String key: client_list.keySet()) {
                ECRHubClientPo client_info = client_list.get(key);
                if (client_info.isIs_connected()) {
                    all_disconnect = false;
                    break;
                }
            }
            if (all_disconnect) {
                alert.showAndWait();
                return;
            }
            connect_type = 2;
        }

        ECRHubClientManager.getInstance().setGetConnectType(connect_type);
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    private void setCenter(String path) {
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
        setCenter("/com/example/ecrhub/fxml/connect.fxml");
    }

    public void toUsb() {
        setCenter("/com/example/ecrhub/fxml/usb.fxml");
    }

}
