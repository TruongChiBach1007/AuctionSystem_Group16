package com.auction.client.controller;

import com.auction.shared.model.items.Art;
import com.auction.shared.model.items.Electronics;
import com.auction.shared.model.items.Item;
import com.auction.shared.model.items.Vehicle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class BidderDashboardController {

    @FXML private Label lblUsername;
    @FXML private Label lblBalance;

    private long balance = 50000;

    public void setLblUsername(String username) {
        lblUsername.setText("Tên TK: " + username.toUpperCase());
    }

    @FXML
    public void initialize() {
        updateBalanceLabel();
    }

    private void updateBalanceLabel() {
        lblBalance.setText(String.format("Số dư: %,d VNĐ", balance));
    }

    @FXML
    public void handleDeposit(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("100000");
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nạp tiền qua VNPay");
        dialog.setContentText("Nhập số tiền muốn nạp (VNĐ):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                long amount = Long.parseLong(input.trim().replace(",", ""));
                if (amount <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải lớn hơn 0!");
                    return;
                }

                // KHÔNG cộng tiền ngay — chờ admin duyệt
                // TODO: gửi yêu cầu lên server sau khi thành viên 3 làm socket

                showAlert(Alert.AlertType.INFORMATION, "Đã gửi yêu cầu",
                        String.format("Yêu cầu nạp %,d VNĐ đã được ghi nhận!\n" +
                                "Vui lòng chờ Admin xác nhận.", amount));

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ! Vui lòng nhập số.");
            }
        });
    }

    @FXML
    public void handleAddProduct(ActionEvent event) {
        Stage popup = new Stage();
        popup.setTitle("Đăng sản phẩm đấu giá");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setPrefWidth(400);
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("ĐĂNG SẢN PHẨM MỚI");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblCategory = new Label("Danh mục:");
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        cmbCategory.setPromptText("Chọn danh mục...");
        cmbCategory.setPrefWidth(360);

        Label lblName = new Label("Tên sản phẩm:");
        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên sản phẩm...");
        txtName.setPrefWidth(360);

        Label lblDesc = new Label("Mô tả:");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Nhập mô tả...");
        txtDesc.setPrefHeight(80);
        txtDesc.setWrapText(true);

        Label lblPrice = new Label("Giá khởi điểm (VNĐ):");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("VD: 1000000");

        Label lblExtra = new Label();
        lblExtra.setVisible(false);
        lblExtra.setManaged(false);
        TextField txtExtra = new TextField();
        txtExtra.setVisible(false);
        txtExtra.setManaged(false);

        cmbCategory.setOnAction(e -> {
            String cat = cmbCategory.getValue();
            if (cat == null) return;
            switch (cat) {
                case "Electronics":
                    lblExtra.setText("Bảo hành (tháng):");
                    txtExtra.setPromptText("VD: 12");
                    break;
                case "Art":
                    lblExtra.setText("Tên nghệ sĩ:");
                    txtExtra.setPromptText("VD: Vincent van Gogh");
                    break;
                case "Vehicle":
                    lblExtra.setText("Năm sản xuất:");
                    txtExtra.setPromptText("VD: 2024");
                    break;
            }
            lblExtra.setVisible(true);
            lblExtra.setManaged(true);
            txtExtra.setVisible(true);
            txtExtra.setManaged(true);
        });

        HBox buttons = new HBox(10);
        Button btnSave = new Button("💾 Đăng sản phẩm");
        btnSave.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        buttons.getChildren().addAll(btnSave, btnCancel);

        btnCancel.setOnAction(e -> popup.close());

        btnSave.setOnAction(e -> {
            if (txtName.getText().isEmpty() || cmbCategory.getValue() == null || txtPrice.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
                return;
            }
            try {
                String id = UUID.randomUUID().toString().substring(0, 8);
                String name = txtName.getText().trim();
                String desc = txtDesc.getText().trim();
                double startPrice = Double.parseDouble(txtPrice.getText().trim());
                String category = cmbCategory.getValue();

                Item newItem;
                switch (category) {
                    case "Electronics":
                        int warranty = txtExtra.getText().isEmpty() ? 0
                                : Integer.parseInt(txtExtra.getText().trim());
                        Electronics elec = new Electronics(id, name, desc, startPrice, startPrice);
                        elec.setWarrantyMonths(warranty);
                        newItem = elec;
                        break;
                    case "Art":
                        newItem = new Art(id, name, desc, startPrice, startPrice);
                        break;
                    case "Vehicle":
                        newItem = new Vehicle(id, name, desc, startPrice, startPrice);
                        break;
                    default: return;
                }

                // TODO: gửi lên server sau khi thành viên 3 làm socket
                System.out.println("Sản phẩm mới: " + newItem.getName() + " - " + newItem.getStartingPrice());

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã đăng sản phẩm \"" + newItem.getName() + "\" thành công!\nChờ Admin duyệt.");
                popup.close();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tiền phải là số!");
            }
        });

        form.getChildren().addAll(
                title, new Separator(),
                lblCategory, cmbCategory,
                lblName, txtName,
                lblDesc, txtDesc,
                lblPrice, txtPrice,
                lblExtra, txtExtra,
                buttons
        );

        popup.setScene(new Scene(form));
        popup.showAndWait();
    }

    @FXML
    public void handleJoinAuction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/views/auction-room.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Phòng Đấu Giá Trực Tiếp - Live!");
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file giao diện phòng đấu giá!");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/views/login-view.fxml"));
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