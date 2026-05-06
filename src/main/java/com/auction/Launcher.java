package com.auction;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Sử dụng Application.launch thay vì gọi main trực tiếp
        Application.launch(AuctionApp.class, args);
    }
}