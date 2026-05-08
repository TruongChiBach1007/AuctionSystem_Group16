package com.auction.controller;

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
import java.util.Arrays;
import java.util.List;

public class LoginController {

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 1234;

    private final List<String> validGuests = Arrays.asList("minh", "bach", "dung", "duy");

    @FXML
    public void handleConnect(ActionEvent event) {
        String username = nameField.getText().trim().toLowerCase();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            hienThiLoi("Vui lòng nhập đầy đủ tên và mật khẩu!");
            return;
        }

        // ADMIN
        if (username.equals("admin") && password.equals("1234")) {
            System.out.println("Role: ADMIN đang đăng nhập...");
            chuyenManHinhAdmin(event, username);
        }
        // GUEST/BIDDER
        else if (validGuests.contains(username) && password.equals("1234")) {
            System.out.println("Role: GUEST - User: " + username);
            chuyenManHinhBidder(event, username);
        }
        else {
            hienThiLoi("Sai tài khoản hoặc mật khẩu. Vui lòng thử lại!");
        }
    }

    private void chuyenManHinhAdmin(ActionEvent event, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdminName(username);

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

    private void chuyenManHinhBidder(ActionEvent event, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            BidderDashboardController controller = loader.getController();
            controller.setLblUsername(username);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Trang Chủ - Chào " + username.toUpperCase());
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
}