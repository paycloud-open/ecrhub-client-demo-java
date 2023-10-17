package com.example.ecrhub.controller;

import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author: yanzx
 * @date: 2023/10/13 15:59
 * @description:
 */
public class ShoppingController {

    public BorderPane container;

    public GridPane productGrid;

    public Label amount;

    @FXML
    ListView<String> listView;

    public void initialize() {
        PurchaseManager.getInstance().setTrans_amount(null);
        // 模拟商品数据，包含商品名称和价格
        String[][] products = {
                {"Beer", "5.00"},
                {"Coffee", "8.80"},
                {"Hamburg", "10.00"},
                {"Waffle", "0.98"},
                {"Milk", "3.50"},
                {"Pasta", "20.00"},
                {"Pudding", "3.33"},
                {"Cocktail", "12.00"},
                {"Steak", "15.00"}
        };

        int col = 0;
        int row = 0;

        for (String[] product : products) {
            String productName = product[0];
            String productPrice = product[1];

            // 商品图标，替换成实际商品图片的路径
            Image productImage = new Image(ShoppingController.class.getResourceAsStream("/image/" + productName + ".png"));
            ImageView imageView = new ImageView(productImage);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);

            // 商品名称和价格
            Label nameLabel = new Label(productName);
            Label priceLabel = new Label("$" + productPrice);

            // 包含商品图标、名称和价格的 VBox
            VBox productBox = new VBox(5);
            productBox.setAlignment(Pos.CENTER);
            Insets productInsets = new Insets(0, 0, 0, 30);
            productBox.setPadding(productInsets);

            HBox priceBox = new HBox(5);
            priceBox.getChildren().addAll(nameLabel, priceLabel);
            productBox.getChildren().addAll(imageView, priceBox);

            // 商品点击事件
            productBox.setOnMouseClicked(event -> addToCart(productName, productPrice));

            productGrid.add(productBox, col, row);

            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private void addToCart(String productName, String productPrice) {
        String item = productName + " - $" + productPrice;
        listView.getItems().add(item);

        String amount_str = amount.getText().replace("$", "");
        BigDecimal total_amount = new BigDecimal(amount_str).add(new BigDecimal(productPrice));
        amount.setText("$" + total_amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        PurchaseManager.getInstance().setTrans_amount(amount);
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("home", "/fxml/home.fxml");
        SceneManager.getInstance().switchScene("home");
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        if ("$0".equals(amount.getText())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setContentText("Please select product!");
            alert.showAndWait();
            return;
        }
        SceneManager.getInstance().loadScene("submit", "/fxml/submit.fxml");
        SceneManager.getInstance().switchScene("submit");
    }
}
