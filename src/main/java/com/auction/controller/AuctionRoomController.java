package com.auction.controller;

import com.auction.model.core.Bid;
import com.auction.model.users.Bidder;
import com.auction.network.GUIClientManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
    public static GUIClientManager networkManager;
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
    @FXML public Label balanceLabel;

    private ObservableList<Bid> bidHistory = FXCollections.observableArrayList();
    private long balance;

    // --- Khai báo các thành phần giao diện trùng khớp với FXML ---

    // Dữ liệu của biểu đồ
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0; // Đếm số lượt đặt giá
    private double currentHighestBid = 10000; // Giá khởi điểm
    private int totalSeconds = 600;
    private Timeline timeline;
    public Bidder currentUser;

    public void setCurrentUser(String username) {
        // Khởi tạo user với tên từ màn hình Login/Dashboard
        this.currentUser = new Bidder(1, username, "pass", username, username + "@gmail.com", 500000.0);
        if (statusLabel != null) {
            statusLabel.setText("Chào mừng " + username + "! Sẵn sàng đấu giá.");
        }
    }

    // --- CẬU THÊM HÀM NÀY VÀO FILE AuctionRoomController.java ---
    public void setBalance(long sharedBalance) {
        // 1. Gán số tiền nhận được vào biến balance vừa tạo ở trên
        this.balance = sharedBalance;

        // 2. Cập nhật chữ cho đúng cái nhãn 'balanceLabel' ở dòng 45 của cậu
        if (balanceLabel != null) {
            balanceLabel.setText(String.format("%,d VNĐ", sharedBalance));
        }
    }


    // Robot của autobid
    private boolean isAutoBidActive = false;
    private double maxAutoBidLimit = 0;

    // Hàm này chạy ngay khi màn hình vừa bật lên
    @FXML
    public void initialize() {
        //tableview
        nguoiDatCol.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        giaCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        thoiGianCol.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        auctionTable.setItems(bidHistory);

        //linechart
        series = new XYChart.Series<>();
        series.setName("Biến động giá iPhone 15 Pro Max");
        priceChart.getData().add(series);

        // Nạp thử 1 điểm giá khởi điểm vào biểu đồ
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        statusLabel.setText("Giá khởi điểm là: VND" + currentHighestBid);

        //Hàm đê ngược
        startCountdown();


        // === PHẦN CẬP NHẬT CỦA THÀNH VIÊN 3 ĐỂ PHÙ HỢP VỚI NETWORK MỚI ===
        if (networkManager == null) {
            // 1. Lấy instance duy nhất (Singleton) thay vì dùng 'new'
            networkManager = GUIClientManager.getInstance();

            // 2. Đăng ký controller này với Network
            networkManager.setController(this);

            // 3. Khởi động kết nối với IP và Port (Ví dụ localhost, 1234)
            networkManager.startConnection("localhost", 1234);
        } else {
            // Nếu đã có rồi thì chỉ cần cập nhật controller mới cho nó thôi
            networkManager.setController(this);
        }
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

        if (totalSeconds < 60) {
            String redStyle = "-fx-text-fill: #ff4757; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
            minutesLabel.setStyle(redStyle);
            secondsLabel.setStyle(redStyle);
            //Trả lại màu trắng khi gia hạn tgian
        }else{
            String whiteStyle = "-fx-text-fill: #000a55; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
            minutesLabel.setStyle(whiteStyle);
            secondsLabel.setStyle(whiteStyle);
        }
    }
    // [MỚI] Hàm bổ trợ để Robot và Người dùng dùng chung logic đặt giá
    private void executeBidLogic(double newBid, String bidderName) {
        if (currentUser != null && bidderName.equals(currentUser.getUsername())) {
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
            statusLabel.setText("✅ " + bidderName + " đặt giá thành công: " + newBid+"VND");
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
                double limit = maxBidField.getText().isEmpty() ? currentUser.getBalance() :
                        Math.min(Double.parseDouble(maxBidField.getText()), currentUser.getBalance());

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
            //executeBidLogic(newBid, currentUser.getUsername());
            networkManager.sendBid(new Bid(currentUser, newBid));

            bidInput.clear();
            bidInput.requestFocus();

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Vui lòng nhập số hợp lệ!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }catch (java.io.IOException e) { // THÊM KHỐI NÀY VÀO
            statusLabel.setText("X Lỗi kết nối Server!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    // --- Xử lý nút QUAY LẠI ---
    @FXML
    public void handleBackToDashboard(ActionEvent event) {
        if (timeline != null) timeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            // 💡 BƯỚC QUAN TRỌNG ĐÂY MINH ƠI:
            // Lấy controller của Dashboard vừa mới load lên
            BidderDashboardController dashboardController = loader.getController();

            // Truyền lại cái tên từ AuctionRoom quay ngược về Dashboard
            // Dùng chính cái currentUser mà em đã có trong AuctionRoom
            if (currentUser != null) {
                dashboardController.setLblUsername(currentUser.getUsername());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chủ - Sàn Đấu Giá");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateUIWithNewBid(Bid bid) {
        javafx.application.Platform.runLater(() -> {
            // Kiểm tra xem tin nhắn này đã có trong bảng chưa để tránh trùng lặp
            // (Nếu Server của bạn đã chuẩn broadcast thì dòng này để cho chắc)
            if (!auctionTable.getItems().contains(bid)) {
                auctionTable.getItems().add(0, bid); // Thêm lên đầu bảng

                // Cập nhật biểu đồ
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(bidCount++, bid.getAmount()));

                // Thông báo
                statusLabel.setText("✓ " + bid.getBidderName() + " vừa đặt giá: " + bid.getAmount());
            }
        });
    }
    // THÊM MỚI HÀM NÀY VÀO CUỐI CLASS
    public void updateBalanceUI(double newBalance) {
        javafx.application.Platform.runLater(() -> {
            if (balanceLabel != null) {
                balanceLabel.setText(String.format("%,.0f", newBalance)); // Format số cho đẹp (100,000)
            }
            if (currentUser != null) {
                currentUser.setBalance(newBalance);
            }
        });
    }
    public void handleIncomingBid(Bid bid) {
        try {
            System.out.println(">>> [DEBUG UI] Hàm handleIncomingBid được gọi! Giá nhận về: " + bid.getAmount());
            // 1. Thêm lượt đặt giá vào danh sách lịch sử để bảng tự nhảy dòng
            if (bidHistory != null) {
                bidHistory.add(bid);
            }

            // Ép bảng cập nhật lại dữ liệu từ list cho chắc chắn
            if (auctionTable != null) {
                auctionTable.refresh();
            }

            // 2. Vẽ điểm giá mới lên biểu đồ LineChart
            if (series != null) {
                bidCount++; // Tăng số lượt đặt giá làm trục hoành X
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(bidCount, bid.getAmount()));
                System.out.println(">>> [UI] Da cap nhat bieu do luot dat thu: " + bidCount);
            }

            // ================= 🤖 LOGIC TĂNG GIÁ TỰ ĐỘNG =================
            if (autoBidCheckBox != null && autoBidCheckBox.isSelected()) {

                // Kiểm tra an toàn xem đối tượng user của mình và người đặt mới có tồn tại không
                if (this.currentUser != null && bid.getBidder() != null) {

                    // Lấy tên người vừa đặt cao nhất và tên của chính cậu ra để đối chiếu
                    String lastBidderName = bid.getBidder().getUsername();
                    String myName = this.currentUser.getUsername(); // SỬA TẠI ĐÂY: Dùng trực tiếp currentUser của Bách

                    // Chỉ tự động tăng giá nếu ĐÓ LÀ NGƯỜI KHÁC ĐẶT (Không tự nâng giá đấu với chính mình)
                    if (!myName.equals(lastBidderName)) {

                        // Đọc hạn mức và bước nhảy từ giao diện
                        long maxBid = maxBidField.getText().isEmpty() ? 0 : Long.parseLong(maxBidField.getText().trim());
                        long stepBid = stepBidField.getText().isEmpty() ? 0 : Long.parseLong(stepBidField.getText().trim());

                        if (maxBid > 0 && stepBid > 0) {
                            // Mức giá tự động mới = Giá vừa nhận + Bước nhảy mong muốn
                            long nextBidAmount = (long) bid.getAmount() + stepBid;

                            // Kiểm tra điều kiện ngắt (Không vượt quá hạn mức VÀ không vượt quá tiền trong ví)
                            if (nextBidAmount <= maxBid && nextBidAmount <= this.balance) {

                                if (bidInput != null) {
                                    // Tự điền con số mới vào ô nhập giá hộ cậu
                                    bidInput.setText(String.valueOf(nextBidAmount));

                                    System.out.println(">>> [AUTO-BID] Dang tu dong kich hoat ra gia: " + nextBidAmount);

                                    // SỬA TẠI ĐÂY: Truyền 'null' vào ngoặc để thỏa mãn tham số ActionEvent của hàm gốc
                                    handlePlaceBid(null);
                                }
                            } else {
                                // Tự động nhả tích ô vuông khi chạm điểm dừng (Hết tiền hoặc quá hạn mức)
                                System.out.println(">>> [AUTO-BID] Tu dong tat vi vuot han muc hoac vi khong du tien!");
                                autoBidCheckBox.setSelected(false);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Loi khi ve giao dien dat gia: " + e.getMessage());
            e.printStackTrace();
        }
    }
// ==================================================================
}