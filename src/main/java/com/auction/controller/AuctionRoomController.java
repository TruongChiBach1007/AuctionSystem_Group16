package com.auction.controller;

import com.auction.model.core.Bid;
import com.auction.model.items.Item;
import com.auction.model.users.Bidder;
import com.auction.model.users.User;
import com.auction.security.AuthService;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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

    private final ObservableList<Bid> bidHistory = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> series;
    private int bidCount = 0;
    private double currentHighestBid = 10000;
    private int totalSeconds = 600;
    private Timeline timeline;
    private Bidder currentUser;
    private Item auctionItem;

    public void setCurrentUser(String username) {
        User loggedInUser = AuthService.getInstance().getCurrentUser();
        if (loggedInUser instanceof Bidder bidder) {
            this.currentUser = bidder;
        } else {
            this.currentUser = new Bidder(1, username, "pass", username, username + "@gmail.com", 500000.0);
        }
        if (statusLabel != null) {
            statusLabel.setText("Chao mung " + currentUser.getUsername() + "! San sang dau gia.");
        }
    }

    public void setAuctionItem(Item item) {
        this.auctionItem = item;
        this.currentHighestBid = item.getCurrentPrice();
        if (series != null) {
            series.setName("Bien dong gia " + item.getName());
            series.getData().clear();
            bidCount = 0;
            series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        }
        if (statusLabel != null) {
            statusLabel.setText("Gia hien tai cua " + item.getName() + ": " + String.format("%,.0f VND", currentHighestBid));
        }
    }

    @FXML
    public void initialize() {
        nguoiDatCol.setCellValueFactory(new PropertyValueFactory<>("bidderName"));
        giaCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        thoiGianCol.setCellValueFactory(new PropertyValueFactory<>("bidTime"));
        auctionTable.setItems(bidHistory);

        series = new XYChart.Series<>();
        series.setName("Bien dong gia iPhone 15 Pro Max");
        priceChart.getData().add(series);
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));

        statusLabel.setText("Gia khoi diem la: VND " + String.format("%.0f", currentHighestBid));
        startCountdown();
    }

    private void startCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (totalSeconds > 0) {
                totalSeconds--;
                updateClockDisplay();
            } else {
                timeline.stop();
                statusLabel.setText("Phien dau gia da ket thuc!");
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

        String style = totalSeconds < 60
                ? "-fx-text-fill: #ff4757; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';"
                : "-fx-text-fill: #000a55; -fx-font-size: 24; -fx-font-weight: bold; -fx-font-family: 'Arial Black';";
        minutesLabel.setStyle(style);
        secondsLabel.setStyle(style);
    }

    private void executeBidLogic(double newBid, String bidderName) {
        currentHighestBid = newBid;
        if (auctionItem != null) {
            auctionItem.setCurrentPrice(newBid);
        }
        bidCount++;
        series.getData().add(new XYChart.Data<>(bidCount, currentHighestBid));
        bidHistory.add(0, new Bid(bidderName, newBid));

        if (totalSeconds < 10) {
            totalSeconds += 15;
            updateClockDisplay();
        }

        statusLabel.setText(bidderName + " dat gia thanh cong: " + String.format("%.0f", newBid) + " VND");
        statusLabel.setStyle("-fx-text-fill: #27ae60;");

        if (!bidderName.equals("He thong (Auto)")) {
            checkAndExecuteAutoBid(currentHighestBid);
        }
    }

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
                if (myNewBid <= limit) {
                    Timeline robotThinking = new Timeline(new KeyFrame(Duration.seconds(1.5), ev ->
                            executeBidLogic(myNewBid, "He thong (Auto)")
                    ));
                    robotThinking.play();
                }
            }
        } catch (Exception e) {
            statusLabel.setText("Loi thong so Auto-Bid!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
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
            statusLabel.setText("Vui long nhap so tien!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            double newBid = Double.parseDouble(input.trim());
            if (currentUser == null) {
                statusLabel.setText("Khong xac dinh duoc nguoi dung hien tai!");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            if (newBid <= currentHighestBid) {
                statusLabel.setText("Gia phai cao hon VND " + String.format("%.0f", currentHighestBid));
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            if (currentUser != null && newBid > currentUser.getBalance()) {
                statusLabel.setText("So du khong du! Ban co: " + String.format("%.0f", currentUser.getBalance()));
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xac nhan dat gia");
            confirm.setHeaderText(null);
            confirm.setContentText("Ban co chac chan muon dat gia " + String.format("%.0f", newBid) + " VND khong?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                statusLabel.setText("Da huy dat gia.");
                statusLabel.setStyle("-fx-text-fill: #e67e22;");
                return;
            }

            executeBidLogic(newBid, currentUser.getUsername());
            bidInput.clear();
            bidInput.requestFocus();
        } catch (NumberFormatException e) {
            statusLabel.setText("Vui long nhap so hop le!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    @FXML
    public void handleBackToDashboard(ActionEvent event) {
        if (timeline != null) timeline.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            BidderDashboardController dashboardController = loader.getController();
            if (currentUser != null) {
                dashboardController.setLblUsername(currentUser.getUsername());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chu - San Dau Gia");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
