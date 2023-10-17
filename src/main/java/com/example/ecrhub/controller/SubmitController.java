package com.example.ecrhub.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.request.PurchaseRequest;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.util.Date;

/**
 * @author: yanzx
 * @date: 2023/10/11 12:38
 * @description:
 */
public class SubmitController {

    @FXML
    private Button submitButton;

    @FXML
    private ProgressIndicator progress_indicator;

    @FXML
    private Label wait_label;

    private Task<String> task = null;

    @FXML
    private Label trans_amount;

    ECRHubClient client = null;

    public void initialize() {
        progress_indicator.setDisable(true);
        progress_indicator.setVisible(false);
        wait_label.setVisible(false);

        if (PurchaseManager.getInstance().getTrans_amount() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Amount error!");
            alert.showAndWait();
        } else {
            trans_amount.setText(PurchaseManager.getInstance().getTrans_amount().getText());
        }
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");

        String amount_str = trans_amount.getText();
        if (StrUtil.isEmpty(amount_str)) {
            alert.setContentText("Please enter trans amount");
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                submitButton.setDisable(true);

                PurchaseManager.getInstance().setResponse(requestToECR(amount_str.replace("$", "")));
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            SceneManager.getInstance().loadScene("response", "/fxml/response.fxml");
            SceneManager.getInstance().switchScene("response");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            submitButton.setDisable(false);

            alert.setContentText("Connect to ECH Hub error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            trans_amount.setDisable(false);
            submitButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.start();
    }


    private PurchaseResponse requestToECR(String amount_str) throws Exception {
        ECRHubClient client = ECRHubClientManager.getInstance().getClient();

        // Purchase
        PurchaseRequest request = new PurchaseRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setMerchant_order_no("DEMO" + new Date().getTime() + RandomUtil.randomNumbers(4));
        request.setOrder_amount(amount_str);
        request.setPay_method_category("BANKCARD");

        // Execute purchase request
        PurchaseResponse response = client.execute(request);
        System.out.println("Purchase Response:" + response);
        return response;
    }

}
