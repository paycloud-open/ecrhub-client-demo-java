package com.example.ecrhub.controller;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRDebugPo;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubSerialPortClient;
import com.wiseasy.ecr.hub.sdk.protobuf.ECRHubRequestProto;
import com.wiseasy.ecr.hub.sdk.sp.serialport.SerialPortMessage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author: yanzx
 * @date: 2023/11/7 14:08
 * @description:
 */
public class DebugController {

    @FXML
    private RadioButton purchaseButton;

    @FXML
    private RadioButton refundButton;

    @FXML
    private RadioButton queryButton;

    @FXML
    private RadioButton closeButton;

    @FXML
    private RadioButton send_raw;

    @FXML
    private RadioButton send_pretty;

    @FXML
    private RadioButton receive_raw;

    @FXML
    private RadioButton receive_pretty;

    @FXML
    private HBox orig_merchant_order_no_box;

    @FXML
    private HBox order_amount_box;

    @FXML
    private TextField merchant_order_no;

    @FXML
    private TextField orig_merchant_order_no;

    @FXML
    private TextField order_amount;

    @FXML
    private TextArea send_message;

    @FXML
    private Button create_message_button;

    @FXML
    private Button send_message_button;

    @FXML
    private TextArea receive_message;

    private static String current_choose = "Purchase";

    private static String REQUEST_ID;

    private ECRDebugPo DEBUG_PO;

    @FXML
    private VBox wait_vbox;

    private Task<String> send_task = null;

