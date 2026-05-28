package com.auction.controller;

import com.auction.model.core.DepositRequest;
import com.auction.model.core.DepositStatus;
import com.auction.model.core.AuctionSummary;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.users.Bidder;
import com.auction.model.users.User;
import com.auction.network.AuctionClient;
import com.auction.network.AuctionMessage;
import com.auction.network.MessageType;
import com.auction.utils.DatabaseConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private Label lblAdminName;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblPending;
    @FXML private Label lblRevenue;

    @FXML private VBox paneOverview;
    @FXML private VBox paneUsers;
    @FXML private VBox paneProducts;
    @FXML private VBox paneDeposits;
    @FXML private VBox paneAuctions;

    @FXML private Button btnOverview;
    @FXML private Button btnUsers;
    @FXML private Button btnProducts;
    @FXML private Button btnDeposits;
    @FXML private Button btnAuctions;

    // ── User table ──
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUserName;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserBalance;
    @FXML private TableColumn<User, String> colUserStatus;

    // ── Product table ──
    @FXML private TableView<Item> tableProducts;
    @FXML private TableColumn<Item, String> colProductName;
    @FXML private TableColumn<Item, String> colProductCategory;
    @FXML private TableColumn<Item, Double> colProductPrice;
    @FXML private TableColumn<Item, String> colProductSeller;
    @FXML private TableColumn<Item, String> colProductStatus;

    // ── Deposit table ──
    @FXML private TableView<DepositRequest> tableDeposits;
    @FXML private TableColumn<DepositRequest, String> colDepositUser;
    @FXML private TableColumn<DepositRequest, String> colDepositAmount;
    @FXML private TableColumn<DepositRequest, String> colDepositTime;
    @FXML private TableColumn<DepositRequest, String> colDepositStatus;

    // ── Auction table ──
    @FXML private TableView<AuctionSummary> tableAuctions;
    @FXML private TableColumn<AuctionSummary, String> colAuctionProduct;
    @FXML private TableColumn<AuctionSummary, String> colAuctionPrice;
    @FXML private TableColumn<AuctionSummary, String> colAuctionLeader;
    @FXML private TableColumn<AuctionSummary, String> colAuctionTime;
    @FXML private TableColumn<AuctionSummary, String> colAuctionStatus;

    @FXML private VBox recentActivityBox;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<Item> pendingItems = FXCollections.observableArrayList();
    private final ObservableList<DepositRequest> pendingDeposits = FXCollections.observableArrayList();
    private final ObservableList<AuctionSummary> auctionList = FXCollections.observableArrayList();
    private AuctionClient auctionClient;

    @FXML
    public void initialize() {
        pendingItems.addAll(DatabaseConnection.getInstance().getItemTable().stream()
                .filter(item -> item.getStatus() == ItemStatus.PENDING)
                .toList());
        pendingDeposits.addAll(DatabaseConnection.getInstance().getDepositRequestTable().stream()
                .filter(request -> request.getStatus() == DepositStatus.PENDING)
                .toList());

        setupUserTable();
        setupProductTable();
        setupDepositTable();
        setupAuctionTable();
        updateOverview();
    }

    public void setAdminName(String name) {
        if (auctionClient == null) {
            connectSocket();
        }
        lblAdminName.setText("Admin " + name.toUpperCase());
        updateOverview();
    }

    // ─────────────────────────────────────────────
    //  SETUP TABLES
    // ─────────────────────────────────────────────
    private void setupUserTable() {
        if (tableUsers == null) return;

        userList.addAll(DatabaseConnection.getInstance().getUserTable());

        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colUserRole.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClass().getSimpleName())
        );

        colUserBalance.setCellValueFactory(data -> {
            User user = data.getValue();
            if (user instanceof Bidder b) {
                return new SimpleStringProperty(String.format("%,.0f VNĐ", b.getBalance()));
            }
            return new SimpleStringProperty("N/A");
        });

        colUserStatus.setCellValueFactory(data ->
                new SimpleStringProperty("Hoạt động")
        );

        tableUsers.setItems(userList);
    }

    private void setupProductTable() {
        if (tableProducts == null) return;
        colProductName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProductCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colProductPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colProductSeller.setCellValueFactory(new PropertyValueFactory<>("sellerName"));
        colProductStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        tableProducts.setItems(pendingItems);
    }

    private void setupDepositTable() {
        if (tableDeposits == null) return;
        colDepositUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDepositAmount.setCellValueFactory(new PropertyValueFactory<>("amountText"));
        colDepositTime.setCellValueFactory(new PropertyValueFactory<>("requestTimeText"));
        colDepositStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        tableDeposits.setItems(pendingDeposits);
    }

    private void setupAuctionTable() {
        if (tableAuctions == null) return;
        colAuctionProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colAuctionPrice.setCellValueFactory(new PropertyValueFactory<>("currentPriceText"));
        colAuctionLeader.setCellValueFactory(new PropertyValueFactory<>("leaderName"));
        colAuctionTime.setCellValueFactory(new PropertyValueFactory<>("remainingTimeText"));
        colAuctionStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableAuctions.setItems(auctionList);
    }

    // ─────────────────────────────────────────────
    //  SOCKET
    // ─────────────────────────────────────────────
    private void connectSocket() {
        auctionClient = new AuctionClient();
        try {
            auctionClient.connect(true, this::handleSocketMessage);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Socket error",
                    "Admin không kết nối được server: " + e.getMessage());
        }
    }

    private void handleSocketMessage(AuctionMessage message) {
        if (message == null || message.getType() == null) return;

        Platform.runLater(() -> {
            if (message.getType() == MessageType.ITEM_PENDING && message.getItem() != null) {
                pendingItems.removeIf(item -> item.getId().equals(message.getItem().getId()));
                pendingItems.add(0, message.getItem());
            } else if ((message.getType() == MessageType.ITEM_APPROVED
                    || message.getType() == MessageType.ITEM_REJECTED)
                    && message.getItem() != null) {
                pendingItems.removeIf(item -> item.getId().equals(message.getItemId()));
            } else if (message.getType() == MessageType.DEPOSIT_PENDING
                    && message.getDepositRequest() != null) {
                pendingDeposits.removeIf(r -> r.getId().equals(message.getDepositRequest().getId()));
                pendingDeposits.add(0, message.getDepositRequest());
            } else if ((message.getType() == MessageType.DEPOSIT_APPROVED
                    || message.getType() == MessageType.DEPOSIT_REJECTED)
                    && message.getDepositRequest() != null) {
                pendingDeposits.removeIf(r -> r.getId().equals(message.getDepositRequest().getId()));
            } else if (message.getType() == MessageType.SYNC_AUCTIONS
                    && message.getAuctionSummaries() != null) {
                auctionList.setAll(message.getAuctionSummaries());
            } else if ((message.getType() == MessageType.AUCTION_OPENED
                    || message.getType() == MessageType.AUCTION_STOPPED)
                    && message.getAuctionSummary() != null) {
                upsertAuction(message.getAuctionSummary());
            } else if (message.getType() == MessageType.ERROR) {
                showAlert(Alert.AlertType.ERROR, "Socket error", message.getMessage());
            }
            updateOverview();
        });
    }

    private void upsertAuction(AuctionSummary summary) {
        auctionList.removeIf(existing -> existing.getItemId().equals(summary.getItemId()));
        auctionList.add(0, summary);
    }

    // ─────────────────────────────────────────────
    //  OVERVIEW
    // ─────────────────────────────────────────────
    private void updateOverview() {
        var db = DatabaseConnection.getInstance();
        long approvedProducts = db.getItemTable().stream()
                .filter(item -> item.getStatus() == ItemStatus.APPROVED)
                .count();
        long approvedDepositTotal = db.getDepositRequestTable().stream()
                .filter(r -> r.getStatus() == DepositStatus.APPROVED)
                .mapToLong(DepositRequest::getAmount)
                .sum();

        lblTotalUsers.setText(String.valueOf(db.getUserTable().size()));
        lblTotalProducts.setText(String.valueOf(approvedProducts));
        lblPending.setText(String.valueOf(pendingItems.size() + pendingDeposits.size()));
        lblRevenue.setText(String.format("%,d VNĐ", approvedDepositTotal));
        renderRecentActivity();
    }

    private void renderRecentActivity() {
        if (recentActivityBox == null) return;
        recentActivityBox.getChildren().clear();

        for (DepositRequest request : pendingDeposits.stream().limit(3).toList()) {
            Label label = new Label(String.format("• %s yêu cầu nạp %,d VNĐ đang chờ duyệt",
                    request.getUsername(), request.getAmount()));
            label.setStyle("-fx-text-fill: #e67e22;");
            recentActivityBox.getChildren().add(label);
        }
        for (Item item : pendingItems.stream().limit(3).toList()) {
            Label label = new Label("• Sản phẩm \"" + item.getName() + "\" đang chờ duyệt");
            label.setStyle("-fx-text-fill: #555;");
            recentActivityBox.getChildren().add(label);
        }
        if (recentActivityBox.getChildren().isEmpty()) {
            Label empty = new Label("• Chưa có yêu cầu nào đang chờ xử lý");
            empty.setStyle("-fx-text-fill: #7f8c8d;");
            recentActivityBox.getChildren().add(empty);
        }
    }

    // ─────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────
    private void showPane(VBox pane, Button activeBtn) {
        paneOverview.setVisible(false); paneOverview.setManaged(false);
        paneUsers.setVisible(false);    paneUsers.setManaged(false);
        paneProducts.setVisible(false); paneProducts.setManaged(false);
        paneDeposits.setVisible(false); paneDeposits.setManaged(false);
        paneAuctions.setVisible(false); paneAuctions.setManaged(false);

        pane.setVisible(true); pane.setManaged(true);

        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-font-size: 13; -fx-padding: 12 15; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #1976D2; "
                + "-fx-font-weight: bold; -fx-padding: 12 15; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";

        btnOverview.setStyle(defaultStyle);
        btnUsers.setStyle(defaultStyle);
        btnProducts.setStyle(defaultStyle);
        btnDeposits.setStyle(defaultStyle);
        btnAuctions.setStyle(defaultStyle);
        activeBtn.setStyle(activeStyle);
    }

    @FXML public void showOverview(ActionEvent e) { showPane(paneOverview, btnOverview); }
    @FXML public void showUsers(ActionEvent e)    { showPane(paneUsers, btnUsers); }
    @FXML public void showProducts(ActionEvent e) { showPane(paneProducts, btnProducts); }
    @FXML public void showDeposits(ActionEvent e) { showPane(paneDeposits, btnDeposits); }
    @FXML public void showAuctions(ActionEvent e) { showPane(paneAuctions, btnAuctions); }

    // ─────────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────────
    @FXML
    public void handleApproveProduct(ActionEvent event) {
        Item selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để duyệt!");
            return;
        }
        if (auctionClient != null)
            auctionClient.send(new AuctionMessage(MessageType.APPROVE_ITEM, selected.getId(), true));
        pendingItems.remove(selected);
        updateOverview();
        showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Sản phẩm đã được duyệt và đưa lên sàn!");
    }

    @FXML
    public void handleRejectProduct(ActionEvent event) {
        Item selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để từ chối!");
            return;
        }
        if (auctionClient != null)
            auctionClient.send(new AuctionMessage(MessageType.REJECT_ITEM, selected.getId(), true));
        pendingItems.remove(selected);
        updateOverview();
        showAlert(Alert.AlertType.INFORMATION, "Đã từ chối", "Sản phẩm đã bị từ chối!");
    }

    @FXML
    public void handleLockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn user để khóa!");
            return;
        }
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        showAlert(Alert.AlertType.INFORMATION, "Đã khóa",
                "Tài khoản \"" + selected.getUsername() + "\" đã bị khóa!");
    }

    @FXML
    public void handleUnlockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn user để mở khóa!");
            return;
        }
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        showAlert(Alert.AlertType.INFORMATION, "Đã mở khóa",
                "Tài khoản \"" + selected.getUsername() + "\" đã được mở khóa!");
    }

    @FXML
    public void handleApproveDeposit(ActionEvent event) {
        DepositRequest selected = tableDeposits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn yêu cầu để duyệt!");
            return;
        }
        if (auctionClient != null)
            auctionClient.send(new AuctionMessage(MessageType.APPROVE_DEPOSIT, selected.getId(), false));
        selected.setStatus(DepositStatus.APPROVED);
        pendingDeposits.remove(selected);
        updateOverview();
        showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Yêu cầu nạp tiền đã được duyệt!");
    }

    @FXML
    public void handleRejectDeposit(ActionEvent event) {
        DepositRequest selected = tableDeposits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn yêu cầu để từ chối!");
            return;
        }
        if (auctionClient != null)
            auctionClient.send(new AuctionMessage(MessageType.REJECT_DEPOSIT, selected.getId(), false));
        selected.setStatus(DepositStatus.REJECTED);
        pendingDeposits.remove(selected);
        updateOverview();
        showAlert(Alert.AlertType.INFORMATION, "Đã từ chối", "Yêu cầu nạp tiền đã bị từ chối!");
    }

    @FXML
    public void handleStopAuction(ActionEvent event) {
        if (tableAuctions.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiên đấu giá để dừng!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận dừng");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn dừng phiên đấu giá này không?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuctionSummary selected = tableAuctions.getSelectionModel().getSelectedItem();
                if (auctionClient != null && selected != null) {
                    auctionClient.send(new AuctionMessage(MessageType.AUCTION_STOPPED, selected.getItemId(), true));
                }
                showAlert(Alert.AlertType.INFORMATION, "Đã dừng",
                        "Phiên đấu giá đã được dừng thủ công!");
            }
        });
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (auctionClient != null) auctionClient.close();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Đăng nhập hệ thống");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
