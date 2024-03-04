package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.constant.CommonConstant;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.example.ecrhub.util.JSONFormatUtil;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.model.request.RefundRequest;
import com.wiseasy.ecr.hub.sdk.model.response.RefundResponse;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author: yanzx
 * @date: 2023/10/12 16:14
 * @description:
 */
public class RefundResponseController {

    public Button refundButton;
    public TextField orig_merchant_order_no;
    @FXML
    private Button returnButton;
    public TextField trans_amount;

    public TextField merchant_order_no;
    public TextArea response_info;
    @FXML
    private Label wait_label;
    @FXML
    private ProgressIndicator progress_indicator;


    private Task<String> task = null;
    public ChoiceBox<String> terminalBox;

    public void initialize() {
        RefundResponse refundResponse = PurchaseManager.getInstance().getRefundResponse();
        orig_merchant_order_no.setText(null);
        merchant_order_no.setText(null);
        response_info.setText(null);
        trans_amount.setText(null);
        if (refundResponse != null) {
            trans_amount.setText(refundResponse.getOrder_amount());
            orig_merchant_order_no.setText(refundResponse.getMerchant_order_no());
            response_info.setText(JSONFormatUtil.formatJson(refundResponse));
        }
    }

    @FXML
    private void refundReturnButtonAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
        PurchaseManager.getInstance().setRefundResponse(null);
        SceneManager.getInstance().loadScene("shopping", "/com/example/ecrhub/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }

    @FXML
    private void handleRefundButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        String origMerchantOrderNo = orig_merchant_order_no.getText();
        if (StrUtil.isEmpty(origMerchantOrderNo)) {
            alert.setContentText("Please enter orig_merchant_order_no");
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progress_indicator.setVisible(true);
                wait_label.setVisible(true);
                refundButton.setDisable(true);
                PurchaseManager.getInstance().setRefundResponse(refund());
                return "success";
            }
        };

        task.setOnSucceeded(success -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            SceneManager.getInstance().loadScene("refundResponse", "/com/example/ecrhub/fxml/refundResponse.fxml");
            SceneManager.getInstance().switchScene("refundResponse");
        });

        task.setOnFailed(fail -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            alert.setContentText("connect error!");
            alert.showAndWait();
        });

        task.setOnCancelled(cancel -> {
            progress_indicator.setVisible(false);
            wait_label.setVisible(false);
            refundButton.setDisable(false);
            PurchaseManager.getInstance().setRefundResponse(null);
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    private RefundResponse refund() throws Exception {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        ECRHubClient client;

        if (1 == instance.getConnectType()) {
            client = instance.getClient();
        } else {
            LinkedHashMap<String, ECRHubClientPo> clientList = instance.getClient_list();
            client = clientList.get(terminalBox.getValue()).getClient();
        }

        String[] origMerchantOrderNumbers = orig_merchant_order_no.getText().split(",");
        System.out.println(Arrays.toString(origMerchantOrderNumbers));

        List<RefundResponse> refundResponses = new ArrayList<>();

        for (String origMerchantOrderNo : origMerchantOrderNumbers) {
            RefundRequest request = createRefundRequest(origMerchantOrderNo);
            System.out.println("Start Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nRefund request:" + request);
            RefundResponse refundResponse = client.execute(request);
            System.out.println("End Time：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")) + "\nRefund response:" + refundResponse);
            refundResponses.add(refundResponse);
        }
        // 返回最后一个响应
        return refundResponses.isEmpty() ? null : refundResponses.get(refundResponses.size() - 1);
    }

    private RefundRequest createRefundRequest(String origMerchantOrderNo) {
        String MerchantOrderNo = "C" + origMerchantOrderNo + "Refund";
        if (merchant_order_no.getText() != null) {
            MerchantOrderNo = merchant_order_no.getText();
        }
        RefundRequest request = new RefundRequest();
        request.setApp_id(CommonConstant.APP_ID);
        request.setOrig_merchant_order_no(origMerchantOrderNo);
        request.setMerchant_order_no(MerchantOrderNo);
        return request;
    }

}