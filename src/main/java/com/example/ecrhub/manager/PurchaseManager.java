package com.example.ecrhub.manager;

import com.wiseasy.ecr.hub.sdk.model.response.PurchaseResponse;

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
}
