package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Nơi này quyết định màn hình nào sẽ hiện lên đầu tiên
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/auction/views/login-view.fxml"));

        // Cài đặt kích thước cửa sổ (Rộng 600, Cao 500)
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        stage.setTitle("Hệ Thống Đấu Giá Realtime - Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}