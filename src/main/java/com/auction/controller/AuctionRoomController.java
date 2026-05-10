package com.auction.controller;

import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
    @FXML private TextField bidInput;
    @FXML private Label statusLabel;
    @FXML private LineChart<Number, Number> priceChart;
    @FXML private Label minutesLabel;
    @FXML private Label secondsLabel;
    @FXML private CheckBox autoBidCheckBox;
    @FXML private TextField maxBidField;
    @FXML private TextField stepBidField;
    @FXML private Tooltip autoBidTooltip;
    @FXML private Label helpIcon;

    private ObservableList<Bid> bidHistory = FXCollections.observableArrayList();

    // --- Khai báo các thành phần giao diện trùng khớp với FXML ---

    // Dữ liệu của biểu đồ
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0; // Đếm số lượt đặt giá
    private double currentHighestBid = 10000; // Giá khởi điểm
    private int totalSeconds = 30;
    private Timeline timeline;


    // Robot của autobid
    private boolean isAutoBidActive = false;
    private double maxAutoBidLimit = 0;
    private Bidder currentUser;

    // Hàm này chạy ngay khi màn hình vừa bật lên
    @FXML
    public void initialize() {
        currentUser = new Bidder(1, "bach123", "pass", "Trương Chí Bách", "bach@gmail.com", 500000);
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
            //Trả lại màu trắng khi gia hạn tgian
        }else{
            String whiteStyle = "-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
            minutesLabel.setStyle(whiteStyle);
            secondsLabel.setStyle(whiteStyle);
        }
    }
    // [MỚI] Hàm bổ trợ để Robot và Người dùng dùng chung logic đặt giá
    private void executeBidLogic(double newBid, String bidderName) {
        if (bidderName.equals("Bạn")) {
            currentUser.setBalance(currentUser.getBalance() - newBid);
        }
        currentHighestBid = newBid;
        bidCount++;
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));

        Bid newBidEntry = new Bid(bidderName, newBid);
        bidHistory.add(0, newBidEntry);

        statusLabel.setText("✅ " + bidderName + " đặt giá: " + String.format("%.0f", newBid));
        statusLabel.setStyle("-fx-text-fill: #27ae60;");

        if (totalSeconds < 10) {
            totalSeconds += 15;
            statusLabel.setText("⚡ Hệ thống tự động gia hạn 15s!");
            updateClockDisplay();
        } else {
            statusLabel.setText("✅ " + bidderName + " đặt giá thành công: VND " + newBid);
        }
        statusLabel.setStyle("-fx-text-fill: #27ae60;");

        // Kích hoạt Robot nếu người vừa đặt không phải Robot
        if (!bidderName.equals("Hệ thống (Auto)")) {
            checkAndExecuteAutoBid(currentHighestBid);
        }
    }
    // [MỚI] Hàm xử lý Robot Auto-Bid
    private void checkAndExecuteAutoBid(double latestPrice) {
        if (autoBidCheckBox.isSelected()) {
            try {
                double step = stepBidField.getText().isEmpty() ? 500.0 : Double.parseDouble(stepBidField.getText());
                double limit;
                if (maxBidField.getText().isEmpty()) {
                    limit = currentUser.getBalance();
                } else {
                    limit = Math.min(Double.parseDouble(maxBidField.getText()), currentUser.getBalance());
                }

                if (latestPrice < limit) {
                    double myNewBid = latestPrice + step;
                    if (myNewBid <= limit) {
                        Timeline robotThinking = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> {
                            executeBidLogic(myNewBid, "Hệ thống (Auto)");
                        }));
                        robotThinking.play();
                    }
                }
            } catch (Exception e) {
                statusLabel.setText("⚠️ Lỗi thông số Auto-Bid!");
            }
        }
    }
    @FXML
    private void handleHelpClick(MouseEvent event) {
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();

        if (autoBidTooltip.isShowing()) {
            autoBidTooltip.hide();
        } else {
            // Ép tooltip hiện ngay tại vị trí click chuột
            autoBidTooltip.show(window, event.getScreenX(), event.getScreenY() + 10);
        }
    }

    // --- Xử lý nút ĐẶT GIÁ ---
    @FXML
    public void handlePlaceBid(ActionEvent event) {
        String input = bidInput.getText();
        if (input == null || input.trim().isEmpty()) {
            statusLabel.setText("❌ Vui lòng nhập số tiền!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double newBid = Double.parseDouble(input);

            // Kiểm tra 1: Phải cao hơn giá hiện tại
            if (newBid <= currentHighestBid) {
                statusLabel.setText("❌ Giá phải cao hơn VND " + currentHighestBid);
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            // Kiểm tra 2: Phải nhỏ hơn hoặc bằng số dư tài khoản
            if (newBid > currentUser.getBalance()) {
                statusLabel.setText("❌ Số dư không đủ! (Bạn có: " + String.format("%.0f", currentUser.getBalance()) + ")");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            // Nếu vượt qua 2 bước chặn trên -> Thực hiện đặt giá
            executeBidLogic(newBid, "Bạn");
            bidInput.clear();

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Vui lòng nhập số hợp lệ!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
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