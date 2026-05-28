package com.auction.controller;

import com.auction.model.core.Bid;
import com.auction.model.items.Item;
import com.auction.model.users.Bidder;
import com.auction.model.users.User;
import com.auction.network.AuctionClient;
import com.auction.network.AuctionMessage;
import com.auction.network.MessageType;
import com.auction.security.AuthService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;

public class AuctionRoomController {
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
    @FXML private Label lblCurrentBalance;
    // [FIX 3] Label hiển thị giá người dùng đang đặt hiện tại
    @FXML private Label lblMyCurrentBid;

    private final ObservableList<Bid> bidHistory = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0;
    private double currentHighestBid = 10000;
    private int totalSeconds = 600;
    private Timeline timeline;
    private Bidder currentUser;
    private Item auctionItem;

    // [FIX 2] Giá đặt hiện tại của bản thân người dùng
    private double myCurrentBid = 0;

    // [FIX 1+2] Instance AuctionClient để giao tiếp với server
    private final AuctionClient client = new AuctionClient();

    public void setCurrentUser(String username) {
        User loggedInUser = AuthService.getInstance().getCurrentUser();
        if (loggedInUser instanceof Bidder bidder) {
            this.currentUser = bidder;
        } else {
            this.currentUser = new Bidder(1, username, "pass", username, username + "@gmail.com", 500000.0);
        }
        if (statusLabel != null) {
            statusLabel.setText("Chào mừng " + currentUser.getUsername() + "! Sẵn sàng đấu giá.");
        }
        updateBalanceUI();
        // [FIX 1] Kết nối tới server sau khi biết user là ai
        connectToServer();
    }

    public void updateBalanceUI() {
        if (currentUser != null && lblCurrentBalance != null) {
            lblCurrentBalance.setText(String.format("%,.0f VNĐ", currentUser.getBalance()));
        }
    }

    public void setAuctionItem(Item item) {
        this.auctionItem = item;
        this.currentHighestBid = item.getCurrentPrice();
        if (series != null) {
            series.setName("Biến động giá " + item.getName());
            series.getData().clear();
            bidCount = 0;
            series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        }
        if (statusLabel != null) {
            statusLabel.setText("Giá hiện tại của " + item.getName() + ": " + String.format("%,.0f VND", currentHighestBid));
        }
        client.send(new AuctionMessage(MessageType.AUCTION_OPENED, item));
    }

