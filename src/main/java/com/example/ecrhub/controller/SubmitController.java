package com.example.ecrhub.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubClientFactory;
import com.wiseasy.ecr.hub.sdk.ECRHubConfig;
import com.wiseasy.ecr.hub.sdk.model.request.PurchaseRequest;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * @author: yanzx
 * @date: 2023/10/11 12:38
 * @description:
 */
public class SubmitController {

    @FXML
    private Button returnButton;

    @FXML
    private Button submitButton;

    @FXML
    private ProgressIndicator progress_indicator;

    @FXML
    private Label wait_label;

    private Task<String> task = null;

    public TextField trans_amount;

    ECRHubClient client = null;

    public void initialize() {
        progress_indicator.setDisable(true);
        progress_indicator.setVisible(false);
        wait_label.setVisible(false);

        trans_amount.requestFocus();
        trans_amount.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 数据改变
                if (oldValue && !newValue) {
                    // 添加事件
                    String amount_str = trans_amount.getText();
                    if (StrUtil.isNotEmpty(amount_str)) {
                        BigDecimal amount;
                        try {
                            amount = new BigDecimal(amount_str);
                            trans_amount.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR!");
                            alert.setContentText("Please enter the correct amount");
                            alert.showAndWait();
                            trans_amount.setText(null);
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        SceneManager.getInstance().switchScene("home");
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
                trans_amount.setDisable(true);
                submitButton.setDisable(true);

                PurchaseManager.getInstance().setResponse(requestToECR(amount_str));
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
            trans_amount.setDisable(false);
            submitButton.setDisable(false);

            disconnectClient();
            alert.setContentText("Connect to ECH Hub error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            trans_amount.setDisable(false);
            submitButton.setDisable(false);

            disconnectClient();
        });

        Thread thread = new Thread(task);
        thread.start();
    }


    private PurchaseResponse requestToECR(String amount_str) throws Exception {
        ECRHubConfig config = new ECRHubConfig(CommonConstant.APP_ID);
        client = ECRHubClientFactory.create("sp://", config);
        client.connect();

        // Purchase
        PurchaseRequest request = new PurchaseRequest();
        request.setMerchant_order_no("DEMO" + new Date().getTime() + RandomUtil.randomNumbers(4));
        request.setOrder_amount(amount_str);
        request.setPay_method_category("BANKCARD");

        // Execute purchase request
        PurchaseResponse response = client.execute(request);
        System.out.println("Purchase Response:" + response);
        disconnectClient();
        return response;
    }

    private void disconnectClient() {
        try {
            client.disconnect();
        } catch (Exception e) {

        }
    }
}
