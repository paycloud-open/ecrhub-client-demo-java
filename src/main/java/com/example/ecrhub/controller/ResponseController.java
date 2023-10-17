package com.example.ecrhub.controller;

import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.util.JSONFormatUtil;
import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * @author: yanzx
 * @date: 2023/10/12 16:14
 * @description:
 */
public class ResponseController {

    @FXML
    private Button returnButton;
    public TextField trans_amount;
    public TextField merchant_order_no;
    public TextArea response_info;

    public void initialize() {
        PurchaseResponse response = PurchaseManager.getInstance().getResponse();
        if (response == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Error message!");
            alert.showAndWait();
            handleReturnButtonAction(null);
            return;
        }
        trans_amount.setText(response.getOrder_amount());
        merchant_order_no.setText(response.getMerchant_order_no());

        ScrollPane scrollPane = (ScrollPane) response_info.getChildrenUnmodifiable().get(0);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);  // 始终显示垂直滚动条
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);  // 根据需要显示水平滚动条
        response_info.setText(JSONFormatUtil.formatJson(response));
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("shopping", "/fxml/shopping.fxml");
        SceneManager.getInstance().switchScene("shopping");
    }
}
