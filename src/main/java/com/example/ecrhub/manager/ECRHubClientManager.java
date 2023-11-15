package com.example.ecrhub.manager;

import com.example.ecrhub.pojo.ECRHubClientPo;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubClientFactory;
import com.wiseasy.ecr.hub.sdk.exception.ECRHubException;
import com.wiseasy.ecr.hub.sdk.model.response.ECRHubResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/17 10:17
 * @description:
 */
public class ECRHubClientManager {

    Logger logger = LoggerFactory.getLogger(ECRHubClientManager.class);

    private ECRHubClientManager() {
    }

    // 连接类型 0；NONE 1：USB 2；WLAN
    private int connectType = 0;

    private static ECRHubClientManager instance;

    // 串口连接client
    private ECRHubClient client;

    // 串口连接设备信息
    private ECRHubResponse connect_info;

    // socket是否开启监听
    private boolean open_listener = false;

    // socket连接client列表
    private LinkedHashMap<String, ECRHubClientPo> client_list = new LinkedHashMap<>();

    public static ECRHubClientManager getInstance() {
        if (instance == null) {
            instance = new ECRHubClientManager();
        }
        return instance;
    }

    public int getConnectType() {
        return connectType;
    }

    public void setConnectType(int connectType) {
        this.connectType = connectType;
    }

    public synchronized ECRHubClient getClient() throws ECRHubException {
        if (client == null) {
            client = ECRHubClientFactory.create("sp://");
        }
        return client;
    }

    public boolean isConnected() {
        try {
            return client.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public ECRHubResponse getConnect_info() {
        return connect_info;
    }

    public void setConnect_info(ECRHubResponse connect_info) {
        this.connect_info = connect_info;
    }

    public boolean isOpen_listener() {
        return open_listener;
    }

    public void setOpen_listener(boolean open_listener) {
        this.open_listener = open_listener;
    }

    public LinkedHashMap<String, ECRHubClientPo> getClient_list() {
        return client_list;
    }
}
