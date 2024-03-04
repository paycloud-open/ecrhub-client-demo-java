package com.example.ecrhub.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubConfig;
import com.wiseasy.ecr.hub.sdk.model.request.CloseRequest;
import com.wiseasy.ecr.hub.sdk.model.request.PurchaseRequest;
import com.wiseasy.ecr.hub.sdk.model.response.CloseResponse;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: yanzx
 * @date: 2023/10/11 12:38
 * @description:
 */
public class SubmitController {

    public HBox pay_method;
    public Label merchant_order_no;
    public Button closeButton;
    public HBox order;
    Logger logger = LoggerFactory.getLogger(SubmitController.class);

    @FXML
    private Button submitButton;

    @FXML
    private ProgressIndicator progress_indicator;

    @FXML
    private Label wait_label;

    private Task<String> task = null;

    @FXML
    private TextField trans_amount;

    @FXML
    private Label terminal_sn;

    public ChoiceBox<String> terminalBox;

    public ChoiceBox<String> pay_method_category_choice;

    public ChoiceBox<String> pay_method_id_choice;


    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        if (1 == instance.getConnectType()) {
            // 串口连接初始化页面
            if (instance.getConnect_info().getDevice_data() != null) {
                terminal_sn.setText(instance.getConnect_info().getDevice_data().getDevice_sn());
            } else {
                terminal_sn.setText("Unknown");
            }
            terminalBox.setVisible(false);
            terminalBox.setManaged(false);
        } else {
            // WLAN 连接初始化页面
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            for (String key : client_list.keySet()) {
                ECRHubClientPo client_info = client_list.get(key);
                if (client_info.isIs_connected()) {
                    terminalBox.getItems().add(key);
                }
            }
            terminalBox.setValue(terminalBox.getItems().get(0));
            terminal_sn.setVisible(false);
            terminal_sn.setManaged(false);
        }
        progress_indicator.setDisable(true);
        progress_indicator.setVisible(false);
        wait_label.setVisible(false);
        closeButton.setDisable(false);

        if (PurchaseManager.getInstance().getTrans_amount() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Amount error!");
            alert.showAndWait();
        } else {
            trans_amount.setText(PurchaseManager.getInstance().getTrans_amount().getText());
        }
        pay_method_category_choice.getItems().addAll("BANKCARD", "QR_C_SCAN_B", "QR_B_SCAN_C");
        pay_method_category_choice.setValue("BANKCARD");

        pay_method_id_choice.getItems().addAll("PAYNOW","Duitnow QR", "Alipay", "Smart Ví", "Alipay+", "WecatPay", "UnionPay QR", "TNG EWALLET");
        pay_method_id_choice.setValue("PAYNOW");

        // 添加监听器
        pay_method_category_choice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("QR_C_SCAN_B".equals(newValue)) {
                // 当值为"QR_C_SCAN_B"时，显示pay_method_id_choice
                pay_method.setVisible(true);
                pay_method.setManaged(true);
            } else {
                // 否则隐藏pay_method_id_choice
                pay_method.setVisible(false);
                pay_method.setManaged(false);
            }
        });
        // 手动触发监听器
        pay_method_category_choice.fireEvent(new ActionEvent());
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
                order.setVisible(true);
                closeButton.setDisable(false);
                PurchaseManager.getInstance().setResponse(requestToECR(amount_str.replace("$", "")));
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            SceneManager.getInstance().loadScene("response", "/com/example/ecrhub/fxml/response.fxml");
            SceneManager.getInstance().switchScene("response");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            submitButton.setDisable(false);
            alert.setContentText("Read timeout!");
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

    @FXML
    private void handleCloseButtonAction(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Close");

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(false);
                wait_label.setVisible(false);
                PurchaseManager.getInstance().setCloseResponse(Close());
                CloseResponse closeResponse = PurchaseManager.getInstance().getCloseResponse();
                if (closeResponse != null && Objects.equals(closeResponse.getResponse_msg(), "")) {
                    return "success";
                } else {
                    String response_msg = closeResponse == null ? "Close error!" : closeResponse.getResponse_msg();
                    task.setOnFailed(fail -> {
                        alert.setContentText(response_msg);
                        alert.showAndWait();
                    });
                    return "fail";
                }
            }
        };

        task.setOnSucceeded(success -> {
            submitButton.setDisable(false);
            closeButton.setDisable(true);
        });//01-04 08:05:50.677  01-04 08:07:07.183

        task.setOnFailed(fail -> {
            CloseResponse closeResponse = PurchaseManager.getInstance().getCloseResponse();
            if (closeResponse != null && closeResponse.getResponse_msg() != null) {
                alert.setContentText(closeResponse.getResponse_msg());
                alert.showAndWait();
            }
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private CloseResponse Close() throws Exception {

        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        // 设备选择
        ECRHubClient client;
        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            client = client_list.get(terminalBox.getValue()).getClient();
        }
        CloseRequest request = new CloseRequest();
        request.setApp_id(CommonConstant.APP_ID);
        ECRHubConfig config = new ECRHubConfig();
        config.getSerialPortConfig().setReadTimeout(10000);
        request.setConfig(config);
//        request.setMerchant_order_no(merchant_order_no.getText());
        System.out.println("Start Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nClose request:" + request);
        CloseResponse closeResponse;
        try {
            closeResponse = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
            closeResponse = null;
        }
        System.out.println("End Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nClose response:" + closeResponse);
        return closeResponse;
    }

    private PurchaseResponse requestToECR(String amount_str) throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        // 设备选择
        ECRHubClient client;
        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> client_list = instance.getClient_list();
            client = client_list.get(terminalBox.getValue()).getClient();
        }

        // Purchase
        PurchaseRequest request = new PurchaseRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setMerchant_order_no("DEMO" + new Date().getTime() + RandomUtil.randomNumbers(4));
        request.setOrder_amount(amount_str);
        request.setPay_method_category(pay_method_category_choice.getValue());
        ECRHubConfig requestConfig = new ECRHubConfig();
        requestConfig.getSerialPortConfig().setReadTimeout(150000);
        request.setConfig(requestConfig);

        if ("QR_C_SCAN_B".equals(request.getPay_method_category())) {
            request.setPay_method_id(pay_method_id_choice.getValue());//Alipay PAYNOW
        }

        // Execute purchase request
        try {
            System.out.println("Start Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nPurchase Request:" + request);
            Platform.runLater(() -> {
                merchant_order_no.setText(request.getMerchant_order_no());
            });
            PurchaseResponse response = client.execute(request);
            System.out.println("End Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nPurchase Response:" + response);
            return response;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
