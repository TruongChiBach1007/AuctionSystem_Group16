package com.auction.controller;

import com.auction.model.items.Item;
import com.auction.network.AuctionClient;
import com.auction.network.AuctionMessage;
import com.auction.network.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @FXML private TableView tableUsers;
    @FXML private TableView<Item> tableProducts;
    @FXML private TableColumn<Item, String> colProductName;
    @FXML private TableColumn<Item, String> colProductCategory;
    @FXML private TableColumn<Item, Double> colProductPrice;
    @FXML private TableColumn<Item, String> colProductSeller;
    @FXML private TableColumn<Item, String> colProductStatus;
    @FXML private TableView tableDeposits;
    @FXML private TableView tableAuctions;

    private final ObservableList<Item> pendingItems = FXCollections.observableArrayList();
    private AuctionClient auctionClient;

    @FXML
    public void initialize() {
        lblTotalUsers.setText("12");
        lblTotalProducts.setText("5");
        lblPending.setText("0");
        lblRevenue.setText("150,000,000 VND");
        setupProductTable();
    }

    public void setAdminName(String name) {
        if (auctionClient == null) {
            connectSocket();
        }
        lblAdminName.setText("Admin " + name.toUpperCase());
        updatePendingCount();
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

    private void connectSocket() {
        auctionClient = new AuctionClient();
        try {
            auctionClient.connect(true, this::handleSocketMessage);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Socket error", "Admin khong ket noi duoc server: " + e.getMessage());
        }
    }

    private void handleSocketMessage(AuctionMessage message) {
        if (message == null || message.getType() == null) return;

        Platform.runLater(() -> {
            if (message.getType() == MessageType.ITEM_PENDING && message.getItem() != null) {
                pendingItems.add(0, message.getItem());
            } else if ((message.getType() == MessageType.ITEM_APPROVED || message.getType() == MessageType.ITEM_REJECTED)
                    && message.getItem() != null) {
                pendingItems.removeIf(item -> item.getId().equals(message.getItemId()));
            } else if (message.getType() == MessageType.ERROR) {
                showAlert(Alert.AlertType.ERROR, "Socket error", message.getMessage());
            }
            updatePendingCount();
        });
    }

    private void updatePendingCount() {
        lblPending.setText(String.valueOf(pendingItems.size()));
    }

    private void showPane(VBox pane, Button activeBtn) {
        paneOverview.setVisible(false);
        paneUsers.setVisible(false);
        paneProducts.setVisible(false);
        paneDeposits.setVisible(false);
        paneAuctions.setVisible(false);
        pane.setVisible(true);

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
    @FXML public void showUsers(ActionEvent e) { showPane(paneUsers, btnUsers); }
    @FXML public void showProducts(ActionEvent e) { showPane(paneProducts, btnProducts); }
    @FXML public void showDeposits(ActionEvent e) { showPane(paneDeposits, btnDeposits); }
    @FXML public void showAuctions(ActionEvent e) { showPane(paneAuctions, btnAuctions); }

    @FXML
    public void handleApproveProduct(ActionEvent event) {
        Item selectedItem = tableProducts.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon san pham de duyet!");
            return;
        }
        auctionClient.send(new AuctionMessage(MessageType.APPROVE_ITEM, selectedItem.getId(), true));
        pendingItems.remove(selectedItem);
        updatePendingCount();
        showAlert(Alert.AlertType.INFORMATION, "Da duyet", "San pham da duoc duyet va dua len san!");
    }

    @FXML
    public void handleRejectProduct(ActionEvent event) {
        Item selectedItem = tableProducts.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon san pham de tu choi!");
            return;
        }
        auctionClient.send(new AuctionMessage(MessageType.REJECT_ITEM, selectedItem.getId(), true));
        pendingItems.remove(selectedItem);
        updatePendingCount();
        showAlert(Alert.AlertType.INFORMATION, "Da tu choi", "San pham da bi tu choi!");
    }

    @FXML
    public void handleLockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon user de khoa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Da khoa", "Tai khoan da bi khoa thanh cong!");
    }

    @FXML
    public void handleUnlockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon user de mo khoa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Da mo khoa", "Tai khoan da duoc mo khoa!");
    }

    @FXML
    public void handleApproveDeposit(ActionEvent event) {
        if (tableDeposits.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon yeu cau de duyet!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Da duyet", "Yeu cau nap tien da duoc duyet!");
    }

    @FXML
    public void handleRejectDeposit(ActionEvent event) {
        if (tableDeposits.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon yeu cau de tu choi!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Da tu choi", "Yeu cau nap tien da bi tu choi!");
    }

    @FXML
    public void handleStopAuction(ActionEvent event) {
        if (tableAuctions.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon phien dau gia de dung!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xac nhan dung");
        confirm.setHeaderText(null);
        confirm.setContentText("Ban co chac muon dung phien dau gia nay khong?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Da dung", "Phien dau gia da duoc dung thu cong!");
            }
        });
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (auctionClient != null) auctionClient.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Dang nhap he thong");
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
