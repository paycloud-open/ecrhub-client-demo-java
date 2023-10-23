package com.example.ecrhub.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.*;

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
    ListView<String> pairedList;

    @FXML
    ListView<String> unPairedList;

    @FXML
    private Button refreshButton;

    @FXML
    private Button pairedButton;

    private String selected_device;

    private String pairing_device;

    List<ECRHubDevice> unpaired_list;

    private Task<String> task = null;

    @FXML
    private VBox wait_vbox;

    public void initialize() {
        ECRHubClientManager instance = ECRHubClientManager.getInstance();
        connectButton.setDisable(true);
        if (instance.isOpen_listener()) {
            // 已连接
            listenerButton.setText("Disable Listening");
            refreshButton.setDisable(false);
            getUnpairedInfo();
        } else {
            // 未连接
            listenerButton.setText("Enable Listening");
            refreshButton.setDisable(true);
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
                // 查询已配对/未配对设备
                getUnpairedInfo();
                getPairedInfo(devicePairInstance);
                devicePairInstance.setDeviceEventListener(new ECRHubDeviceEventListener() {
                    @Override
                    public void onAdded(ECRHubDevice ecrHubDevice) {
                        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
                        String terminal_sn = ecrHubDevice.getTerminal_sn();
                        if (!client_list.containsKey(terminal_sn) || !client_list.get(terminal_sn).isIs_connected()) {
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
                            client_list.put(terminal_sn, clientPo);
                            Platform.runLater(() -> {
                                getConnectInfo();
                            });
                        }
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
                    public void onUnpaired(ECRHubDevice ecrHubDevice) {
                        String terminal_sn = ecrHubDevice.getTerminal_sn();
                        ECRHubClientManager.getInstance().getClient_list().remove(terminal_sn);
                        getConnectInfo();
                    }

                    @Override
                    public void onRemoved(ECRHubDevice ecrHubDevice) {
                        String terminal_sn = ecrHubDevice.getTerminal_sn();
                        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
                        if (client_list.containsKey(terminal_sn)) {
                            ECRHubClientPo clientPo = client_list.get(terminal_sn);
                            if (clientPo.isIs_connected()) {
                                clientPo.setIs_connected(false);
                                client_list.put(terminal_sn, clientPo);
                                getConnectInfo();
                            }
                        }
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
            refreshButton.setDisable(false);
        } else {
            try {
                devicePairInstance.stop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ECRHubClientManager.getInstance().setOpen_listener(false);
                listenerButton.setText("Enable Listening");
                refreshButton.setDisable(true);
            }
        }
        getConnectInfo();
    }

    @FXML
    protected void onDisconnectAction() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");

        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
        String terminal_sn = StrUtil.isEmpty(selected_device) ? "null" : selected_device.split("-")[0].trim();
        if (!client_list.containsKey(terminal_sn)) {
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
                if (StrUtil.isEmpty(clientPo.getDevice().getWs_address())) {
                    alert.setContentText("The device is offline and cannot be connected");
                    alert.showAndWait();
                    return;
                }
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

    @FXML
    private void pairingButtonAction() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setContentText("Unpaired device information does not exist!");

        if (StrUtil.isEmpty(pairing_device) || unpaired_list == null || unpaired_list.size() == 0) {
            alert.showAndWait();
            return;
        }

        ECRHubDevice unpaired_device = unpaired_list.stream().
                filter(device -> pairing_device.equals(device.getTerminal_sn()))
                .findFirst().orElse(null);

        if (unpaired_device == null) {
            alert.showAndWait();
            return;
        }

        task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                refreshButton.setDisable(true);
                pairedButton.setDisable(true);

                wait_vbox.setVisible(true);
                wait_vbox.setManaged(true);
                unPairedList.setVisible(false);
                unPairedList.setManaged(false);

                ECRHubClient socketPortClient = ECRHubClientFactory.create(unpaired_device.getWs_address());
                socketPortClient.connect();
                ECRHubClientPo clientPo = new ECRHubClientPo();
                clientPo.setIs_connected(true);
                clientPo.setDevice(unpaired_device);
                clientPo.setClient(socketPortClient);
                return JSON.toJSONString(clientPo);
            }
        };

        task.setOnSucceeded(success -> {
            String response_info = task.getValue();
            ECRHubClientPo clientPo = JSONObject.parseObject(response_info, ECRHubClientPo.class);

            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            unPairedList.setVisible(true);
            unPairedList.setManaged(true);

            refreshButton.setDisable(true);
            ECRHubClientManager.getInstance().getClient_list().put(pairing_device, clientPo);
            getConnectInfo();
            getUnpairedInfo();
        });

        task.setOnFailed(fail -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            unPairedList.setVisible(true);
            unPairedList.setManaged(true);

            refreshButton.setDisable(false);
        });

        task.setOnCancelled(cancel -> {
            wait_vbox.setVisible(false);
            wait_vbox.setManaged(false);
            unPairedList.setVisible(true);
            unPairedList.setManaged(true);

            LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
            if (client_list.containsKey(pairing_device)) {
                ECRHubClientPo clientPo = client_list.get(pairing_device);
                try {
                    ECRHubClient client = clientPo.getClient();
                    client.disconnect();
                } catch (Exception e) {

                } finally {
                    client_list.remove(pairing_device);
                }
            }
            refreshButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.start();
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        if (task != null && task.isRunning()) {
            task.cancel();
        }
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
        pairedList.setItems(connectDevices);

        // 渲染
        pairedList.setCellFactory(list -> new ListCell<>() {
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

        pairedList.getStylesheets().add(getClass().getResource("/com/example/ecrhub/css/style.css").toExternalForm());

        // 选中事件
        pairedList.getSelectionModel().selectedItemProperty().addListener((arg0, old_str, new_str) -> {
            // getSelectedIndex方法可获得选中项的序号，getSelectedItem方法可获得选中项的对象
//            String desc = String.format("您点了第%d项，快餐名称是%s",
//                    listView.getSelectionModel().getSelectedIndex(),
//                    listView.getSelectionModel().getSelectedItem());
            selected_device = pairedList.getSelectionModel().getSelectedItem();
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

    public void getUnpairedInfo() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        ECRHubClientWebSocketService clientWebSocketService = ECRHubClientWebSocketService.getInstance();
        if (!clientWebSocketService.isRunning()) {
            alert.setContentText("Please start listening!");
            alert.showAndWait();
            return;
        }

        unpaired_list = clientWebSocketService.getUnpairedDeviceList();
        if (unpaired_list == null || unpaired_list.size() == 0) {
            return;
        }

        List<String> unpaired_info = new ArrayList<>();
        unpaired_list.forEach(device -> unpaired_info.add(device.getTerminal_sn()));
        ObservableList unpairedDevices = FXCollections.observableArrayList(unpaired_info);
        unPairedList.setItems(unpairedDevices);

        // 渲染
        unPairedList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && StrUtil.isNotEmpty(item)) {
                    setTextFill(Color.GREEN);
                    setText(item);
                }
            }
        });

        unPairedList.getStylesheets().add(getClass().getResource("/com/example/ecrhub/css/style.css").toExternalForm());

        // 选中事件
        unPairedList.getSelectionModel().selectedItemProperty().addListener((arg0, old_str, new_str) -> {
            pairing_device = unPairedList.getSelectionModel().getSelectedItem();
            pairedButton.setDisable(StrUtil.isEmpty(pairing_device));
        });
    }

    public void getPairedInfo(ECRHubClientWebSocketService clientWebSocketService) {
        LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
        List<ECRHubDevice> paired_list = clientWebSocketService.getPairedDeviceList();
        if (paired_list != null && paired_list.size() > 0) {
            paired_list.forEach(device -> {
                ECRHubClientPo clientPo = new ECRHubClientPo();
                clientPo.setDevice(device);
                clientPo.setIs_connected(false);
                client_list.put(device.getTerminal_sn(), clientPo);
            });
        }
    }

}
