package com.auction.controller;

import com.auction.dao.IUserDAO;
import com.auction.dao.UserDAOImpl;
import com.auction.model.users.Bidder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {
    private final IUserDAO userDAO = new UserDAOImpl();

    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label messageLabel;

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!email.contains("@")) {
            messageLabel.setText("Email không hợp lệ!");
            return;
        }
        if (userDAO.findByUsername(username) != null) {
            messageLabel.setText("Tên tài khoản đã tồn tại!");
            return;
        }

        int newId = userDAO.getAllUsers().stream()
                .mapToInt(user -> user.getId())
                .max()
                .orElse(0) + 1;
        userDAO.addUser(new Bidder(newId, username, password, fullName, email, 0.0));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng ký thành công");
        alert.setHeaderText(null);
        alert.setContentText("Tài khoản \"" + username + "\" đã được tạo!\nVui lòng đăng nhập để tiếp tục.");
        alert.showAndWait();
        handleBackToLogin(event);

    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
