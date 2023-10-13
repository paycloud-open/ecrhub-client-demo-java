package com.example.ecrhub.controller;

import com.example.ecrhub.manager.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

/**
 * @author: yanzx
 * @date: 2023/10/11 10:08
 * @description:
 */
public class JumpController {

    public BorderPane container;

    public ChoiceBox<String> choiceBox;

    @FXML
    private Button nextButton;

    public void initialize() {
        choiceBox.getItems().addAll("LAN/WLAN", "USB");
        choiceBox.setValue("LAN/WLAN");
        URL resource = this.getClass().getResource("/fxml/connect.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onChoiceBoxChange() {
        String value = choiceBox.getValue();
        if ("LAN/WLAN".equals(value)) {
            toConnect();
        } else {
            toBlack();
        }
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("submit", "/fxml/submit.fxml");
        SceneManager.getInstance().switchScene("submit");
    }

    private void setCenter(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.load();
        Parent root = loader.getRoot();
        container.setCenter(root);
    }

    public void toConnect() {
        URL resource = getClass().getResource("/fxml/connect.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toBlack() {
        URL resource = getClass().getResource("/fxml/black.fxml");
        try {
            setCenter(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