    public void initialize() {
        DEBUG_PO = new ECRDebugPo();

        ToggleGroup group = new ToggleGroup();
        purchaseButton.setToggleGroup(group);
        refundButton.setToggleGroup(group);
        queryButton.setToggleGroup(group);
        closeButton.setToggleGroup(group);

        ToggleGroup send_group = new ToggleGroup();
        send_raw.setToggleGroup(send_group);
        send_pretty.setToggleGroup(send_group);

        ToggleGroup receive_group = new ToggleGroup();
        receive_raw.setToggleGroup(receive_group);
        receive_pretty.setToggleGroup(receive_group);

        order_amount.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 数据改变
                if (oldValue && !newValue) {
                    // 添加事件
                    String amount_str = order_amount.getText();
                    if (StrUtil.isNotEmpty(amount_str)) {
                        BigDecimal amount;
                        try {
                            amount = new BigDecimal(amount_str);
                            order_amount.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR!");
                            alert.setContentText("Please enter the correct amount");
                            alert.showAndWait();
                            order_amount.setText(null);
                        }
                    }
                }
            }
        });

        send_message.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 数据改变
                if (oldValue && !newValue) {
                    String hex_string = send_message.getText();
                    if (StrUtil.isNotEmpty(hex_string)) {
                        try {
                            byte[] send_message_bytes = HexUtil.decodeHex(hex_string);
                            SerialPortMessage portMessage = new SerialPortMessage().decode(send_message_bytes);
                            byte[] message_bytes = portMessage.getMessageData();
                            ECRHubRequestProto.ECRHubRequest hubRequest = ECRHubRequestProto.ECRHubRequest.parseFrom(message_bytes);
                            REQUEST_ID = hubRequest.getRequestId();
                            send_message_button.setDisable(false);

                            DEBUG_PO.setSend_raw(hex_string);
                            DEBUG_PO.setSend_pretty(new SerialPortMessage().decode(send_message_bytes).toString());
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("ERROR!");
                            alert.setContentText("Please enter the correct HEX Message!");
                            alert.showAndWait();
                            send_message.setText(null);
                        }
                    }
                }
            }
        });

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selectedRadioButton = (RadioButton) newValue;
            current_choose = selectedRadioButton.getText();
            initScreen();
        });

        send_group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selectedRadioButton = (RadioButton) newValue;
            String send_choose = selectedRadioButton.getText();
            if ("Raw".equals(send_choose)) {
                send_message.setEditable(true);
                send_message.setText(DEBUG_PO.getSend_raw());
            } else {
                send_message.setEditable(false);
                send_message.setText(DEBUG_PO.getSend_pretty());
            }
        });

        receive_group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selectedRadioButton = (RadioButton) newValue;
            String receive_choose = selectedRadioButton.getText();
            if ("Raw".equals(receive_choose)) {
                receive_message.setText(DEBUG_PO.getReceive_raw());
            } else {
                receive_message.setText(DEBUG_PO.getReceive_pretty());
            }
        });
    }

    private void initScreen() {
        // 初始化
        DEBUG_PO = new ECRDebugPo();
        send_raw.setSelected(true);
        receive_raw.setSelected(true);
        merchant_order_no.setText(null);
        orig_merchant_order_no.setText(null);
        order_amount.setText(null);
        send_message.setText(null);
        send_message_button.setDisable(true);
        receive_message.setText(null);
        REQUEST_ID = null;
        switch (current_choose) {
            case "Purchase": {
                orig_merchant_order_no_box.setVisible(false);
                orig_merchant_order_no_box.setManaged(false);
                order_amount_box.setVisible(true);
                order_amount_box.setManaged(true);
            }
            return;
            case "Refund": {
                orig_merchant_order_no_box.setVisible(true);
                orig_merchant_order_no_box.setManaged(true);
                order_amount_box.setVisible(false);
                order_amount_box.setManaged(false);
            }
            return;
            case "Query":
            case "Close": {
                orig_merchant_order_no_box.setVisible(false);
                orig_merchant_order_no_box.setManaged(false);
                order_amount_box.setVisible(false);
                order_amount_box.setManaged(false);
            }
        }
    }

    @FXML
    private void createSampleMessageAction(ActionEvent event) {

        String topic = checkAndGetTopic();
        if ("error".equals(topic)) {
            return;
        }
        ECRHubRequestProto.RequestBizData.Builder builder = ECRHubRequestProto.RequestBizData.newBuilder();
        builder.setMerchantOrderNo(merchant_order_no.getText());
        if ("Purchase".equals(current_choose)) {
            builder.setPayMethodCategory("BANKCARD").setTransType("1").setOrderAmount(order_amount.getText());
        } else if ("Refund".equals(current_choose)) {
            builder.setOrigMerchantOrderNo(orig_merchant_order_no.getText()).setTransType("3");
        }

        REQUEST_ID = IdUtil.fastSimpleUUID();
        ECRHubRequestProto.RequestBizData bizData = builder.build();
        ECRHubRequestProto.ECRHubRequest request = ECRHubRequestProto.ECRHubRequest.newBuilder()
                .setRequestId(REQUEST_ID)
                .setTimestamp(String.valueOf(System.currentTimeMillis()))
                .setTopic(topic)
                .setBizData(bizData)
                .build();
        byte[] message = new SerialPortMessage.DataMessage(request.toByteArray()).encode();
        DEBUG_PO.setSend_raw(HexUtil.encodeHexStr(message, false));
        DEBUG_PO.setSend_pretty(new SerialPortMessage().decode(message).toString());

        send_message.setText(send_raw.isSelected() ? DEBUG_PO.getSend_raw() : DEBUG_PO.getSend_pretty());
        send_message_button.setDisable(false);
    }

    private String checkAndGetTopic() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        if (StrUtil.isEmpty(merchant_order_no.getText())) {
            alert.setContentText("Please enter the merchant order number");
            alert.showAndWait();
            return "error";
        }
        switch (current_choose) {
            case "Purchase": {
                if (StrUtil.isEmpty(order_amount.getText())) {
                    alert.setContentText("Please enter the order amount");
                    alert.showAndWait();
                    return "error";
                }
            }
            return "ecrhub.pay.order";
            case "Refund": {
                if (StrUtil.isEmpty(orig_merchant_order_no.getText())) {
                    alert.setContentText("Please enter the original merchant order number");
                    alert.showAndWait();
                    return "error";
                }
            }
            return "ecrhub.pay.order";
            case "Query":
                return "ecrhub.pay.query";
            case "Close":
                return "ecrhub.pay.close";
        }
        return "error";
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("home", "/com/example/ecrhub/fxml/home.fxml");
        SceneManager.getInstance().switchScene("home");
    }

    @FXML
    private void sendSampleMessageAction(ActionEvent event) {
        send_task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                create_message_button.setDisable(true);
                send_message_button.setDisable(true);

                wait_vbox.setVisible(true);
                wait_vbox.setManaged(true);
                receive_message.setVisible(false);
                receive_message.setManaged(false);


                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
                ECRHubSerialPortClient portClient = (ECRHubSerialPortClient) client;
                byte[] response_bytes = portClient.send(REQUEST_ID, HexUtil.decodeHex(DEBUG_PO.getSend_raw()));
                DEBUG_PO.setReceive_raw(HexUtil.encodeHexStr(response_bytes, false));
                DEBUG_PO.setReceive_pretty(new SerialPortMessage().decode(response_bytes).toString());

                receive_message.setText(receive_raw.isSelected() ? DEBUG_PO.getReceive_raw() : DEBUG_PO.getReceive_pretty());
                return "success";
            }
        };

        send_task.setOnSucceeded(success -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            receive_message.setVisible(true);
            receive_message.setManaged(true);

            create_message_button.setDisable(false);
        });

        send_task.setOnFailed(fail -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            receive_message.setVisible(true);
            receive_message.setManaged(true);

            create_message_button.setDisable(false);
            send_message_button.setDisable(false);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR!");
                alert.setContentText("Connect to ECR-Hub error!");
                alert.showAndWait();
            });
        });

        send_task.setOnCancelled(cancel -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            receive_message.setVisible(true);
            receive_message.setManaged(true);

            create_message_button.setDisable(false);
            send_message_button.setDisable(false);
        });

        Thread thread = new Thread(send_task);
        thread.start();
    }

    @FXML
    private void sendCancelAction(ActionEvent event) {
        if (send_task != null && send_task.isRunning()) {
            send_task.cancel();
        }
    }
}
