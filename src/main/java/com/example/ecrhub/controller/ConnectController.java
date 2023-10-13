package com.example.ecrhub.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:37
 * @description:
 */
public class ConnectController {

    @FXML
    ListView<String> listView;

    public void initialize() {
        getConnectInfo();
    }

    @FXML
    protected void onRefreshAction() {
        getConnectInfo();
    }

    private void getConnectInfo() {
        // TODO 查询设备连接情况
        ObservableList<String> updatedDevices = FXCollections.observableArrayList(
                "设备1 - 已连接",
                "设备2 - 未连接",
                "设备3 - 未连接",
                "设备4 - 未连接",
                "设备5 - 未连接"
        );
        listView.setItems(updatedDevices);

        // 渲染
        listView.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        // There is no item to display in this cell, so leave it empty
                        setGraphic(null);

                        // Clear the style from the cell
                        setStyle(null);
                    } else {
                        // If the item is equal to the first item in the list, set the style
                        if (item.contains("已连接")) {
                            // Set the background color to blue
                            setTextFill(Color.GREEN);
                        }
                        // Finally, show the item text in the cell
                        setText(item);

                    }
                }
            };
            return cell;
        });
    }

}
