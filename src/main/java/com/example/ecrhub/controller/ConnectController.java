package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import com.example.ecrhub.util.ConfirmWindow;
import com.wiseasy.ecr.hub.sdk.ECRHubClient;
import com.wiseasy.ecr.hub.sdk.ECRHubClientFactory;
import com.wiseasy.ecr.hub.sdk.device.ECRHubClientWebSocketService;
import com.wiseasy.ecr.hub.sdk.device.ECRHubDevice;
import com.wiseasy.ecr.hub.sdk.device.ECRHubDeviceEventListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:37
 * @description:
 */
public class ConnectController {

    @FXML
    private Button listenerButton;

    @FXML
    private Button connectButton;

    @FXML
    ListView<String> listView;

    private String selected_device;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        connectButton.setDisable(true);
        if (instance.isOpen_listener()) {
            // 已连接
            listenerButton.setText("Disable Listening");
        } else {
            // 未连接
            listenerButton.setText("Enable Listening");
        }
        getConnectInfo();
    }

    @FXML
    protected void onListenerAction() {
        ECRHubClientWebSocketService devicePairInstance = ECRHubClientWebSocketService.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        if ("Enable Listening".equals(listenerButton.getText())) {
            // 开启监听
            try {
                devicePairInstance.start();
                devicePairInstance.setDeviceEventListener(new ECRHubDeviceEventListener() {
                    @Override
                    public void onAdded(ECRHubDevice ecrHubDevice) {
                        String terminal_sn = ecrHubDevice.getTerminal_sn();
                        ECRHubClientPo clientPo = new ECRHubClientPo();
                        clientPo.setIs_connected(false);
                        clientPo.setDevice(ecrHubDevice);
                        try {
                            ECRHubClient socketPortClient = ECRHubClientFactory.create(ecrHubDevice.getWs_address());
                            socketPortClient.connect();
                            clientPo.setIs_connected(true);
                            clientPo.setClient(socketPortClient);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ECRHubClientManager.getInstance().getClient_list().put(terminal_sn, clientPo);
                        Platform.runLater(() -> {
                            getConnectInfo();
                        });
                    }

                    @Override
                    public boolean onPaired(ECRHubDevice ecrHubDevice) {
                        String terminal_sn = ecrHubDevice.getTerminal_sn();
                        String content = "Device sn: \n    [" + terminal_sn + "]\n request connection";
                        boolean is_confirm = new ConfirmWindow().open("Connection confirmed",content);
                        if (is_confirm) {
                            try {
                                ECRHubClient socketPortClient = ECRHubClientFactory.create(ecrHubDevice.getWs_address());
                                socketPortClient.connect();
                                ECRHubClientPo clientPo = new ECRHubClientPo();
                                clientPo.setIs_connected(true);
                                clientPo.setDevice(ecrHubDevice);
                                clientPo.setClient(socketPortClient);
                                ECRHubClientManager.getInstance().getClient_list().put(terminal_sn, clientPo);
                                getConnectInfo();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Platform.runLater(() -> {
                                    alert.setContentText("Connect to ECR-Hub error!");
                                    alert.showAndWait();
                                });
                                return false;
                            }
                        }
                        return is_confirm;
                    }

                    @Override
                    public void unPaired(ECRHubDevice ecrHubDevice) {

                    }

                    @Override
                    public void onRemoved(ECRHubDevice ecrHubDevice) {

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                alert.setContentText("Enable Listening error!");
                alert.showAndWait();
                return;
            }

            ECRHubClientManager.getInstance().setOpen_listener(true);
            listenerButton.setText("Disable Listening");
        } else {
            try {
                devicePairInstance.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ECRHubClientManager.getInstance().setOpen_listener(false);
                listenerButton.setText("Enable Listening");
            }
        }
        getConnectInfo();
    }

    @FXML
    protected void onDisconnectAction() {
        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
        String terminal_sn = StrUtil.isEmpty(selected_device) ? "null" : selected_device.split("-")[0].trim();
        if (!client_list.containsKey(terminal_sn)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Device information does not exist!");
            alert.showAndWait();
            return;
        }

        ECRHubClientPo clientPo = client_list.get(terminal_sn);
        ECRHubClient client = clientPo.getClient();
        if ("Disconnect".equals(connectButton.getText())) {
            try {
                client.disconnect();
                clientPo.setIs_connected(false);
                client_list.put(terminal_sn, clientPo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                client = ECRHubClientFactory.create(clientPo.getDevice().getWs_address());
                client.connect();
                clientPo.setIs_connected(true);
                clientPo.setClient(client);
                client_list.put(terminal_sn, clientPo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getConnectInfo();
    }

    private void getConnectInfo() {
        // 查询设备连接情况
        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
        List<String> device_info = new ArrayList<>();
        for (String key : client_list.keySet()) {
            ECRHubClientPo clientPo = client_list.get(key);
            device_info.add(key + " - " + (clientPo.isIs_connected() ? "Connected" : "Unconnected"));
        }
        ObservableList connectDevices = FXCollections.observableArrayList(device_info);
        listView.setItems(connectDevices);

        // 渲染
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && StrUtil.isNotEmpty(item)) {
                    if (item.contains("Unconnected")) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.GREEN);
                    }
                    setText(item);
                }
            }
        });

        listView.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // 选中事件
        listView.getSelectionModel().selectedItemProperty().addListener((arg0, old_str, new_str) -> {
            // getSelectedIndex方法可获得选中项的序号，getSelectedItem方法可获得选中项的对象
//            String desc = String.format("您点了第%d项，快餐名称是%s",
//                    listView.getSelectionModel().getSelectedIndex(),
//                    listView.getSelectionModel().getSelectedItem());
            selected_device = listView.getSelectionModel().getSelectedItem();
            if (StrUtil.isNotEmpty(selected_device)) {
                connectButton.setDisable(false);
                if (selected_device.contains("Unconnected")) {
                    connectButton.setText("Connect");
                } else {
                    connectButton.setText("Disconnect");
                }
            } else {
                connectButton.setDisable(true);
            }
        });
    }

}
