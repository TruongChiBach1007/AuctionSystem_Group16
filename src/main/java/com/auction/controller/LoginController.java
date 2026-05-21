package com.auction.controller;

import com.auction.model.users.Admin;
import com.auction.model.users.Bidder;
import com.auction.model.users.Seller;
import com.auction.model.users.User;
import com.auction.security.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void handleConnect(ActionEvent event) {
        String username = nameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            hienThiLoi("Vui lòng nhập đầy đủ tên và mật khẩu!");
            return;
        }

        // Gọi AuthService thật thay vì if/else hardcode
        AuthService auth = AuthService.getInstance();
        boolean success = auth.login(username, password);

        if (!success) {
            hienThiLoi("Sai tài khoản hoặc mật khẩu. Vui lòng thử lại!");
            return;
        }

        User currentUser = auth.getCurrentUser();

        // Phân quyền theo role
        if (currentUser instanceof Admin) {
            chuyenManHinhAdmin(event, currentUser);
        } else if (currentUser instanceof Seller) {
            chuyenManHinhSeller(event, currentUser);
        } else  {
            chuyenManHinhBidder(event, currentUser);
        }
    }

    private void chuyenManHinhAdmin(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdminName(user.getFullName());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Admin Dashboard - Quản lý hệ thống");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            hienThiLoi("Lỗi hệ thống: Không tải được giao diện Admin!");
            e.printStackTrace();
        }
    }

    private void chuyenManHinhBidder(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            BidderDashboardController controller = loader.getController();
            controller.setUserInfo(user.getUsername(), user.getFullName());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chủ - Chào " + user.getFullName().toUpperCase());
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            hienThiLoi("Lỗi hệ thống: Không tải được giao diện!");
            e.printStackTrace();
        }
    }
    private void chuyenManHinhSeller(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/seller-dashboard.fxml"));
            Parent root = loader.load();

            SellerDashboardController controller = loader.getController();
            controller.initData(user.getUsername(), user.getFullName());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chủ - Chào " + user.getFullName().toUpperCase());
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            hienThiLoi("Lỗi hệ thống: Không tải được giao diện!");
            e.printStackTrace();
        }
    }

    private void hienThiLoi(String thongBao) {
        messageLabel.setText(thongBao);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }
    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
