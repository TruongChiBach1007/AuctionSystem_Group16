package com.auction.controller;

import com.auction.model.core.TopUpMessage;
import com.auction.model.users.Bidder;
import com.auction.network.GUIClientManager;
import javafx.application.Platform;
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
import javafx.scene.control.Alert;

import java.io.IOException;

import static com.auction.controller.AuctionRoomController.networkManager;

public class AdminDashboardController {

    @FXML
    private Label lblAdminName;
    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblTotalProducts;
    @FXML
    private Label lblPending;
    @FXML
    private Label lblRevenue;

    // Các pane tab
    @FXML
    private VBox paneOverview;
    @FXML
    private VBox paneUsers;
    @FXML
    private VBox paneProducts;
    @FXML
    private VBox paneDeposits;
    @FXML
    private VBox paneAuctions;

    // Sidebar buttons
    @FXML
    private Button btnOverview;
    @FXML
    private Button btnUsers;
    @FXML
    private Button btnProducts;
    @FXML
    private Button btnDeposits;
    @FXML
    private Button btnAuctions;

    // Tables
    @FXML
    private TableView tableUsers;
    @FXML
    private TableView tableProducts;
    @FXML
    private TableView tableDeposits;
    @FXML
    private TableView tableAuctions;

    @FXML
    public void initialize() {
        GUIClientManager.getInstance().startConnection("localhost", 1234);
        com.auction.network.GUIClientManager.getInstance().setController(this);
        // Load dữ liệu tổng quan tạm thời
//        lblTotalUsers.setText("12");
//        lblTotalProducts.setText("5");
//        lblPending.setText("3");
//        lblRevenue.setText("150,000,000 VNĐ");
        ((TableColumn<com.auction.model.core.TopUpMessage, String>) tableDeposits.getColumns().get(0))
                .setCellValueFactory(new PropertyValueFactory<>("username"));

        // Cột 1 (Số tiền): Lấy từ biến "newBalance" trong TopUpMessage
        ((TableColumn<com.auction.model.core.TopUpMessage, Double>) tableDeposits.getColumns().get(1))
                .setCellValueFactory(new PropertyValueFactory<>("newBalance"));

        // 3. Khởi tạo danh sách rỗng cho bảng để nó sẵn sàng nhận dữ liệu
        tableDeposits.setItems(javafx.collections.FXCollections.observableArrayList());

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

    @FXML
    public void showOverview(ActionEvent e) {
        showPane(paneOverview, btnOverview);
    }

    @FXML
    public void showUsers(ActionEvent e) {
        showPane(paneUsers, btnUsers);
    }

    @FXML
    public void showProducts(ActionEvent e) {
        showPane(paneProducts, btnProducts);
    }

    @FXML
    public void showDeposits(ActionEvent e) {
        showPane(paneDeposits, btnDeposits);
    }

    @FXML
    public void showAuctions(ActionEvent e) {
        showPane(paneAuctions, btnAuctions);
    }

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
        if (tableProducts.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để duyệt!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Sản phẩm đã được duyệt và đưa lên sàn!");
    }

    @FXML
    public void handleRejectProduct(ActionEvent event) {
        if (tableProducts.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để từ chối!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Đã từ chối", "Sản phẩm đã bị từ chối!");
    }

    // --- Duyệt nạp tiền ---
    @FXML
    public void handleApproveDeposit(ActionEvent event) {
        if (tableDeposits.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn yêu cầu để duyệt!");
            return;
        }
        // TODO: gửi lệnh duyệt qua socket sau khi thành viên 3 làm xong
        // Lấy username của người được nạp và số dư mới sau khi cộng
        // Lấy người dùng được chọn từ bảng tableDeposits
        TopUpMessage targetUser = (TopUpMessage) tableDeposits.getSelectionModel().getSelectedItem();

        try {
            // Lấy chính xác gói tin TopUpMessage từ hàng đang được chọn trên bảng
            com.auction.model.core.TopUpMessage selectedMsg = (com.auction.model.core.TopUpMessage) tableDeposits.getSelectionModel().getSelectedItem();

            if (selectedMsg != null) {
                // 1. ĐỔI TRẠNG THÁI: Chuyển từ "Chờ duyệt" sang "Hoàn thành"
                selectedMsg.setStatus("Hoàn thành");

                // Ép bảng vẽ lại giao diện để kích hoạt bộ lọc đổi sang MÀU XANH LÁ ngay lập tức
                tableDeposits.refresh();

                // 2. PHÁT LOA LÊN SERVER: Bắn gói tin đã đổi trạng thái này lên mạng
                // Sử dụng bản getInstance() static chống null của cậu nhé
                GUIClientManager.getInstance().sendTopUp(selectedMsg);

                // 3. Hiện thông báo UI (Giữ nguyên hàm showAlert của bạn nhóm bạn)
                showAlert(Alert.AlertType.INFORMATION, "Đã duyệt", "Yêu cầu nạp tiền của " + selectedMsg.getUsername() + " đã được phê duyệt!");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không thể gửi dữ liệu duyệt lên Server!");
        }
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

    // === PHẦN CỦA THÀNH VIÊN 3 (NETWORK) - CHỈ THÊM MỚI ===
    public void receiveDepositRequest(TopUpMessage msg) {
        javafx.application.Platform.runLater(() -> {
            if (tableDeposits.getItems() == null) {
                tableDeposits.setItems(javafx.collections.FXCollections.observableArrayList());
            }

            // Cột 0: Tài khoản
            ((javafx.scene.control.TableColumn<TopUpMessage, String>) tableDeposits.getColumns().get(0))
                    .setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));

            // Cột 1: Số tiền
            ((javafx.scene.control.TableColumn<TopUpMessage, Double>) tableDeposits.getColumns().get(1))
                    .setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("newBalance"));

            // Cột 2: Thời gian
            ((javafx.scene.control.TableColumn<TopUpMessage, String>) tableDeposits.getColumns().get(2))
                    .setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("requestTime"));

            // Cột 3: Trạng thái
            javafx.scene.control.TableColumn<TopUpMessage, String> statusCol =
                    (javafx.scene.control.TableColumn<TopUpMessage, String>) tableDeposits.getColumns().get(3);
            statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));

            // Bộ đổ màu chữ: Vàng (Chờ duyệt), Xanh lá (Hoàn thành)
            statusCol.setCellFactory(column -> new javafx.scene.control.TableCell<TopUpMessage, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if ("Chờ duyệt".equals(item)) {
                            setStyle("-fx-text-fill: #ffcc00; -fx-font-weight: bold;");
                        } else if ("Hoàn thành".equals(item)) {
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            tableDeposits.getItems().add(msg);

            try {
                int p = Integer.parseInt(lblPending.getText());
                lblPending.setText(String.valueOf(p + 1));
            } catch (Exception e) {
                lblPending.setText("1");
            }
            System.out.println(">>> [NETWORK] Admin da hien thi yeu cau cua: " + msg.getUsername());
        });
    }
}