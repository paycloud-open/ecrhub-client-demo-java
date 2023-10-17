package com.example.ecrhub.manager;

import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubClientFactory;
import com.wiseasy.ecr.hub.sdk.exception.ECRHubException;

/**
 * @author: yanzx
 * @date: 2023/10/17 10:17
 * @description:
 */
public class ECRHubClientManager {

    private ECRHubClientManager() {
    }

    private static ECRHubClientManager instance;

    private ECRHubClient client;

    private String connect_info;

    public static ECRHubClientManager getInstance() {
        if (instance == null) {
            instance = new ECRHubClientManager();
        }
        return instance;
    }

    public ECRHubClient getClient() throws ECRHubException {
        if (client != null && client.isConnected()) {
            return client;
        }
        client = ECRHubClientFactory.create("sp://");
        return client;
    }

    public boolean isConnected() {
        try {
            return client.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public String getConnect_info() {
        return connect_info;
    }

    public void setConnect_info(String connect_info) {
        this.connect_info = connect_info;
    }
}
