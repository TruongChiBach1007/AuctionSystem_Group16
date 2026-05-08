package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionRoomController {

    // --- Khai báo các thành phần giao diện trùng khớp với FXML ---
    @FXML
    private TextField bidInput;

    @FXML
    private Label statusLabel;

    @FXML
    private LineChart<Number, Number> priceChart;

    // Dữ liệu của biểu đồ
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0; // Đếm số lượt đặt giá
    private double currentHighestBid = 1000.0; // Giá khởi điểm

    // Hàm này chạy ngay khi màn hình vừa bật lên
    @FXML
    public void initialize() {
        // Cài đặt biểu đồ
        series = new XYChart.Series<>();
        series.setName("Biến động giá iPhone 15 Pro Max");
        priceChart.getData().add(series);

        // Nạp thử 1 điểm giá khởi điểm vào biểu đồ
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        statusLabel.setText("Giá khởi điểm là: VND" + currentHighestBid);
    }

    // --- Xử lý nút ĐẶT GIÁ ---
    @FXML
    public void handlePlaceBid(ActionEvent event) {
        String input = bidInput.getText();

        try {
            double newBid = Double.parseDouble(input);

            // Kiểm tra xem giá đặt có cao hơn giá hiện tại không
            if (newBid > currentHighestBid) {
                currentHighestBid = newBid;
                bidCount++;

                // Thêm điểm mới vào biểu đồ để nó nhảy lên
                series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));

                statusLabel.setText("✅ Đặt giá thành công: VND" + newBid);
                statusLabel.setStyle("-fx-text-fill: #27ae60;"); // Màu xanh lá
                bidInput.clear(); // Xóa ô nhập liệu

                // --- CHỖ NÀY DÀNH CHO DUY (SOCKET) ---
                // Sau này Duy sẽ gửi giá trị "newBid" này lên Server ở đây!

            } else {
                statusLabel.setText("❌ Giá phải cao hơn VND" + currentHighestBid);
                statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Màu đỏ
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Vui lòng nhập số hợp lệ!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Màu đỏ
        }
    }

    // --- Xử lý nút QUAY LẠI ---
    @FXML
    public void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chủ - Sàn Đấu Giá");
            stage.centerOnScreen();

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file guest-dashboard.fxml");
            e.printStackTrace();
        }
    }
}