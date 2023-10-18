package com.example.ecrhub.pojo;

import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.device.ECRHubDevice;

/**
 * @author: yanzx
 * @date: 2023/10/18 16:45
 * @description:
 */
public class ECRHubClientPo {

    private ECRHubClient client;

    private boolean is_connected;

    private ECRHubDevice device;

    public ECRHubClient getClient() {
        return client;
    }

    public void setClient(ECRHubClient client) {
        this.client = client;
    }

    public boolean isIs_connected() {
        return is_connected;
    }

    public void setIs_connected(boolean is_connected) {
        this.is_connected = is_connected;
    }

    public ECRHubDevice getDevice() {
        return device;
    }

    public void setDevice(ECRHubDevice device) {
        this.device = device;
    }
}
