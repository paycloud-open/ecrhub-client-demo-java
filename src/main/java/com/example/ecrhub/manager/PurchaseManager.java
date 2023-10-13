package com.example.ecrhub.manager;

import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;
import javafx.scene.control.Label;

/**
 * @author: yanzx
 * @date: 2023/10/12 15:51
 * @description:
 */
public class PurchaseManager {

    private PurchaseManager() {
    }

    private static PurchaseManager instance;

    private PurchaseResponse response;

    private Label trans_amount;

    public static PurchaseManager getInstance() {
        if (instance == null) {
            instance = new PurchaseManager();
        }
        return instance;
    }

    public PurchaseResponse getResponse() {
        return response;
    }

    public void setResponse(PurchaseResponse response) {
        this.response = response;
    }

    public Label getTrans_amount() {
        return trans_amount;
    }

    public void setTrans_amount(Label trans_amount) {
        this.trans_amount = trans_amount;
    }
}
