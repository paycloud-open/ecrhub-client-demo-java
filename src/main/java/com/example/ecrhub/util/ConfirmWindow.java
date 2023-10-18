package com.example.ecrhub.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author: yanzx
 * @date: 2023/10/18 14:55
 * @description:
 */
public class ConfirmWindow {

    private Boolean isConfirmed = null;
    private Stage confirmWindow;

    public boolean open(String title, String content) {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20, 50, 20, 50));
        vBox.setSpacing(20);

        Label contentLabel = new Label(content);
        contentLabel.setFont(new Font(20));
        vBox.getChildren().add(contentLabel);

        HBox confirmHBox = new HBox();
        confirmHBox.setSpacing(20);
        {
            // 添加确认、取消两个按钮
            Button confirmButton = new Button("Confirm");
            confirmButton.setDefaultButton(true);
            Button cancelButton = new Button("Cancel");
            confirmHBox.getChildren().addAll(confirmButton, cancelButton);

            confirmButton.setOnAction(e -> {
                isConfirmed = true;
                confirmWindow.close();
            });

            cancelButton.setOnAction(e -> {
                isConfirmed = false;
                confirmWindow.close();
            });
        }
        vBox.getChildren().add(confirmHBox);

        Scene scene = new Scene(vBox);

        Platform.runLater(() -> {
            confirmWindow = new Stage();
            confirmWindow.setTitle(title);
            confirmWindow.initModality(Modality.APPLICATION_MODAL);
            confirmWindow.setScene(scene);
            confirmWindow.showAndWait(); // 执行代码在此处暂停，当窗口关闭后继续执行后面的代码，即 return 一个确认状态
        });

        while (isConfirmed == null) {
            try {
                Thread.sleep(1000);
                System.out.println("wait!!!!");
            } catch (Exception e) {

            }
        }
        return isConfirmed;
    }
}
