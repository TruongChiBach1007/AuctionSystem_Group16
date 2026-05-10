package com.auction.controller;

import com.auction.security.AuthService;
import com.auction.model.users.Admin;
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

        // Gọi AuthService để kiểm tra đăng nhập từ DatabaseConnection
        boolean isSuccess = AuthService.getInstance().login(username, password);

        if (isSuccess) {
            System.out.println("Đăng nhập thành công!");
            var currentUser = AuthService.getInstance().getCurrentUser();

            // Kiểm tra role để chuyển màn hình tương ứng
            if (currentUser instanceof Admin) {
                chuyenManHinhAdmin(event, username);
            } else {
                chuyenManHinhBidder(event, username);
            }
        } else {
            hienThiLoi("Sai tài khoản hoặc mật khẩu. Vui lòng thử lại!");
            passwordField.clear();       // Xóa trắng ô mật khẩu cũ
            passwordField.requestFocus(); // Tự động đưa con trỏ chuột vào ô mật khẩu để gõ lại luôn
        }
    }

    private void chuyenManHinhAdmin(ActionEvent event, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/admin-dashboard.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdminName(username);

            thayDoiScene(event, root, "Admin Dashboard - Quản lý hệ thống");
        } catch (IOException e) {
            hienThiLoi("Lỗi: Không tải được giao diện Admin!");
            e.printStackTrace();
        }
    }

    private void chuyenManHinhBidder(ActionEvent event, String username) {
        try {
            // Thay dấu chấm bằng dấu gạch chéo /
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/bidder-dashboard.fxml"));
            Parent root = loader.load();

            BidderDashboardController controller = loader.getController();
            controller.setLblUsername(username);

            thayDoiScene(event, root, "Trang Chủ - Chào " + username.toUpperCase());
        } catch (IOException e) {
            hienThiLoi("Lỗi: Không tải được giao diện Bidder!");
            e.printStackTrace();
        }
    }

    private void thayDoiScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 700));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }

    private void hienThiLoi(String thongBao) {
        messageLabel.setText(thongBao);
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }
}