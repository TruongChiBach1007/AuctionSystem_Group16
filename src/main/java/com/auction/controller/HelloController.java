package com.auction.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText; // fx:id này phải khớp với trong file FXML

    @FXML
    private void handleBidAction() {
        System.out.println("Nút đặt giá đã được nhấn!");
    }
}