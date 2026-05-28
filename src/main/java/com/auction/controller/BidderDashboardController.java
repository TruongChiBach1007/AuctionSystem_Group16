package com.auction.controller;

import com.auction.model.core.DepositRequest;
import com.auction.model.items.Art;
import com.auction.model.items.Electronics;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.items.Vehicle;
import com.auction.model.users.Bidder;
import com.auction.network.AuctionClient;
import com.auction.network.AuctionMessage;
import com.auction.network.MessageType;
import com.auction.security.AuthService;
import com.auction.utils.DatabaseConnection;
import javafx.application.Platform;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BidderDashboardController {

    // ── Header ──
    @FXML private Label lblUsername;
    @FXML private Label lblBalance;
    @FXML private Label lblAvatar;

    // ── Search ──
    @FXML private TextField txtSearch;
    @FXML private VBox searchDropdown;
    @FXML private ListView<String> searchList;

    // ── Banner ──
    @FXML private Label lblWelcome;
    @FXML private Label lblBannerSub;

    // ── Sidebar trái – category counts ──
    @FXML private Label lblCountElectronics;
    @FXML private Label lblCountArt;
    @FXML private Label lblCountVehicle;

    // ── Sidebar trái – category lists ──
    @FXML private VBox listElectronics;
    @FXML private VBox listArt;
    @FXML private VBox listVehicle;

    // ── Sidebar trái – stats ──
    @FXML private Label lblRunningCount;
    @FXML private Label lblEndingSoonCount;
    @FXML private Label lblBidCount;

    // ── Sidebar phải ──
    @FXML private Label lblRightBalance;
    @FXML private VBox historyBox;

    // ── Center ──
    @FXML private FlowPane productFlow;

    // ── State ──
    private final javafx.collections.ObservableList<Item> approvedItems =
            FXCollections.observableArrayList();
    private long balance = 500000;
    private AuctionClient auctionClient;
    private String currentUsername = "Bidder";
    private String displayName = "Bidder";

    // ─────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Load từ DB
        approvedItems.addAll(
                DatabaseConnection.getInstance().getItemTable().stream()
                        .filter(item -> item.getStatus() == ItemStatus.APPROVED)
                        .toList()
        );

        syncBalanceFromCurrentUser();
        updateBalanceLabels();
        setupSearch();
        renderAll();
        connectSocket();
    }

    // ─────────────────────────────────────────────
    //  PUBLIC SETTER (gọi từ LoginController)
    // ─────────────────────────────────────────────
    public void setLblUsername(String username) {
        setUserInfo(username, resolveFullName(username));
    }

    public void setUserInfo(String username, String fullName) {
        currentUsername = username.replace("Tên tài khoản: ", "")
                .replace("Tên tài khoản: ", "")
                .replace("Ten tai khoan: ", "").trim();
        displayName = fullName == null || fullName.isBlank() ? currentUsername : fullName.trim();
        lblUsername.setText(displayName);
        lblAvatar.setText(displayName.length() > 0
                ? String.valueOf(displayName.charAt(0)).toUpperCase() : "B");
        lblWelcome.setText("Chào mừng " + displayName + " trở lại! 👋");
        syncBalanceFromCurrentUser();
        updateBalanceLabels();
    }

    // ─────────────────────────────────────────────
    //  SOCKET
    // ─────────────────────────────────────────────
    private void connectSocket() {
        auctionClient = new AuctionClient();
        try {
            auctionClient.connect(MessageType.REGISTER_BIDDER, this::handleSocketMessage);
        } catch (IOException e) {
            System.out.println("Bidder cannot connect socket: " + e.getMessage());
        }
    }

    private void handleSocketMessage(AuctionMessage message) {
        if (message == null || message.getType() == null) return;
        Platform.runLater(() -> {
            if (message.getType() == MessageType.ITEM_APPROVED && message.getItem() != null) {
                upsertApprovedItem(message.getItem());
                renderAll();
            } else if (message.getType() == MessageType.DEPOSIT_APPROVED
                    && message.getDepositRequest() != null
                    && currentUsername.equalsIgnoreCase(message.getDepositRequest().getUsername())) {
                addLocalBalance(message.getDepositRequest().getAmount());
                updateBalanceLabels();
            } else if (message.getType() == MessageType.DEPOSIT_REJECTED
                    && message.getDepositRequest() != null
                    && currentUsername.equalsIgnoreCase(message.getDepositRequest().getUsername())) {
                // Admin rejected the request; keep the bidder balance unchanged.
            }
        });
    }

    private void upsertApprovedItem(Item item) {
        approvedItems.removeIf(existing -> existing.getId().equals(item.getId()));
        approvedItems.add(item);
    }

    // ─────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────
    private void setupSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.trim().toLowerCase();

            if (keyword.isEmpty()) {
                searchDropdown.setVisible(false);
                searchDropdown.setManaged(false);
                renderProducts("");
                return;
            }

            List<String> results = new ArrayList<>();
            for (Item item : approvedItems) {
                if (item.getName().toLowerCase().contains(keyword)) {
                    results.add(item.getName());
                }
            }
            if (results.isEmpty()) results.add("Không tìm thấy sản phẩm nào");

            searchList.setItems(FXCollections.observableArrayList(results));
            searchList.setPrefHeight(Math.min(results.size(), 6) * 36 + 2.0);
            searchDropdown.setVisible(true);
            searchDropdown.setManaged(true);

            renderProducts(keyword);
        });

        searchList.setOnMouseClicked(e -> {
            String selected = searchList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.equals("Không tìm thấy sản phẩm nào")) return;

            approvedItems.stream()
                    .filter(item -> item.getName().equals(selected))
                    .findFirst()
                    .ifPresent(this::openAuctionRoom);

            searchDropdown.setVisible(false);
            searchDropdown.setManaged(false);
            txtSearch.clear();
        });
    }

    // ─────────────────────────────────────────────
    //  RENDER ALL
    // ─────────────────────────────────────────────
    private void renderAll() {
        renderCategoryLists();
        renderStats();
        renderHistory();
        renderProducts(txtSearch == null ? "" : txtSearch.getText());
        updateBannerSub();
    }

    /** Cập nhật số lượng badge + danh sách tên sản phẩm trong accordion */
    private void renderCategoryLists() {
        List<Item> electronics = approvedItems.stream().filter(i -> i instanceof Electronics).toList();
        List<Item> arts        = approvedItems.stream().filter(i -> i instanceof Art).toList();
        List<Item> vehicles    = approvedItems.stream().filter(i -> i instanceof Vehicle).toList();

        lblCountElectronics.setText(String.valueOf(electronics.size()));
        lblCountArt.setText(String.valueOf(arts.size()));
        lblCountVehicle.setText(String.valueOf(vehicles.size()));

        fillCategoryList(listElectronics, electronics);
        fillCategoryList(listArt, arts);
        fillCategoryList(listVehicle, vehicles);
    }

    private void fillCategoryList(VBox container, List<Item> items) {
        container.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("  Chưa có sản phẩm");
            empty.setStyle("-fx-text-fill: #94aac8; -fx-font-size: 11; -fx-padding: 4 8;");
            container.getChildren().add(empty);
            return;
        }
        items.stream().limit(5).forEach(item -> {
            Button btn = new Button("• " + item.getName());
            btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; "
                    + "-fx-text-fill: #2c3e50; -fx-padding: 6 8; "
                    + "-fx-alignment: CENTER_LEFT; -fx-font-size: 12;");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> showProductDetail(item));
            container.getChildren().add(btn);
        });
    }

    private void renderStats() {
        lblRunningCount.setText(String.valueOf(approvedItems.size()));
        lblEndingSoonCount.setText("0");
        lblBidCount.setText("0");
    }

    private void updateBannerSub() {
        if (lblBannerSub != null) {
            lblBannerSub.setText("Có " + approvedItems.size() + " phiên đang diễn ra — đừng bỏ lỡ!");
        }
    }

    private void renderHistory() {
        historyBox.getChildren().clear();
        if (approvedItems.isEmpty()) {
            Label empty = new Label("Chưa có sản phẩm để theo dõi.");
            empty.setStyle("-fx-text-fill: #94aac8; -fx-font-size: 12;");
            historyBox.getChildren().add(empty);
            return;
        }

        String[] icons  = {"📈", "⭐", "🔔", "🏷️"};
        String[] colors = {"#fff3e8", "#e8faf0", "#eff6ff", "#fef9e7"};
        String[] tColors = {"#e67e22", "#27ae60", "#3b82f6", "#f59e0b"};

        int[] idx = {0};
        approvedItems.stream().limit(4).forEach(item -> {
            int i = idx[0] % icons.length;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 4 0; -fx-cursor: hand;");

            StackPane iconBox = new StackPane();
            iconBox.setMinSize(32, 32); iconBox.setMaxSize(32, 32);
            iconBox.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 8;");
            Label icon = new Label(icons[i]);
            icon.setStyle("-fx-font-size: 14;");
            iconBox.getChildren().add(icon);

            VBox text = new VBox(2);
            HBox.setHgrow(text, Priority.ALWAYS);
            Label name = new Label(item.getName());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: " + tColors[i] + ";");
            name.setWrapText(true);
            Label sub = new Label("Đang theo dõi");
            sub.setStyle("-fx-text-fill: #94aac8; -fx-font-size: 11;");
            text.getChildren().addAll(name, sub);

            Label arrow = new Label("›");
            arrow.setStyle("-fx-text-fill: #94aac8; -fx-font-size: 16;");

            row.getChildren().addAll(iconBox, text, arrow);
            row.setOnMouseClicked(e -> showProductDetail(item));
            historyBox.getChildren().add(row);
            idx[0]++;
        });
    }

    private void renderProducts(String keyword) {
        productFlow.getChildren().clear();
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase();

        approvedItems.stream()
                .filter(item -> normalized.isEmpty()
                        || item.getName().toLowerCase().contains(normalized))
                .forEach(item -> productFlow.getChildren().add(createProductCard(item)));
    }

    // ─────────────────────────────────────────────
    //  PRODUCT CARD (UI đẹp giống FXML tĩnh)
    // ─────────────────────────────────────────────
    private VBox createProductCard(Item item) {
        VBox card = new VBox(0);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Màu nền ảnh theo category
        String fillColor;
        String icon;
        if (item instanceof Electronics) {
            fillColor = "#dbeafe"; icon = "💻";
        } else if (item instanceof Art) {
            fillColor = "#fef9c3"; icon = "🎨";
        } else if (item instanceof Vehicle) {
            fillColor = "#dcfce7"; icon = "🚗";
        } else {
            fillColor = "#f1f5f9"; icon = "📦";
        }

        // Image area
        StackPane imagePane = new StackPane();
        Rectangle bg = new Rectangle(260, 150);
        bg.setFill(Color.web(fillColor));
        bg.setArcWidth(14); bg.setArcHeight(14);
        Node imageContent = createItemImageNode(item, 260, 150, icon, 36);
        imagePane.getChildren().addAll(bg, imageContent);

        // Badge "Hot" / "Mới"
        Label badge = new Label("Live");
        badge.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; "
                + "-fx-font-size: 10; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-padding: 3 8;");
        StackPane.setAlignment(badge, Pos.TOP_LEFT);
        badge.setTranslateX(10); badge.setTranslateY(10);
        imagePane.getChildren().add(badge);

        // Body
        VBox body = new VBox(6);
        body.setStyle("-fx-padding: 14 14 14 14;");

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1e3154;");

        Label price = new Label(String.format("%,.0f VNĐ", item.getCurrentPrice()));
        price.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 15;");

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
        dot.setStyle("-fx-fill: #27ae60;");
        Label statusLbl = new Label("Đang diễn ra");
        statusLbl.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11;");
        statusBox.getChildren().addAll(dot, statusLbl);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label seller = new Label("🔨 " + safeText(item.getSellerName()));
        seller.setStyle("-fx-font-size: 11; -fx-text-fill: #94aac8;");
        meta.getChildren().addAll(statusBox, spacer, seller);

        body.getChildren().addAll(name, price, meta);
        card.getChildren().addAll(imagePane, body);
        card.setOnMouseClicked(e -> showProductDetail(item));
        return card;
    }

    // ─────────────────────────────────────────────
    //  POPUP CHI TIẾT SẢN PHẨM (UI đẹp từ v2)
    // ─────────────────────────────────────────────
    private void showProductDetail(Item item) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Thông tin sản phẩm");

        VBox root = new VBox(0);
        root.setPrefWidth(450);

        // Header gradient
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6);");

        Label lblTitle = new Label(item.getName().toUpperCase());
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.close());
        header.getChildren().addAll(lblTitle, spacer, btnClose);

        // Image placeholder
        String fillColor = item instanceof Electronics ? "#dbeafe"
                : item instanceof Art ? "#fef9c3"
                : item instanceof Vehicle ? "#dcfce7" : "#f1f5f9";
        StackPane imagePane = new StackPane();
        Rectangle imgPlaceholder = new Rectangle(450, 190);
        imgPlaceholder.setFill(Color.web(fillColor));
        Node popupImage = createItemImageNode(item, 450, 190,
                item instanceof Electronics ? "💻" : item instanceof Art ? "🎨" : item instanceof Vehicle ? "🚗" : "📦",
                42);
        imagePane.getChildren().addAll(imgPlaceholder, popupImage);

        // Body
        VBox body = new VBox(15);
        body.setPadding(new Insets(20));

        VBox infoBox = new VBox(0);
        infoBox.getChildren().addAll(
                createInfoRow("📦 Danh mục", item.getCategory()),
                createInfoRow("💰 Giá hiện tại",
                        String.format("%,.0f VNĐ", item.getCurrentPrice())),
                createInfoRow("👤 Người bán", safeText(item.getSellerName())),
                createInfoRow("📋 Mô tả", safeText(item.getDescription()))
        );

        // Extra info theo loại
        if (item instanceof Electronics e) {
            infoBox.getChildren().add(createInfoRow("🛡️ Bảo hành", e.getWarrantyMonths() + " tháng"));
        } else if (item instanceof Art a) {
            infoBox.getChildren().add(createInfoRow("🎨 Nghệ sĩ", safeText(a.getArtist())));
        } else if (item instanceof Vehicle v) {
            infoBox.getChildren().add(createInfoRow("🚗 Dung tích", v.getEngineCapacity() + "cc"));
        }

        Button btnJoin = new Button("🚀  THAM GIA ĐẤU GIÁ NGAY");
        btnJoin.setMaxWidth(Double.MAX_VALUE);
        btnJoin.setPadding(new Insets(12));
        btnJoin.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 10; "
                + "-fx-cursor: hand; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.35), 10, 0, 0, 3);");
        btnJoin.setOnAction(e -> {
            popup.close();
            openAuctionRoom(item);
        });

        body.getChildren().addAll(infoBox, btnJoin);
        ScrollPane scroller = new ScrollPane(body);
        scroller.setFitToWidth(true);
        scroller.setPrefViewportHeight(360);
        scroller.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");
        root.getChildren().addAll(header, imagePane, scroller);

        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    private HBox createInfoRow(String key, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-border-color: transparent transparent #f0f4ff transparent; "
                + "-fx-border-width: 0 0 1 0;");

        Label lblK = new Label(key);
        lblK.setPrefWidth(160);
        lblK.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a6080; -fx-font-size: 12;");

        Label lblV = new Label(value);
        lblV.setWrapText(true);
        lblV.setStyle("-fx-text-fill: #374151; -fx-font-size: 13;");

        row.getChildren().addAll(lblK, lblV);
        return row;
    }

    // ─────────────────────────────────────────────
    //  MỞ PHÒNG ĐẤU GIÁ
    // ─────────────────────────────────────────────
    private void openAuctionRoom(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/auction-room.fxml"));
            Parent root = loader.load();
            AuctionRoomController controller = loader.getController();
            controller.setCurrentUser(currentUsername);
            controller.setAuctionItem(item);
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("Phòng đấu giá - " + item.getName());
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    //  FXML ACTIONS
    // ─────────────────────────────────────────────
    @FXML
    public void handleAddProduct(ActionEvent event) {
        showAddProductDialog();
    }

    /** Popup đăng sản phẩm – giữ nguyên UI đẹp từ v2 */
    private void showAddProductDialog() {
        Stage popup = new Stage();
        popup.setTitle("Đăng sản phẩm đấu giá");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(0);
        root.setPrefWidth(480);
        root.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 16;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); "
                + "-fx-background-radius: 16 16 0 0;");
        Label lblSub = new Label("ĐĂNG SẢN PHẨM MỚI");
        lblSub.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.7);");
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(32, 32); iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        iconBox.getChildren().add(new Label("🏷️"));
        Label lblTitle = new Label("Thông tin sản phẩm đấu giá");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        titleRow.getChildren().addAll(iconBox, lblTitle);
        header.getChildren().addAll(lblSub, titleRow);

        // Body
        String inputStyle = "-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10 14; -fx-font-size: 13;";
        String labelStyle = "-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e3154;";

        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 20, 28));
        body.setStyle("-fx-background-color: white;");

        // Danh mục
        ComboBox<String> cmbCategory = new ComboBox<>(
                FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        cmbCategory.setPromptText("Chọn danh mục...");
        cmbCategory.setMaxWidth(Double.MAX_VALUE);
        cmbCategory.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 13;");

        // Tên
        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên sản phẩm...");
        txtName.setStyle(inputStyle);

        // Mô tả
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Nhập mô tả sản phẩm...");
        txtDesc.setPrefHeight(90);
        txtDesc.setWrapText(true);
        txtDesc.setStyle(inputStyle);

        // Giá
        HBox priceRow = new HBox(0);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeVnd = new Label("  VNĐ  ");
        badgeVnd.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1976D2; "
                + "-fx-font-weight: bold; -fx-font-size: 12; "
                + "-fx-background-radius: 10 0 0 10; -fx-border-color: #dde3f0; "
                + "-fx-border-width: 1 0 1 1; -fx-border-radius: 10 0 0 10; -fx-padding: 10 10;");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("VD: 1,000,000");
        txtPrice.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; "
                + "-fx-padding: 10 14; -fx-font-size: 13;");
        HBox.setHgrow(txtPrice, Priority.ALWAYS);
        priceRow.getChildren().addAll(badgeVnd, txtPrice);

        // ── CHỌN ẢNH TỪ MÁY (thay URL) ──
        final String[] selectedImagePath = {""};

        HBox imageRow = new HBox(10);
        imageRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtImagePath = new TextField();
        txtImagePath.setPromptText("Chưa chọn ảnh...");
        txtImagePath.setEditable(false);
        txtImagePath.setStyle(inputStyle);
        HBox.setHgrow(txtImagePath, Priority.ALWAYS);

        // Preview ảnh nhỏ
        ImageView imgPreview = new ImageView();
        imgPreview.setFitWidth(50);
        imgPreview.setFitHeight(50);
        imgPreview.setPreserveRatio(true);
        imgPreview.setStyle("-fx-background-radius: 8;");

        Button btnChooseImage = new Button("📁 Chọn ảnh");
        btnChooseImage.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 8; -fx-cursor: hand;");
        btnChooseImage.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Chọn ảnh sản phẩm");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(
                            "Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
            );
            java.io.File file = fileChooser.showOpenDialog(popup);
            if (file != null) {
                String uri = file.toURI().toString();
                selectedImagePath[0] = uri;
                txtImagePath.setText(file.getName()); // hiện tên file
                // Preview
                try {
                    Image preview = new Image(uri, 50, 50, true, true);
                    imgPreview.setImage(preview);
                } catch (Exception ex) {
                    System.out.println("Không preview được: " + ex.getMessage());
                }
            }
        });

        imageRow.getChildren().addAll(txtImagePath, btnChooseImage, imgPreview);

        // Field phụ
        VBox boxExtra = new VBox(6);
        boxExtra.setVisible(false); boxExtra.setManaged(false);
        Label lblExtra = new Label("");
        lblExtra.setStyle(labelStyle);
        TextField txtExtra = new TextField();
        txtExtra.setStyle(inputStyle);
        boxExtra.getChildren().addAll(lblExtra, txtExtra);

        cmbCategory.setOnAction(e -> {
            String cat = cmbCategory.getValue();
            if (cat == null) return;
            switch (cat) {
                case "Electronics" -> { lblExtra.setText("🛡️  Bảo hành (tháng):"); txtExtra.setPromptText("VD: 12"); }
                case "Art"         -> { lblExtra.setText("🎨  Tên nghệ sĩ:");      txtExtra.setPromptText("VD: Van Gogh"); }
                case "Vehicle"     -> { lblExtra.setText("🚗  Dung tích (cc):");   txtExtra.setPromptText("VD: 1500"); }
            }
            boxExtra.setVisible(true); boxExtra.setManaged(true);
        });

        body.getChildren().addAll(
                makeLabeledField("📂  Danh mục *", cmbCategory),
                makeLabeledField("🏷️  Tên sản phẩm *", txtName),
                makeLabeledField("📝  Mô tả", txtDesc),
                makeLabeledField("💰  Giá khởi điểm *", priceRow),
                makeLabeledField("🖼️  Ảnh sản phẩm", imageRow),
                boxExtra
        );

        // Footer
        // --- 1. Tạo Footer ---
        HBox footer = makeFooter(popup, () -> {
            Item item = buildItemFromForm(
                    cmbCategory.getValue(), txtName.getText(),
                    txtDesc.getText(), txtPrice.getText(),
                    selectedImagePath[0], // dùng path đã chọn
                    txtExtra.getText());
            if (item == null) return;
            item.setSellerName(currentUsername);
            item.setSellerUsername(currentUsername);
            if (auctionClient != null)
                auctionClient.send(new AuctionMessage(MessageType.ITEM_REQUEST, item));
            showAlert(Alert.AlertType.INFORMATION, "Đã gửi",
                    "Sản phẩm \"" + item.getName() + "\" đã gửi Admin duyệt.");
            popup.close();
        }, "🏷️  Đăng sản phẩm");


        // --- 2. BỌC BODY VÀO SCROLLPANE Ở ĐÂY ---
        ScrollPane scrollBody = new ScrollPane(body);
        scrollBody.setFitToWidth(true);
        scrollBody.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBody.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");

        // (Tuỳ chọn: Nếu em muốn giới hạn chiều cao tổng của popup, em có thể
        // dùng dòng này để báo cho ScrollPane biết chiều cao kỳ vọng của nội dung hiển thị)
        scrollBody.setPrefViewportHeight(400);

        // --- 3. Add SCROLLBODY vào Root thay vì Body nguyên thủy ---
        root.getChildren().addAll(header, scrollBody, new Separator(), footer);

        // --- 4. Hiển thị Popup ---
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    /** Popup nạp tiền – giữ nguyên UI đẹp từ v2 */
    @FXML
    public void handleDeposit(ActionEvent event) {
        Stage popup = new Stage();
        popup.setTitle("Nạp tiền");
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(0);
        root.setPrefWidth(420);
        root.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 16;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(22, 28, 20, 28));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #3b82f6); "
                + "-fx-background-radius: 16 16 0 0;");
        Label lblSub = new Label("THANH TOÁN AN TOÀN");
        lblSub.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.7);");
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(32, 32); iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;");
        iconBox.getChildren().add(new Label("🏦"));
        Label lblTitle = new Label("Nạp tiền ");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        titleRow.getChildren().addAll(iconBox, lblTitle);
        header.getChildren().addAll(lblSub, titleRow);

        // Body
        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 20, 28));
        body.setStyle("-fx-background-color: white;");

        // Số dư hiện tại
        HBox balanceBox = new HBox(10);
        balanceBox.setAlignment(Pos.CENTER_LEFT);
        balanceBox.setPadding(new Insets(12, 16, 12, 16));
        balanceBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10; "
                + "-fx-border-color: #dbeafe; -fx-border-radius: 10;");
        VBox balanceText = new VBox(2);
        Label lblBT = new Label("Số dư hiện tại");
        lblBT.setStyle("-fx-font-size: 11; -fx-text-fill: #94aac8;");
        Label lblBV = new Label(String.format("%,d VNĐ", balance));
        lblBV.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #1976D2;");
        balanceText.getChildren().addAll(lblBT, lblBV);
        balanceBox.getChildren().addAll(new Label("💰"), balanceText);

        // Input số tiền
        HBox inputRow = new HBox(0);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        Label badgeVnd = new Label("  VNĐ  ");
        badgeVnd.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1976D2; "
                + "-fx-font-weight: bold; -fx-font-size: 12; "
                + "-fx-background-radius: 10 0 0 10; -fx-border-color: #dde3f0; "
                + "-fx-border-width: 1 0 1 1; -fx-border-radius: 10 0 0 10; -fx-padding: 10 10;");
        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Nhập số tiền muốn nạp...");
        txtAmount.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; "
                + "-fx-padding: 10 14; -fx-font-size: 13;");
        HBox.setHgrow(txtAmount, Priority.ALWAYS);
        inputRow.getChildren().addAll(badgeVnd, txtAmount);

        // Gợi ý nhanh
        HBox quickBtns = new HBox(8);
        for (String amt : new String[]{"50000", "100000", "500000", "1000000"}) {
            Button btn = new Button(String.format("%,d", Long.parseLong(amt)));
            btn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1976D2; "
                    + "-fx-font-weight: bold; -fx-font-size: 11; "
                    + "-fx-background-radius: 8; -fx-border-color: #dbeafe; -fx-border-radius: 8; "
                    + "-fx-padding: 6 10; -fx-cursor: hand;");
            btn.setOnAction(e -> txtAmount.setText(amt));
            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            quickBtns.getChildren().add(btn);
        }

        Label lblNote = new Label("ℹ️  Yêu cầu sẽ được Admin xác nhận trong vòng 5–15 phút.");
        lblNote.setStyle("-fx-font-size: 11; -fx-text-fill: #94aac8;");
        lblNote.setWrapText(true);

        body.getChildren().addAll(balanceBox, quickBtns, inputRow, lblNote);

        // Footer
        HBox footer = makeFooter(popup, () -> {
            String input = txtAmount.getText().trim().replace(",", "");
            if (input.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số tiền!"); return; }
            try {
                long amount = Long.parseLong(input);
                if (amount <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền phải lớn hơn 0!");
                    return;
                }
                DepositRequest request = new DepositRequest(currentUsername, amount);
                if (auctionClient != null) {
                    auctionClient.send(new AuctionMessage(MessageType.DEPOSIT_REQUEST, request));
                }
                showAlert(Alert.AlertType.INFORMATION, "Đã gửi yêu cầu",
                        String.format("Yêu cầu nạp %,d VNĐ đã ghi nhận!\nVui lòng chờ Admin xác nhận.", amount));
                popup.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ!");
            }
        }, "🏦  Xác nhận nạp tiền");

        root.getChildren().addAll(header, body, new Separator(), footer);
        popup.setScene(new Scene(root));
        popup.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (auctionClient != null) auctionClient.close();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Đăng nhập hệ thống");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    /** Tạo footer popup chuẩn: nút Huỷ + nút confirm */
    private HBox makeFooter(Stage popup, Runnable onConfirm, String confirmText) {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 28, 16, 28));
        footer.setStyle("-fx-background-color: #f8faff; -fx-background-radius: 0 0 16 16;");

        Button btnCancel = new Button("✕  Huỷ");
        btnCancel.setStyle("-fx-background-color: white; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 10; -fx-background-radius: 10; "
                + "-fx-text-fill: #4a6080; -fx-font-size: 13; -fx-font-weight: bold; "
                + "-fx-padding: 10 22; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> popup.close());

        Button btnConfirm = new Button(confirmText);
        btnConfirm.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 13; "
                + "-fx-background-radius: 10; -fx-padding: 10 22; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.35), 10, 0, 0, 3);");
        btnConfirm.setOnAction(e -> onConfirm.run());

        footer.getChildren().addAll(btnCancel, btnConfirm);
        return footer;
    }

    /** Wrap một control với label tiêu đề bên trên */
    private VBox makeLabeledField(String labelText, Node field) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e3154;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private Item buildItemFromForm(String category, String name, String desc,
                                   String priceText, String imageUrl, String extra) {
        if (category == null || name == null || name.isBlank()
                || priceText == null || priceText.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập danh mục, tên và giá.");
            return null;
        }
        try {
            double price = Double.parseDouble(priceText.trim().replace(",", ""));
            String id = UUID.randomUUID().toString();
            Item item;
            switch (category) {
                case "Electronics" -> {
                    Electronics el = new Electronics(id, name.trim(), desc.trim(), price, price);
                    if (extra != null && !extra.isBlank())
                        el.setWarrantyMonths(Integer.parseInt(extra.trim()));
                    item = el;
                }
                case "Art" -> {
                    Art art = new Art(id, name.trim(), desc.trim(), price, price);
                    art.setArtist(extra == null ? "" : extra.trim());
                    item = art;
                }
                case "Vehicle" -> {
                    Vehicle vehicle = new Vehicle(id, name.trim(), desc.trim(), price, price);
                    if (extra != null && !extra.isBlank())
                        vehicle.setEngineCapacity(Double.parseDouble(extra.trim()));
                    item = vehicle;
                }
                default -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Danh mục không hợp lệ.");
                    return null;
                }
            }
            item.setImageUrl(imageUrl == null ? "" : imageUrl.trim());
            item.setStatus(ItemStatus.PENDING);
            return item;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá hoặc thông tin thêm phải là số.");
            return null;
        }
    }

    private void updateBalanceLabels() {
        String text = String.format("%,d VNĐ", balance);
        if (lblBalance != null)      lblBalance.setText("Số dư: " + text);
        if (lblRightBalance != null) lblRightBalance.setText(text);
    }

    private void syncBalanceFromCurrentUser() {
        if (AuthService.getInstance().getCurrentUser() instanceof Bidder bidder) {
            balance = Math.round(bidder.getBalance());
        }
    }

    private String resolveFullName(String username) {
        if (AuthService.getInstance().getCurrentUser() != null
                && AuthService.getInstance().getCurrentUser().getUsername().equalsIgnoreCase(username)) {
            return AuthService.getInstance().getCurrentUser().getFullName();
        }
        return DatabaseConnection.getInstance().getUserTable().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .map(user -> user.getFullName())
                .findFirst()
                .orElse(username);
    }

    private void addLocalBalance(long amount) {
        balance += amount;
        if (AuthService.getInstance().getCurrentUser() instanceof Bidder bidder) {
            bidder.setBalance(bidder.getBalance() + amount);
        }
    }

    private Node createItemImageNode(Item item, double width, double height, String fallbackIcon, double fallbackSize) {
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            try {
                // true = background loading
                Image image = new Image(imageUrl, width, height, false, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                imageView.setPreserveRatio(false);
                imageView.setSmooth(true);
                imageView.setMouseTransparent(true);

                // Nếu load lỗi thì thay bằng icon
                image.errorProperty().addListener((obs, oldVal, isError) -> {
                    if (isError) {
                        Platform.runLater(() -> {
                            StackPane parent = (StackPane) imageView.getParent();
                            if (parent != null) {
                                parent.getChildren().remove(imageView);
                                parent.getChildren().add(createFallbackImageNode(fallbackIcon, fallbackSize));
                            }
                        });
                    }
                });

                return imageView;
            } catch (Exception e) {
                System.out.println("Lỗi load ảnh: " + e.getMessage());
            }
        }
        return createFallbackImageNode(fallbackIcon, fallbackSize);
    }

    private Node createFallbackImageNode(String fallbackIcon, double fallbackSize) {
        Label imgIcon = new Label(fallbackIcon);
        imgIcon.setStyle("-fx-font-size: " + fallbackSize + "; -fx-opacity: 0.35;");
        return imgIcon;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
