package com.auction.controller;

import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.model.Item;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class HelloController {

    // giao diện
    @FXML
    private TableView<Item> itemTable;

    @FXML
    private TableColumn<Item, String> nameColumn;

    @FXML
    private TableColumn<Item, Double> priceColumn;

    @FXML
    private TableColumn<Item, String> statusColumn;

    @FXML
    private TextField bidAmountField;

    @FXML
    private Label messageLabel;

    // Danh sách ảo để hiển thị lên bảng
    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Kết nối các cột với thuộc tính của class Item (Model)
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Đổ danh sách vào bảng
        itemTable.setItems(itemList);
    }

    @FXML
    private void handleBidAction() {
        try {
            // 1. Lấy dữ liệu từ TextField
            String input = bidAmountField.getText();
            double amount = Double.parseDouble(input);

            // 2. GỌI LOGIC TỪ MODEL (Phối hợp với TV1)

            // Nếu không có lỗi, thông báo thành công
            messageLabel.setText("Đặt giá thành công: " + amount);
            messageLabel.setStyle("-fx-text-fill: green;");

            // Xóa trắng ô nhập sau khi đặt thành công
            bidAmountField.clear();

        } catch (NumberFormatException e) {
            // Bắt lỗi khi người dùng nhập chữ thay vì nhập số
            messageLabel.setText("Lỗi: Vui lòng nhập số tiền hợp lệ!");
            messageLabel.setStyle("-fx-text-fill: red;");

        } catch (Exception e) {
            // Đây là nơi Minh bắt InvalidBidException và AuctionClosedException (Mục 1 & 2)
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public void onPriceUpdate(double newPrice) {
        // Phải dùng Platform.runLater vì UI không được cập nhật từ Thread ngoài
        Platform.runLater(() -> {
            // Cập nhật lại giao diện ở đây
            // Ví dụ: làm mới bảng hoặc hiện thông báo giá mới
            itemTable.refresh();
            System.out.println("Giá mới đã được cập nhật lên giao diện: " + newPrice);
        });
    }
}