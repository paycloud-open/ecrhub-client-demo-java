package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.util.JSONFormatUtil;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.response.ECRHubResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * @author: yanzx
 * @date: 2023/10/17 10:16
 * @description:
 */
public class UsbController {

    @FXML
    private Button connectButton;

    @FXML
    private Button disconnectButton;

    public TextArea connect_info;

    private Task<String> task = null;

    @FXML
    private VBox wait_vbox;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        if (instance.isConnected()) {
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            connect_info.setText(instance.getConnect_info() == null ? "Connected!" : JSONFormatUtil.formatJson(instance.getConnect_info()));
        }
    }

    @FXML
    private void connectButtonAction(ActionEvent event) {
        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                connectButton.setDisable(true);

                wait_vbox.setVisible(true);
                wait_vbox.setManaged(true);
                connect_info.setVisible(false);
                connect_info.setManaged(false);

                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                ECRHubResponse ecrHubResponse = client.connect2();
                return JSON.toJSONString(ecrHubResponse);
            }
        };

        task.setOnSucceeded(success -> {
            String response_info = task.getValue();
            ECRHubResponse ecrHubResponse = JSONObject.parseObject(response_info, ECRHubResponse.class);
            ECRHubClientManager.getInstance().setConnect_info(ecrHubResponse);

            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);

            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            connect_info.setText(response_info);
        });

        task.setOnFailed(fail -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);

            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            connect_info.setText("Unconnected! \n Connect error! Please try again!");
        });

        task.setOnCancelled(cancel -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            connect_info.setVisible(true);
            connect_info.setManaged(true);
            try {
                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                client.disconnect();
            } catch (Exception e) {

            }
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            connect_info.setText("Unconnected!");
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
    }

    @FXML
    private void disconnectButtonAction(ActionEvent event) {
        try {
            ECRHubClientManager instance = ECRHubClientManager.getInstance();
            ECRHubClient client = instance.getClient();
            client.disconnect();
            instance.setConnect_info(null);
        } catch (Exception e) {

        } finally {
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            connect_info.setText("Unconnected!");
        }
    }
}
