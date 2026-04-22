package com.auction.controller;

import com.auction.model.items.Item;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller điều khiển giao diện chính của ứng dụng đấu giá.
 * Đảm nhiệm vai trò "Bồi bàn" trong mô hình MVC.
 */
public class HelloController {

    // --- PHẦN 1: KẾT NỐI VỚI FXML (VIEW) ---
    // Các biến này phải trùng khớp với fx:id trong file hello-view.fxml
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

    // Danh sách quan sát được dùng để tự động cập nhật TableView khi dữ liệu thay đổi
    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    /**
     * Hàm initialize() tự động chạy sau khi file FXML được tải xong.
     * Dùng để thiết lập cấu hình ban đầu cho các thành phần giao diện.
     */
    @FXML
    public void initialize() {
        // Thiết lập cách lấy dữ liệu từ lớp Item để hiển thị vào từng cột
        // "name", "currentPrice", "status" phải khớp với tên biến trong class Item
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Gán danh sách itemList vào TableView
        itemTable.setItems(itemList);
    }

    /**
     * PHẦN 2: XỬ LÝ SỰ KIỆN NHẤN NÚT (MỤC 5 - MVC)
     * Hàm này thực hiện việc lấy dữ liệu người dùng, gọi Logic và bắt lỗi.
     */
    @FXML
    private void handleBidAction() {
        try {
            // Bước 1: Lấy số tiền từ ô nhập (View)
            String input = bidAmountField.getText();
            double amount = Double.parseDouble(input);

            // Bước 2: GỌI LOGIC TỪ MODEL
            // auctionManager.placeBid(itemSelected, amount);

            // Nếu không có ngoại lệ ném ra, thông báo thành công
            messageLabel.setText("Đặt giá thành công: " + amount);
            messageLabel.setStyle("-fx-text-fill: green;");
            bidAmountField.clear();

        } catch (NumberFormatException e) {
            // Xử lý lỗi khi nhập chữ hoặc để trống (Mục 2)
            messageLabel.setText("Lỗi: Vui lòng nhập một con số!");
            messageLabel.setStyle("-fx-text-fill: red;");

        } catch (Exception e) {
            // Bắt các ngoại lệ Custom
            // Ví dụ: InvalidBidException hoặc AuctionClosedException
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * PHẦN 3: CẬP NHẬT TỰ ĐỘNG (MỤC TIÊU TUẦN 8 - OBSERVER PATTERN)
     * Hàm này để các thành phần khác gọi khi có giá mới từ hệ thống.
     */
    public void onPriceUpdate() {
        // Sử dụng Platform.runLater để đảm bảo việc cập nhật UI diễn ra trên luồng chính (Main Thread)
        // Tránh lỗi xung đột luồng khi làm việc với Multi-threading (Mục 4)
        Platform.runLater(() -> {
            itemTable.refresh(); // Làm mới bảng để hiện giá mới nhất
            System.out.println("Giao diện đã cập nhật giá mới!");
        });
    }
}