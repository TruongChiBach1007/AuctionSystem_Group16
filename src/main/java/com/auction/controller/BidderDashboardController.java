package com.auction.controller;

import com.auction.model.core.RequestHistoryMessage;
import com.auction.model.core.TopUpMessage;
import com.auction.model.items.*;
import com.auction.network.GUIClientManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BidderDashboardController {

    @FXML
    private Label lblUsername;
    @FXML
    private Label lblBalance;
    @FXML
    private TextField txtSearch;
    @FXML
    private ListView<String> searchList;
    @FXML
    private VBox searchDropdown;
    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblBalanceVal;
    @FXML
    private Label lblSidebarBalance;


    private GUIClientManager networkManager = GUIClientManager.getInstance();

    private long balance = 50000;

    // Danh sách sản phẩm mẫu
    private final List<ProductCard> allProducts = new ArrayList<>();

    public void setLblUsername(String username) {
        if (lblUsername != null) {
            // Kiểm tra nếu tên chưa có chữ "Tên TK: " thì mới thêm vào
            if (!username.startsWith("Tên tài khoản: ")) {
                lblUsername.setText("Tên tài khoản: " + username);
            } else {
                lblUsername.setText(username);
            }
            if (lblWelcome != null) {
                // Giữ lại câu chào và ghép thêm tên của em vào
                lblWelcome.setText("Chào mừng " + username + " trở lại! 👋");
            }
        }
    }

    @FXML
    public void initialize() {
        updateBalanceLabel();
        loadProducts();
        setupSearch();
        GUIClientManager.getInstance().startConnection("localhost", 1234);
        GUIClientManager.getInstance().setController(this);
    }

    private void loadProducts() {
        allProducts.add(new ProductCard(
                "iPhone 15 Pro Max 256GB", "Electronics",
                35000000, 30000000, "Đang diễn ra", "Còn 2h 30p",
                "iPhone 15 Pro Max 256GB, màu titan tự nhiên, mới 100%, fullbox.", "#e8f4fd"));
        allProducts.add(new ProductCard(
                "Bức Họa Đêm Đầy Sao", "Art",
                100000000, 80000000, "Sắp đóng", "Còn 5p",
                "Tranh sơn dầu phong cách Van Gogh, kích thước 80x60cm.", "#fef9e7"));
        allProducts.add(new ProductCard(
                "Honda Civic 2024", "Vehicle",
                750000000, 700000000, "Đang diễn ra", "Còn 1 ngày",
                "Honda Civic RS 2024, màu đỏ, đã đi 5000km.", "#eafaf1"));
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.trim().toLowerCase();
            if (keyword.isEmpty()) {
                searchDropdown.setVisible(false);
                searchDropdown.setManaged(false);
                return;
            }

            List<String> results = new ArrayList<>();
            for (ProductCard p : allProducts) {
                if (p.getName().toLowerCase().contains(keyword)) {
                    results.add(p.getName());
                }
            }

            if (results.isEmpty()) {
                results.add("Không tìm thấy sản phẩm nào");
            }

            searchList.setItems(FXCollections.observableArrayList(results));
            searchList.setPrefHeight(results.size() * 36 + 2);
            searchDropdown.setVisible(true);
            searchDropdown.setManaged(true);
        });

        // Click vào item trong dropdown
        searchList.setOnMouseClicked(e -> {
            String selected = searchList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.equals("Không tìm thấy sản phẩm nào")) return;

            for (ProductCard p : allProducts) {
                if (p.getName().equals(selected)) {
                    showProductDetail(p);
                    break;
                }
            }
            searchDropdown.setVisible(false);
            searchDropdown.setManaged(false);
            txtSearch.clear();
        });
    }

    // Click card 1
    @FXML
    public void handleCardClick1() {
        showProductDetail(allProducts.get(0));
    }

    // Click card 2
    @FXML
    public void handleCardClick2() {
        showProductDetail(allProducts.get(1));
    }

    // Popup chi tiết sản phẩm
    private void showProductDetail(ProductCard product) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Thông tin sản phẩm");

        VBox root = new VBox(0);
        root.setPrefWidth(450);
        root.getStyleClass().add("popup-vbox");
        // Đảm bảo link file CSS
        root.getStylesheets().add(getClass().getResource("/com/auction/css/styles.css").toExternalForm());

        // --- 1. Header Xanh (Đồng bộ banner) ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);");

        Label lblTitle = new Label(product.getName().toUpperCase());
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());

        header.getChildren().addAll(lblTitle, spacer, btnClose);

        // --- 2. Ảnh đại diện (Vùng màu) ---
        Rectangle imgPlaceholder = new Rectangle(450, 160);
        imgPlaceholder.setFill(Color.web(product.getCardColor()));

        // --- 3. Vùng nội dung ---
        VBox body = new VBox(15);
        body.setPadding(new Insets(20));

        // Các dòng thông tin dùng hàm bổ trợ bên dưới
        VBox infoBox = new VBox(0);
        infoBox.getChildren().addAll(
                createInfoRow("📦 Danh mục", product.getCategory()),
                createInfoRow("💰 Giá khởi điểm", String.format("%,.0f VNĐ", product.getStartingPrice())),
                createInfoRow("📈 Giá hiện tại", String.format("%,.0f VNĐ", product.getCurrentPrice())),
                createInfoRow("⏰ Thời gian còn", product.getTimeLeft())
        );

        Label lblDesc = new Label(product.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic; -fx-padding: 5 0;");

        // Nút tham gia (Dùng lại class btn-success xịn của em)
        Button btnJoin = new Button("🚀 THAM GIA ĐẤU GIÁ NGAY");
        btnJoin.getStyleClass().add("btn-success"); // Class xanh lá có đổ bóng
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setPadding(new Insets(12));
        btnJoin.setOnAction(e -> {
            popup.close();
            openAuctionRoom();
        });

        body.getChildren().addAll(infoBox, lblDesc, btnJoin);
        root.getChildren().addAll(header, imgPlaceholder, body);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    // Hàm bổ trợ để tạo dòng thông tin đẹp
    private HBox createInfoRow(String key, String value) {
        HBox row = new HBox();
        row.getStyleClass().add("info-item-row");
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblK = new Label(key);
        lblK.getStyleClass().add("info-item-key");
        lblK.setPrefWidth(150);

        Label lblV = new Label(value);
        lblV.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px;");

        row.getChildren().addAll(lblK, lblV);
        return row;
    }

    private HBox makeRow(String key, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lblKey = new Label(key);
        lblKey.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 160;");
        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");
        row.getChildren().addAll(lblKey, lblVal);
        return row;
    }

    private void openAuctionRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/auction-room.fxml"));
            Parent root = loader.load();
            AuctionRoomController controller = loader.getController();
            controller.setCurrentUser(lblUsername.getText().replace("Tên tài khoản: ", ""));
            controller.setBalance(this.balance);
            GUIClientManager.getInstance().setController(controller);
            GUIClientManager.getInstance().sendRequestHistory(new RequestHistoryMessage());
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Phòng Đấu Giá Trực Tiếp - Live!");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleJoinAuction(ActionEvent event) {
        openAuctionRoom();
    }

    @FXML
    public void handleDeposit(ActionEvent event) {

        Stage popup = new Stage();
        popup.setTitle("Nạp tiền");
        popup.initModality(Modality.APPLICATION_MODAL);

        // ── ROOT ──
        VBox root = new VBox(0);
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 16;");

        // ── HEADER ──
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-background-radius: 16 16 0 0;");

        Label lblSub = new Label("THANH TOÁN AN TOÀN");
        lblSub.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.7);");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(32, 32);
        iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        Label iconLabel = new Label("🏦");
        iconLabel.setStyle("-fx-font-size: 15;");
        iconBox.getChildren().add(iconLabel);
        Label lblTitle = new Label("Nạp tiền qua VNPay");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        titleRow.getChildren().addAll(iconBox, lblTitle);
        header.getChildren().addAll(lblSub, titleRow);

        // ── BODY ──
        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 20, 28));
        body.setStyle("-fx-background-color: white;");

        // Số dư hiện tại
        HBox balanceBox = new HBox(10);
        balanceBox.setAlignment(Pos.CENTER_LEFT);
        balanceBox.setPadding(new Insets(12, 16, 12, 16));
        balanceBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10; -fx-border-color: #dbeafe; -fx-border-radius: 10;");
        Label icoBalance = new Label("💰");
        icoBalance.setStyle("-fx-font-size: 16;");
        VBox balanceText = new VBox(2);
        Label lblBalanceTitle = new Label("Số dư hiện tại");
        lblBalanceTitle.setStyle("-fx-font-size: 11; -fx-text-fill: #94aac8;");
        lblBalanceVal = new Label(String.format("%,d VNĐ", balance));
        lblBalanceVal.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        balanceText.getChildren().addAll(lblBalanceTitle, lblBalanceVal);
        balanceBox.getChildren().addAll(icoBalance, balanceText);

        // Gợi ý nhanh
        VBox boxQuick = new VBox(8);
        HBox lblQuickRow = new HBox(6);
        lblQuickRow.setAlignment(Pos.CENTER_LEFT);
        Label icoQuick = new Label("⚡");
        icoQuick.setStyle("-fx-font-size: 12;");
        Label lblQuick = new Label("Chọn nhanh");
        lblQuick.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e3154;");
        lblQuickRow.getChildren().addAll(icoQuick, lblQuick);

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Nhập số tiền muốn nạp...");
        txtAmount.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 14; -fx-font-size: 13;");

        HBox quickBtns = new HBox(8);
        String[] amounts = {"50,000", "100,000", "500,000", "1,000,000"};
        for (String amt : amounts) {
            Button btn = new Button(amt);
            btn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1976D2; " +
                    "-fx-font-weight: bold; -fx-font-size: 11; " +
                    "-fx-background-radius: 8; -fx-border-color: #dbeafe; -fx-border-radius: 8; " +
                    "-fx-padding: 6 10; -fx-cursor: hand;");
            btn.setOnAction(e -> txtAmount.setText(amt.replace(",", "")));
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            quickBtns.getChildren().add(btn);
        }

        boxQuick.getChildren().addAll(lblQuickRow, quickBtns);

        // Input số tiền
        VBox boxAmount = new VBox(6);
        HBox lblAmountRow = new HBox(6);
        lblAmountRow.setAlignment(Pos.CENTER_LEFT);
        Label icoAmt = new Label("💵");
        icoAmt.setStyle("-fx-font-size: 12;");
        Label lblAmt = new Label("Số tiền nạp");
        lblAmt.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e3154;");
        Label star = new Label("*");
        star.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblAmountRow.getChildren().addAll(icoAmt, lblAmt, star);

        HBox inputRow = new HBox(0);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeVnd = new Label("  VNĐ  ");
        badgeVnd.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1976D2; " +
                "-fx-font-weight: bold; -fx-font-size: 12; " +
                "-fx-background-radius: 10 0 0 10; " +
                "-fx-border-color: #dde3f0; -fx-border-width: 1 0 1 1; " +
                "-fx-border-radius: 10 0 0 10; -fx-padding: 10 10;");
        txtAmount.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; " +
                "-fx-padding: 10 14; -fx-font-size: 13;");
        HBox.setHgrow(txtAmount, Priority.ALWAYS);
        inputRow.getChildren().addAll(badgeVnd, txtAmount);

        // Note
        Label lblNote = new Label("ℹ️  Yêu cầu sẽ được Admin xác nhận trong vòng 5–15 phút.");
        lblNote.setStyle("-fx-font-size: 11; -fx-text-fill: #94aac8;");
        lblNote.setWrapText(true);

        boxAmount.getChildren().addAll(lblAmountRow, inputRow, lblNote);

        body.getChildren().addAll(balanceBox, boxQuick, boxAmount);

        // ── SEPARATOR ──
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e8edf5;");

        // ── FOOTER ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 28, 16, 28));
        footer.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 0 0 16 16;");

        Button btnCancel = new Button("✕  Huỷ");
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-text-fill: #4a6080; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-padding: 10 22; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> popup.close());

        Button btnConfirm = new Button("🏦  Xác nhận nạp tiền");
        btnConfirm.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13; " +
                "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.35), 10, 0, 0, 3);");
        btnConfirm.setOnAction(e -> {
            String input = txtAmount.getText().trim().replace(",", "");
            if (input.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số tiền!");
                return;
            }
            try {
                long amount = Long.parseLong(input);
                //sendDepositRequestToNetwork(amount);
                if (amount <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải lớn hơn 0!");
                    return;
                }
                sendDepositRequestToNetwork(amount); // them de ho tro cho phan admin thay so tien user muon nap
                showAlert(Alert.AlertType.INFORMATION, "Đã gửi yêu cầu",
                        String.format("Yêu cầu nạp %,d VNĐ đã được ghi nhận!\nVui lòng chờ Admin xác nhận.", amount));
                popup.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ!");
            }
        });

        footer.getChildren().addAll(btnCancel, btnConfirm);

        root.getChildren().addAll(header, body, sep, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();


    }

    @FXML
    public void handleAddProduct(ActionEvent event) {
        Stage popup = new Stage();
        popup.setTitle("Đăng sản phẩm đấu giá");
        popup.initModality(Modality.APPLICATION_MODAL);

        // ── ROOT ──
        VBox root = new VBox(0);
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 16;");

        // ── HEADER gradient ──
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); -fx-background-radius: 16 16 0 0;");

        Label lblSub = new Label("ĐĂNG SẢN PHẨM MỚI");
        lblSub.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.7);");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(32, 32);
        iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        Label iconLabel = new Label("🏷️");
        iconLabel.setStyle("-fx-font-size: 15;");
        iconBox.getChildren().add(iconLabel);
        Label lblTitle = new Label("Thông tin sản phẩm đấu giá");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        titleRow.getChildren().addAll(iconBox, lblTitle);
        header.getChildren().addAll(lblSub, titleRow);

        // ── BODY ──
        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 20, 28));
        body.setStyle("-fx-background-color: white;");

        // Style dùng lại
        String inputStyle = "-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 14; -fx-font-size: 13;";
        String labelStyle = "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e3154;";

        // Danh mục
        VBox boxCat = new VBox(6);
        HBox lblCatRow = new HBox(6);
        lblCatRow.setAlignment(Pos.CENTER_LEFT);
        Label icoCat = new Label("📂");
        icoCat.setStyle("-fx-font-size: 12;");
        Label lblCat = new Label("Danh mục");
        lblCat.setStyle(labelStyle);
        Label starCat = new Label("*");
        starCat.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblCatRow.getChildren().addAll(icoCat, lblCat, starCat);
        ComboBox<String> cmbCategory = new ComboBox<>();
        cmbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        cmbCategory.setPromptText("Chọn danh mục...");
        cmbCategory.setMaxWidth(Double.MAX_VALUE);
        cmbCategory.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13;");
        boxCat.getChildren().addAll(lblCatRow, cmbCategory);

        // Tên sản phẩm
        VBox boxName = new VBox(6);
        HBox lblNameRow = new HBox(6);
        lblNameRow.setAlignment(Pos.CENTER_LEFT);
        Label icoName = new Label("🏷️");
        icoName.setStyle("-fx-font-size: 12;");
        Label lblName = new Label("Tên sản phẩm");
        lblName.setStyle(labelStyle);
        Label starName = new Label("*");
        starName.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblNameRow.getChildren().addAll(icoName, lblName, starName);
        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên sản phẩm...");
        txtName.setStyle(inputStyle);
        boxName.getChildren().addAll(lblNameRow, txtName);

        // Mô tả
        VBox boxDesc = new VBox(6);
        HBox lblDescRow = new HBox(6);
        lblDescRow.setAlignment(Pos.CENTER_LEFT);
        Label icoDesc = new Label("📝");
        icoDesc.setStyle("-fx-font-size: 12;");
        Label lblDesc = new Label("Mô tả");
        lblDesc.setStyle(labelStyle);
        lblDescRow.getChildren().addAll(icoDesc, lblDesc);
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Nhập mô tả sản phẩm...");
        txtDesc.setPrefHeight(90);
        txtDesc.setWrapText(true);
        txtDesc.setStyle(inputStyle);
        boxDesc.getChildren().addAll(lblDescRow, txtDesc);

        // Giá khởi điểm
        VBox boxPrice = new VBox(6);
        HBox lblPriceRow = new HBox(6);
        lblPriceRow.setAlignment(Pos.CENTER_LEFT);
        Label icoPrice = new Label("💰");
        icoPrice.setStyle("-fx-font-size: 12;");
        Label lblPrice = new Label("Giá khởi điểm");
        lblPrice.setStyle(labelStyle);
        Label starPrice = new Label("*");
        starPrice.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        lblPriceRow.getChildren().addAll(icoPrice, lblPrice, starPrice);
        HBox priceRow = new HBox(0);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeVnd = new Label("  VNĐ  ");
        badgeVnd.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1976D2; " +
                "-fx-font-weight: bold; -fx-font-size: 12; " +
                "-fx-background-radius: 10 0 0 10; " +
                "-fx-border-color: #dde3f0; -fx-border-width: 1 0 1 1; " +
                "-fx-border-radius: 10 0 0 10; -fx-padding: 10 10;");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("VD: 1,000,000");
        txtPrice.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; " +
                "-fx-padding: 10 14; -fx-font-size: 13;");
        HBox.setHgrow(txtPrice, Priority.ALWAYS);
        priceRow.getChildren().addAll(badgeVnd, txtPrice);
        boxPrice.getChildren().addAll(lblPriceRow, priceRow);

        // Field phụ theo danh mục
        VBox boxExtra = new VBox(6);
        boxExtra.setVisible(false);
        boxExtra.setManaged(false);
        HBox lblExtraRow = new HBox(6);
        lblExtraRow.setAlignment(Pos.CENTER_LEFT);
        Label icoExtra = new Label("🔧");
        icoExtra.setStyle("-fx-font-size: 12;");
        Label lblExtra = new Label("");
        lblExtra.setStyle(labelStyle);
        lblExtraRow.getChildren().addAll(icoExtra, lblExtra);
        TextField txtExtra = new TextField();
        txtExtra.setStyle(inputStyle);
        boxExtra.getChildren().addAll(lblExtraRow, txtExtra);

        cmbCategory.setOnAction(e -> {
            String cat = cmbCategory.getValue();
            if (cat == null) return;
            switch (cat) {
                case "Electronics":
                    icoExtra.setText("🛡️");
                    lblExtra.setText("Bảo hành (tháng):");
                    txtExtra.setPromptText("VD: 12");
                    break;
                case "Art":
                    icoExtra.setText("🎨");
                    lblExtra.setText("Tên nghệ sĩ:");
                    txtExtra.setPromptText("VD: Van Gogh");
                    break;
                case "Vehicle":
                    icoExtra.setText("📅");
                    lblExtra.setText("Năm sản xuất:");
                    txtExtra.setPromptText("VD: 2024");
                    break;
            }
            boxExtra.setVisible(true);
            boxExtra.setManaged(true);
        });

        body.getChildren().addAll(boxCat, boxName, boxDesc, boxPrice, boxExtra);

        // ── SEPARATOR ──
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e8edf5;");

        // ── FOOTER ──
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 28, 16, 28));
        footer.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 0 0 16 16;");

        Button btnCancel = new Button("✕  Huỷ");
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #dde3f0; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-text-fill: #4a6080; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-padding: 10 22; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> popup.close());

        Button btnSave = new Button("🏷️  Đăng sản phẩm");
        btnSave.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13; " +
                "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.35), 10, 0, 0, 3);");
        btnSave.setOnAction(e -> {
            if (txtName.getText().isEmpty() || cmbCategory.getValue() == null || txtPrice.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin bắt buộc!");
                return;
            }
            try {
                double startPrice = Double.parseDouble(txtPrice.getText().trim().replace(",", ""));
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã đăng sản phẩm \"" + txtName.getText().trim() + "\"!\nChờ Admin duyệt.");
                popup.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tiền phải là số!");
            }
        });

        footer.getChildren().addAll(btnCancel, btnSave);

        root.getChildren().addAll(header, body, sep, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

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

    private void updateBalanceLabel() {
        lblBalance.setText(String.format("Số dư: %,d VNĐ", balance));
        if (lblSidebarBalance != null) {
            lblSidebarBalance.setText(String.format("%,d VNĐ", balance)); // sẽ hoạt động sau khi thêm fx:id
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
    // === PHẦN CỦA THÀNH VIÊN 3 (NETWORK) - BẢN THÊM THỜI GIAN & TRẠNG THÁI ===
// === PHẦN CỦA THÀNH VIÊN 3 (NETWORK) - BẢN THÊM THỜI GIAN & TRẠNG THÁI ===
    public void sendDepositRequestToNetwork(double amount) {
        try {
            // 1. Lấy tên user (Giữ nguyên logic của cậu)
            String username = lblUsername.getText().replace("Tên TK: ", "").trim();

            // --- THÊM: Tự động lấy thời gian thực của máy tính (Định dạng Giờ:Phút Ngày/Tháng) ---
            String currentTime = new java.text.SimpleDateFormat("HH:mm dd/MM").format(new java.util.Date());
            String initialStatus = "Chờ duyệt";

            // 2. Tạo gói tin mới với đầy đủ 4 thông tin
            TopUpMessage request = new TopUpMessage(username, amount, currentTime, initialStatus);

            // 3. Bắn lên Server
            GUIClientManager.getInstance().sendTopUp(request);

            System.out.println(">>> [NETWORK] Da gui yeu cau nap " + amount + " | Thoi gian: " + currentTime);

        } catch (Exception e) {
            System.err.println(">>> [NETWORK] LOI KHI GUI TIN: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // === PHẦN CỦA THÀNH VIÊN 3 (NETWORK)
    public void handleNetworkTopUpSuccess(double amount) {
        try {
            this.balance += amount; // 1. Cộng tiền vào biến chung (Đã chạy đúng)
            updateBalanceLabel();   // 2. Gọi hàm để tự động đổi chữ cả góc trên và sidebar Trang chủ!
            System.out.println(">>> [NETWORK] Đã đồng bộ số dư mới: " + this.balance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// =======================================================================
// =======================================================================
}