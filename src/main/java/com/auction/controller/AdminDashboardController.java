package com.auction.controller;

import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.auction.dao.IItemDAO;
import com.auction.dao.ItemDAOImpl;

import java.io.IOException;

public class AdminDashboardController {
    private final IItemDAO itemDAO = new ItemDAOImpl();

    @FXML private Label lblAdminName;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblPending;
    @FXML private Label lblRevenue;

    // Các pane tab
    @FXML private VBox paneOverview;
    @FXML private VBox paneUsers;
    @FXML private VBox paneProducts;
    @FXML private VBox paneDeposits;
    @FXML private VBox paneAuctions;

    // Sidebar buttons
    @FXML private Button btnOverview;
    @FXML private Button btnUsers;
    @FXML private Button btnProducts;
    @FXML private Button btnDeposits;
    @FXML private Button btnAuctions;

    // Tables
    @FXML private TableView tableUsers;
    @FXML private TableView tableProducts;
    @FXML private TableView tableDeposits;
    @FXML private TableView tableAuctions;

    @FXML
    public void initialize() {
        // Load dữ liệu tổng quan tạm thời
        lblTotalUsers.setText("12");
        lblTotalProducts.setText("5");
        lblPending.setText("3");
        lblRevenue.setText("150,000,000 VNĐ");
    }

    public void setAdminName(String name) {
        lblAdminName.setText("👤 " + name.toUpperCase());
    }

    // Chuyển tab — ẩn hết rồi hiện cái được chọn
    private void showPane(VBox pane, Button activeBtn) {
        paneOverview.setVisible(false);
        paneUsers.setVisible(false);
        paneProducts.setVisible(false);
        paneDeposits.setVisible(false);
        paneAuctions.setVisible(false);
        pane.setVisible(true);

        // Reset style sidebar
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 13; -fx-padding: 12 15; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #1976D2; " +
                "-fx-font-weight: bold; -fx-padding: 12 15; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";

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

    // --- Quản lý Users ---
    @FXML
    public void handleLockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn user để khóa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã khóa", "Tài khoản đã bị khóa thành công!");
    }

    @FXML
    public void handleUnlockUser(ActionEvent event) {
        if (tableUsers.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn user để mở khóa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã mở khóa", "Tài khoản đã được mở khóa!");
    }

    // --- Duyệt sản phẩm ---
    @FXML
    public void handleApproveProduct(ActionEvent event) {
        // 1. Ép kiểu dòng được chọn trên bảng thành đối tượng Item
        Item selectedItem = (Item) tableProducts.getSelectionModel().getSelectedItem();


        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để duyệt");
            return;
        }

        // 2. Gọi DAO để update trạng thái thành APPROVED
        itemDAO.updateItemStatus(selectedItem, ItemStatus.APPROVED)
        ;

        showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Sản phẩm đã được đưa lên sàn đấu giá");

        tableProducts.getItems().remove(selectedItem);
        tableProducts.refresh();
    }

    @FXML
    public void handleRejectProduct(ActionEvent event) {
        // 1. Ép kiểu dòng được chọn trên bảng thành đối tượng Item
        Item selectedItem = (Item) tableProducts.getSelectionModel().getSelectedItem();

        // 2. Kiểm tra điều kiện (Tránh lỗi nếu chưa chọn gì trên bảng)
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để từ chối");
            return;
        }

        // 3. Gọi DAO để update trạng thái thành REJECTED trong Data
        itemDAO.updateItemStatus(selectedItem, ItemStatus.REJECTED);

        // 4. Xóa sản phẩm vừa xử lý khỏi danh sách trên bảng và làm mới giao diện
        tableProducts.getItems().remove(selectedItem);
        tableProducts.refresh();

        // 5. Hiển thị thông báo thành công
        showAlert(Alert.AlertType.INFORMATION, "Đã từ chối", "Sản phẩm đã bị từ chối và xóa khỏi danh sách chờ duyệt");
    }

    // --- Duyệt nạp tiền ---
    @FXML
    public void handleApproveDeposit(ActionEvent event) {
        if (tableDeposits.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn yêu cầu để duyệt!");
            return;
        }
        // TODO: gửi lệnh duyệt qua socket sau khi thành viên 3 làm xong
        showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Yêu cầu nạp tiền đã được duyệt!\nSố dư user đã được cộng.");
    }

    @FXML
    public void handleRejectDeposit(ActionEvent event) {
        if (tableDeposits.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn yêu cầu để từ chối!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã từ chối", "Yêu cầu nạp tiền đã bị từ chối!");
    }

    // --- Quản lý đấu giá ---
    @FXML
    public void handleStopAuction(ActionEvent event) {
        if (tableAuctions.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiên đấu giá để dừng!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận dừng");
        confirm.setContentText("Bạn có chắc muốn dừng phiên đấu giá này không?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Đã dừng", "Phiên đấu giá đã được dừng thủ công!");
            }
        });
    }

    // --- Đăng xuất ---
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/login-view.fxml"));
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