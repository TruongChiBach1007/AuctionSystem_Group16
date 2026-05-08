package com.auction.controller;

import com.auction.model.core.Bid;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class AuctionRoomController {
// khai báo bảng
    @FXML private TableView<Bid> auctionTable;
    @FXML private TableColumn<Bid, String> nguoiDatCol;
    @FXML private TableColumn<Bid, Double> giaCol;
    @FXML private TableColumn<Bid, String> thoiGianCol;


    private ObservableList<Bid> bidHistory = FXCollections.observableArrayList();

    // --- Khai báo các thành phần giao diện trùng khớp với FXML ---
    @FXML private TextField bidInput;
    @FXML private Label statusLabel;
    @FXML private LineChart<Number, Number> priceChart;
    // Dữ liệu của biểu đồ
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0; // Đếm số lượt đặt giá
    private double currentHighestBid = 1000.0; // Giá khởi điểm

    @FXML private Label minutesLabel;
    @FXML private Label secondsLabel;

    private int totalSeconds = 300;
    private Timeline timeline;



    // Hàm này chạy ngay khi màn hình vừa bật lên
    @FXML
    public void initialize() {
        // Cài đặt biểu đồ
        //bidderName và amount trong hàm class Bid

        nguoiDatCol.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        giaCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        thoiGianCol.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        auctionTable.setItems(bidHistory);

        series = new XYChart.Series<>();
        series.setName("Biến động giá iPhone 15 Pro Max");
        priceChart.getData().add(series);

        // Nạp thử 1 điểm giá khởi điểm vào biểu đồ
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        statusLabel.setText("Giá khởi điểm là: VND" + currentHighestBid);

        //Hàm đê ngược
        startCountdown();
    }
    private void startCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (totalSeconds > 0) {
                totalSeconds--;
                updateClockDisplay();
            } else {
                timeline.stop();
                statusLabel.setText("🛑 PHIÊN ĐẤU GIÁ ĐÃ KẾT THÚC!");
                bidInput.setDisable(true);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateClockDisplay() {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        minutesLabel.setText(String.format("%02d", min));
        secondsLabel.setText(String.format("%02d", sec));

        if (totalSeconds < 30) {
            String redStyle = "-fx-text-fill: #ff4757; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
            minutesLabel.setStyle(redStyle);
            secondsLabel.setStyle(redStyle);
        }
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

                Bid newBidEntry = new Bid(null, newBid);
                bidHistory.add(0, newBidEntry);

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