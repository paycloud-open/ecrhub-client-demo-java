package com.example.ecrhub.controller;

import com.codepay.register.sdk.ECRHubClient;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:08
 * @description:
 */
public class SettingController {

    public BorderPane container;

    public ChoiceBox<String> choiceBox;

    public void initialize() {
        choiceBox.getItems().addAll("USB", "WLAN/LAN");
        choiceBox.setValue("USB");
        setCenter("/com/example/ecrhub/fxml/usb.fxml");
    }

    @FXML
    protected void onChoiceBoxChange() {
        String value = choiceBox.getValue();
        if ("WLAN/LAN".equals(value)) {
            toConnect();
        } else {
            toUsb();
        }
    }

    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        int connect_type = 0;
        if ("USB".equals(choiceBox.getValue())) {
            // 串口连接
            try {
                ECRHubClient client = instance.getClient();
                if (client.isConnected()) {
                    connect_type = 1;
                }
            } catch (Exception e) {
            }
        } else {
            // WLAN连接
            boolean all_disconnect = true;
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            if (!client_list.isEmpty()) {
                for (String key : client_list.keySet()) {
                    ECRHubClientPo client_info = client_list.get(key);
                    if (client_info.isIs_connected()) {
                        all_disconnect = false;
                        break;
                    }
                }
                if (!all_disconnect) {
                    connect_type = 2;
                }
            }
        }

        instance.setConnectType(connect_type);
        SceneManager.getInstance().loadScene("home", "/com/example/ecrhub/fxml/home.fxml");
        SceneManager.getInstance().switchScene("home");
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
