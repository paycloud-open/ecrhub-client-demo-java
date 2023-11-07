package com.example.ecrhub.controller;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.manager.SceneManager;
import com.wiseasy.ecr.hub.sdk.protobuf.ECRHubRequestProto;
import com.wiseasy.ecr.hub.sdk.sp.serialport.SerialPortMessage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

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
    private TextArea request_sample_hex;

    @FXML
    private Button send_message_button;

    @FXML
    private TextArea response_sample_hex;

    @FXML
    private TextArea response_info;

    private static String current_choose = "Purchase";

    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        purchaseButton.setToggleGroup(group);
        refundButton.setToggleGroup(group);
        queryButton.setToggleGroup(group);
        closeButton.setToggleGroup(group);

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

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            RadioButton selectedRadioButton = (RadioButton) newValue;
            current_choose = selectedRadioButton.getText();
            initScreen();
        });
    }

    private void initScreen() {
        // 初始化
        merchant_order_no.setText(null);
        orig_merchant_order_no.setText(null);
        order_amount.setText(null);
        request_sample_hex.setText(null);
        send_message_button.setDisable(true);
        response_sample_hex.setText(null);
        response_info.setText(null);
        switch (current_choose) {
            case "Purchase": {
                orig_merchant_order_no_box.setVisible(false);
                orig_merchant_order_no_box.setManaged(false);
                order_amount_box.setVisible(true);
                order_amount_box.setManaged(true);
            } return;
            case "Refund": {
                orig_merchant_order_no_box.setVisible(true);
                orig_merchant_order_no_box.setManaged(true);
                order_amount_box.setVisible(false);
                order_amount_box.setManaged(false);
            } return;
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

        ECRHubRequestProto.RequestBizData bizData = builder.build();
        ECRHubRequestProto.ECRHubRequest request = ECRHubRequestProto.ECRHubRequest.newBuilder()
                .setRequestId(IdUtil.fastSimpleUUID())
                .setTimestamp(String.valueOf(System.currentTimeMillis()))
                .setTopic(topic)
                .setBizData(bizData)
                .build();
        byte[] message = new SerialPortMessage.DataMessage(request.toByteArray()).encode();
        request_sample_hex.setText(HexUtil.encodeHexStr(message, false));
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
            } return "ecrhub.pay.order";
            case "Refund": {
                if (StrUtil.isEmpty(orig_merchant_order_no.getText())) {
                    alert.setContentText("Please enter the original merchant order number");
                    alert.showAndWait();
                    return "error";
                }
            } return "ecrhub.pay.order";
            case "Query": return "ecrhub.pay.query";
            case "Close": return "ecrhub.pay.close";
        }
        return "error";
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("home", "/com/example/ecrhub/fxml/home.fxml");
        SceneManager.getInstance().switchScene("home");
    }


    public static void main(String[] args) {
        ECRHubRequestProto.RequestBizData bizData = ECRHubRequestProto.RequestBizData.newBuilder()
                .setMerchantOrderNo("O1698817488630")
                .setPayMethodCategory("BANKCARD")
                .setTransType("1")
                .setOrderAmount("1")
                .build();

        ECRHubRequestProto.ECRHubRequest request = ECRHubRequestProto.ECRHubRequest.newBuilder()
                .setRequestId(IdUtil.fastSimpleUUID())
                .setTimestamp(String.valueOf(System.currentTimeMillis()))
                .setTopic("ecrhub.pay.order")
                .setBizData(bizData)
                .build();

        byte[] message = new SerialPortMessage.DataMessage(request.toByteArray()).encode();
        System.out.println(HexUtil.encodeHexStr(message, false));
    }
}