    @FXML
    public void initialize() {
        nguoiDatCol.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        giaCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        thoiGianCol.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        auctionTable.setItems(bidHistory);

        series = new XYChart.Series<>();
        series.setName("Biến động giá");
        priceChart.getData().add(series);
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));

        statusLabel.setText("Giá khởi điểm là: VND " + String.format("%.0f", currentHighestBid));

        // [FIX 3] Khởi tạo label giá đặt của bản thân
        if (lblMyCurrentBid != null) {
            lblMyCurrentBid.setText("Chưa đặt giá");
        }

        startCountdown();
    }

    // [FIX 1+2] Kết nối tới server và lắng nghe message
    private void connectToServer() {
        try {
            client.connect(MessageType.REGISTER_BIDDER, this::handleServerMessage);
            System.out.println(">>> [CLIENT] Đã kết nối tới Auction Server");
        } catch (IOException e) {
            System.out.println(">>> [CLIENT] Không thể kết nối server: " + e.getMessage());
            // Nếu không có server, vẫn cho chạy offline
        }
    }

    // [FIX 1+2+3] Xử lý tất cả message nhận từ server
    private void handleServerMessage(AuctionMessage msg) {
        Platform.runLater(() -> {
            if (auctionItem != null && msg.getItemId() != null
                    && !auctionItem.getId().equals(msg.getItemId())) {
                return;
            }
            switch (msg.getType()) {

                // [FIX 1] Nhận snapshot lịch sử khi vừa vào phòng
                case SYNC_BID_HISTORY -> {
                    // Đồng bộ đồng hồ với server — luôn thực hiện kể cả khi chưa có bid nào
                    int serverRemaining = msg.getRemainingSeconds();
                    if (serverRemaining > 0) {
                        totalSeconds = serverRemaining;
                        updateClockDisplay();
                    }

                    if (msg.getBidList() != null && !msg.getBidList().isEmpty()) {
                        bidHistory.setAll(msg.getBidList());
                        currentHighestBid = msg.getWinnerAmount();
                        bidCount = msg.getBidList().size();
                        // Vẽ lại chart từ history (đảo ngược vì list lưu mới nhất đầu)
                        series.getData().clear();
                        int idx = 1;
                        for (int i = msg.getBidList().size() - 1; i >= 0; i--) {
                            series.getData().add(new XYChart.Data<>(idx++, msg.getBidList().get(i).getAmount()));
                        }
                        statusLabel.setText("Giá cao nhất hiện tại: " + String.format("%,.0f VND", currentHighestBid));
                        statusLabel.setStyle("-fx-text-fill: #1e3a8a;");
                    }
                }

                // [FIX 2] Nhận BID từ server (của bất kỳ ai, kể cả chính mình)
                case BID -> {
                    Bid incomingBid = msg.getBid();
                    if (incomingBid == null) return;

                    String bidderName = incomingBid.getBidderName();
                    double bidAmount = incomingBid.getAmount();

                    // [SYNC TIMER] Đồng bộ đồng hồ từ server sau mỗi lần có bid (bao gồm anti-sniping)
                    if (msg.getRemainingSeconds() > 0) {
                        totalSeconds = msg.getRemainingSeconds();
                        updateClockDisplay();
                    }

                    // Chỉ cập nhật nếu giá này cao hơn giá hiện tại (tránh duplicate)
                    if (bidAmount > currentHighestBid) {
                        currentHighestBid = bidAmount;
                        if (auctionItem != null) auctionItem.setCurrentPrice(bidAmount);
                        bidCount++;
                        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
                        bidHistory.add(0, incomingBid);

                        // Anti-sniping được server quản lý — không cần client tự cộng thêm giây

                        statusLabel.setText(bidderName + " đặt: " + String.format("%,.0f VND", bidAmount));
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");

                        // [FIX 2] Nếu bid này KHÔNG phải của mình → kiểm tra auto-bid
                        boolean isMyBid = currentUser != null && bidderName.equals(currentUser.getFullName());

                        // [FIX] Cập nhật "Giá bạn đang đặt" cho cả đặt tay lẫn auto-bid
                        if (isMyBid) {
                            myCurrentBid = bidAmount;
                            if (lblMyCurrentBid != null) {
                                lblMyCurrentBid.setText(String.format("%,.0f VNĐ", myCurrentBid));
                                lblMyCurrentBid.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            }
                        }

                        // Cả đặt tay lẫn auto-bid đều dùng tên thật → chỉ cần check isMyBid
                        if (!isMyBid) {
                            checkAndExecuteAutoBid(currentHighestBid);
                        }
                    }
                }

                // [FIX 3] Kết thúc phiên: thông báo người thắng và trừ tiền
                case AUCTION_ENDED -> {
                    if (timeline != null) timeline.stop();
                    bidInput.setDisable(true);
                    if (autoBidCheckBox != null) autoBidCheckBox.setDisable(true);
                    autoBidCheckBox.setSelected(false);

                    String winner = msg.getWinnerName();
                    double finalPrice = msg.getWinnerAmount();

                    boolean iAmWinner = currentUser != null && winner != null
                            && winner.equals(currentUser.getFullName());

                    if (iAmWinner) {
                        // Trừ số dư của người thắng
                        currentUser.setBalance(currentUser.getBalance() - finalPrice);
                        updateBalanceUI();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("🎉 Chúc mừng!");
                        alert.setHeaderText("Bạn đã giành được vật phẩm!");
                        alert.setContentText(String.format(
                                "Bạn đã thắng phiên đấu giá với mức giá: %,.0f VNĐ\n" +
                                "Số dư còn lại: %,.0f VNĐ\n\n" +
                                "Cảm ơn bạn đã tham gia!", finalPrice, currentUser.getBalance()));
                        alert.showAndWait();

                        statusLabel.setText("🏆 Bạn đã thắng! Giá: " + String.format("%,.0f VNĐ", finalPrice));
                        statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Phiên đấu giá kết thúc");
                        alert.setHeaderText("Phiên đấu giá đã kết thúc!");
                        alert.setContentText(winner != null && !winner.equals("Không có")
                                ? String.format("Người thắng: %s\nGiá cuối cùng: %,.0f VNĐ", winner, finalPrice)
                                : "Không có ai đặt giá.");
                        alert.showAndWait();

                        statusLabel.setText("Phiên kết thúc. Người thắng: " + winner);
                        statusLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }

                default -> { /* ignore */ }
            }
        });
    }

    private void startCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (totalSeconds > 0) {
                totalSeconds--;
                updateClockDisplay();
            } else {
                timeline.stop();
                bidInput.setDisable(true);
                // [FIX 3] Server phát AUCTION_ENDED — chỉ cần 1 client (người chủ phòng)
                // gọi broadcastAuctionEnded. Để đơn giản, broadcast ngay từ client đầu tiên hết giờ
                // bằng cách gửi 1 message đặc biệt lên server
                try {
                    client.send(new AuctionMessage(MessageType.AUCTION_ENDED,
                            auctionItem != null ? auctionItem.getId() : null,
                            "timer_end", 0.0, true));
                } catch (Exception ex) {
                    // Nếu offline, xử lý local
                    handleAuctionEndedLocal();
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    // Fallback xử lý kết thúc phiên offline (không có server)
    private void handleAuctionEndedLocal() {
        statusLabel.setText("Phiên đấu giá đã kết thúc!");
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        if (!bidHistory.isEmpty()) {
            Bid winningBid = bidHistory.get(0); // phần tử đầu là giá cao nhất
            String winnerName = winningBid.getBidderName();
            double winAmount = winningBid.getAmount();
            boolean iAmWinner = currentUser != null && winnerName.equals(currentUser.getFullName());
            if (iAmWinner) {
                currentUser.setBalance(currentUser.getBalance() - winAmount);
                updateBalanceUI();
                statusLabel.setText("🏆 Bạn đã thắng với giá " + String.format("%,.0f VNĐ", winAmount));
                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            }
        }
    }

    private void updateClockDisplay() {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        minutesLabel.setText(String.format("%02d", min));
        secondsLabel.setText(String.format("%02d", sec));
        String style = totalSeconds < 60
                ? "-fx-text-fill: #ff4757; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';"
                : "-fx-text-fill: #000a55; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
        minutesLabel.setStyle(style);
        secondsLabel.setStyle(style);
    }

    // [FIX 2] Auto-bid: gửi qua server thay vì chạy local
    private void checkAndExecuteAutoBid(double latestPrice) {
        if (!autoBidCheckBox.isSelected()) return;
        if (currentUser == null) return;

        try {
            double step = stepBidField.getText().isEmpty() ? 500.0 : Double.parseDouble(stepBidField.getText());
            double limit = maxBidField.getText().isEmpty()
                    ? currentUser.getBalance()
                    : Math.min(Double.parseDouble(maxBidField.getText()), currentUser.getBalance());

            if (latestPrice < limit) {
                double myNewBid = latestPrice + step;
                if (myNewBid <= limit && myNewBid <= currentUser.getBalance()) {
                    // [FIX] Dùng đúng Bidder object như đặt tay bình thường, không dùng tên "Hệ thống"
                    Timeline robotThinking = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> {
                        Bid autoBid = new Bid(currentUser, myNewBid);
                        AuctionMessage packet = new AuctionMessage(
                                MessageType.BID,
                                auctionItem != null ? auctionItem.getId() : null,
                                autoBid);
                        try {
                            client.send(packet);
                        } catch (Exception ex) {
                            // Fallback offline
                            handleLocalBid(currentUser.getFullName(), myNewBid);
                        }
                    }));
                    robotThinking.play();
                } else if (myNewBid > limit) {
                    // Vượt hạn mức → tắt auto-bid
                    autoBidCheckBox.setSelected(false);
                    statusLabel.setText("Auto-bid đã tắt: đã đạt hạn mức " + String.format("%,.0f VNĐ", limit));
                    statusLabel.setStyle("-fx-text-fill: #e67e22;");
                }
            } else {
                // Giá hiện tại đã vượt hạn mức → tắt auto-bid
                autoBidCheckBox.setSelected(false);
                statusLabel.setText("Auto-bid đã tắt: giá vượt hạn mức của bạn.");
                statusLabel.setStyle("-fx-text-fill: #e67e22;");
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi thông số Auto-Bid!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    // Fallback offline: cập nhật UI trực tiếp khi không có server
    private void handleLocalBid(String bidderName, double amount) {
        currentHighestBid = amount;
        if (auctionItem != null) auctionItem.setCurrentPrice(amount);
        bidCount++;
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        bidHistory.add(0, new Bid(bidderName, amount));
        statusLabel.setText(bidderName + " đặt: " + String.format("%,.0f VND", amount));
        statusLabel.setStyle("-fx-text-fill: #27ae60;");
    }

    @FXML
    private void handleHelpClick(MouseEvent event) {
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();
        if (autoBidTooltip.isShowing()) {
            autoBidTooltip.hide();
        } else {
            autoBidTooltip.show(window, event.getScreenX(), event.getScreenY() + 10);
        }
    }

    @FXML
    public void handlePlaceBid(ActionEvent event) {
        String input = bidInput.getText();
        if (input == null || input.trim().isEmpty()) {
            statusLabel.setText("Vui lòng nhập số tiền!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        try {
            double newBid = Double.parseDouble(input.trim());

            User user = AuthService.getInstance().getCurrentUser();
            if (!(user instanceof Bidder bidder)) {
                statusLabel.setText("Không xác định được người dùng hợp lệ!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            if (newBid <= currentHighestBid) {
                statusLabel.setText("Giá phải cao hơn " + String.format("%,.0f VNĐ", currentHighestBid));
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            if (newBid > bidder.getBalance()) {
                statusLabel.setText("Số dư không đủ! Bạn chỉ có: " + String.format("%,.0f VNĐ", bidder.getBalance()));
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận đặt giá");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn có chắc muốn đặt mức giá " + String.format("%,.0f VNĐ", newBid) + " không?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Bid bidData = new Bid(bidder, newBid);
                AuctionMessage packet = new AuctionMessage(
                        MessageType.BID,
                        auctionItem != null ? auctionItem.getId() : null,
                        bidData);

                try {
                    // [FIX 2] Gọi đúng instance method thay vì static
                    client.send(packet);
                    // lblMyCurrentBid sẽ tự cập nhật khi nhận BID broadcast từ server
                    statusLabel.setText("Đã gửi lệnh đặt giá thành công!");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                    bidInput.clear();
                    bidInput.requestFocus();
                } catch (Exception e) {
                    // Fallback offline
                    handleLocalBid(bidder.getFullName(), newBid);
                    bidInput.clear();
                }
            } else {
                statusLabel.setText("Đã hủy lượt đặt giá.");
                statusLabel.setStyle("-fx-text-fill: #e67e22;");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Vui lòng nhập một con số hợp lệ!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void handleBackToDashboard(ActionEvent event) {
        if (timeline != null) timeline.stop();
        client.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();
            BidderDashboardController dashboardController = loader.getController();
            if (currentUser != null) {
                dashboardController.setLblUsername(currentUser.getUsername());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang chủ - Sàn đấu giá");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
