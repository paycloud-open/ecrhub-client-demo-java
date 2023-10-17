package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.util.JSONFormatUtil;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.response.ECRHubResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

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

    private Alert alert;

    @FXML
    private void connectButtonAction(ActionEvent event) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connecting...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/wait.fxml"));
            loader.load();
            Parent root = loader.getRoot();
            alert.setGraphic(root);
        } catch (Exception e) {
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                connectButton.setDisable(true);

                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                alert.show();
                ECRHubResponse ecrHubResponse = client.connect2();
                return JSONFormatUtil.formatJson(ecrHubResponse);
            }
        };

        task.setOnSucceeded(success -> {
            String response_info = task.getValue();
            alert.close();
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            connect_info.setText(response_info);
        });

        task.setOnFailed(fail -> {
            alert.close();
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            connect_info.setText("Unconnected! \n Connect error! Please try again!");
        });

        task.setOnCancelled(cancel -> {
            alert.close();
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
            ECRHubClient client = ECRHubClientManager.getInstance().getClient();
            client.disconnect();
        } catch (Exception e) {

        } finally {
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            connect_info.setText("Unconnected!");
        }
    }
}
