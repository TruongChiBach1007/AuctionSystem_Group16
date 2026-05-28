package com.auction.controller;

import com.auction.dao.ItemDAOImpl;
import com.auction.model.items.Art;
import com.auction.model.items.Electronics;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.items.Vehicle;
import com.auction.network.AuctionClient;
import com.auction.network.AuctionMessage;
import com.auction.network.MessageType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SellerDashboardController {
    @FXML private Label lblUsername;
    @FXML private Label lblFormTitle;
    @FXML private Label lblExtra;
    @FXML private TableView<Item> tableItems;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, Double> colStartPrice;
    @FXML private TableColumn<Item, Double> colCurrentPrice;
    @FXML private TableColumn<Item, String> colStatus;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartPrice;
    @FXML private TextField txtImageUrl;  // chỉ hiển thị tên file, không nhập tay
    @FXML private TextField txtExtra;
    @FXML private ImageView imgPreview;   // thêm fx:id này vào FXML

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item selectedItem;
    private AuctionClient auctionClient;
    private final ItemDAOImpl itemDAO = new ItemDAOImpl();
    private String sellerName = "Seller";
    private String sellerUsername = "Seller";
    private String selectedImagePath = ""; // lưu URI thật của ảnh

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
        tableItems.setItems(itemList);

        cmbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        cmbCategory.setOnAction(e -> updateExtraField());
        tableItems.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadItemToForm(newVal));

        // txtImageUrl chỉ đọc, không cho nhập tay
        txtImageUrl.setEditable(false);
        txtImageUrl.setStyle("-fx-background-color: #f5f7fc; -fx-border-color: #dde3f0; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");

        connectSocket();
    }

    private void connectSocket() {
        auctionClient = new AuctionClient();
        try {
            auctionClient.connect(MessageType.REGISTER_SELLER, this::handleSocketMessage);
        } catch (IOException e) {
            System.out.println("Seller cannot connect socket: " + e.getMessage());
        }
    }

    private void handleSocketMessage(AuctionMessage message) {
        if (message == null || message.getType() == null || message.getItem() == null) return;
        javafx.application.Platform.runLater(() -> {
            Item item = message.getItem();
            if (item.getSellerUsername() == null
                    || !item.getSellerUsername().equalsIgnoreCase(sellerUsername)) {
                return;
            }
            if (message.getType() == MessageType.ITEM_PENDING
                    || message.getType() == MessageType.ITEM_APPROVED
                    || message.getType() == MessageType.ITEM_REJECTED) {
                upsertItemInTable(item);
            }
        });
    }

    private void upsertItemInTable(Item item) {
        for (int i = 0; i < itemList.size(); i++) {
            if (item.getId().equals(itemList.get(i).getId())) {
                itemList.set(i, item);
                return;
            }
        }
        itemList.add(item);
    }

    // ── CHỌN ẢNH TỪ MÁY (giống BidderDashboard) ──
    @FXML
    public void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImagePath = file.toURI().toString();
            txtImageUrl.setText(file.getName()); // chỉ hiện tên file

            // Preview ảnh nhỏ (nếu có imgPreview trong FXML)
            if (imgPreview != null) {
                try {
                    Image preview = new Image(selectedImagePath, 80, 80, true, true);
                    imgPreview.setImage(preview);
                    imgPreview.setVisible(true);
                    imgPreview.setManaged(true);
                } catch (Exception ex) {
                    System.out.println("Không preview được: " + ex.getMessage());
                }
            }
        }
    }

    private void updateExtraField() {
        String category = cmbCategory.getValue();
        if (category == null) return;
        lblExtra.setVisible(true);
        lblExtra.setManaged(true);
        txtExtra.setVisible(true);
        txtExtra.setManaged(true);

        switch (category) {
            case "Electronics" -> { lblExtra.setText("Bảo hành (tháng):"); txtExtra.setPromptText("VD: 12"); }
            case "Art"         -> { lblExtra.setText("Tên nghệ sĩ:");       txtExtra.setPromptText("VD: Van Gogh"); }
            case "Vehicle"     -> { lblExtra.setText("Dung tích động cơ:"); txtExtra.setPromptText("VD: 1.5"); }
        }
    }

    private void loadItemToForm(Item item) {
        if (item == null) return;
        selectedItem = item;
        lblFormTitle.setText("SỬA SẢN PHẨM");
        txtName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        txtStartPrice.setText(String.valueOf(item.getStartingPrice()));
        cmbCategory.setValue(item.getCategory());

        // Load ảnh
        selectedImagePath = item.getImageUrl() != null ? item.getImageUrl() : "";
        if (!selectedImagePath.isBlank()) {
            // Chỉ hiện tên file
            String displayName = selectedImagePath.contains("/")
                    ? selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1)
                    : selectedImagePath;
            txtImageUrl.setText(displayName);

            // Preview
            if (imgPreview != null) {
                try {
                    Image preview = new Image(selectedImagePath, 80, 80, true, true);
                    imgPreview.setImage(preview);
                    imgPreview.setVisible(true);
                    imgPreview.setManaged(true);
                } catch (Exception ex) {
                    imgPreview.setVisible(false);
                    imgPreview.setManaged(false);
                }
            }
        } else {
            txtImageUrl.clear();
            if (imgPreview != null) {
                imgPreview.setImage(null);
                imgPreview.setVisible(false);
                imgPreview.setManaged(false);
            }
        }

        if (item instanceof Electronics electronics) txtExtra.setText(String.valueOf(electronics.getWarrantyMonths()));
        if (item instanceof Art art)                 txtExtra.setText(art.getArtist());
        if (item instanceof Vehicle vehicle)         txtExtra.setText(String.valueOf(vehicle.getEngineCapacity()));
        updateExtraField();
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        clearForm();
        lblFormTitle.setText("THÊM SẢN PHẨM");
        selectedItem = null;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        Item item = buildItemFromForm();
        if (item == null) return;

        if (selectedItem != null) {
            int index = itemList.indexOf(selectedItem);
            itemList.set(index, item);
            itemDAO.updateItem(item);
            if (auctionClient != null) {
                auctionClient.send(new AuctionMessage(MessageType.ITEM_REQUEST, item));
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật sản phẩm.");
        } else {
            upsertItemInTable(item);
            itemDAO.addItem(item);
            if (auctionClient != null) {
                auctionClient.send(new AuctionMessage(MessageType.ITEM_REQUEST, item));
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi sản phẩm cho admin duyệt.");
        }

        clearForm();
        selectedItem = null;
    }

    private Item buildItemFromForm() {
        if (txtName.getText().isBlank() || cmbCategory.getValue() == null
                || txtStartPrice.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập tên, danh mục và giá.");
            return null;
        }

        try {
            String id    = selectedItem == null ? UUID.randomUUID().toString() : selectedItem.getId();
            String name  = txtName.getText().trim();
            String desc  = txtDescription.getText().trim();
            double price = Double.parseDouble(txtStartPrice.getText().trim().replace(",", ""));
            Item item;

            switch (cmbCategory.getValue()) {
                case "Electronics" -> {
                    Electronics el = new Electronics(id, name, desc, price, price);
                    if (!txtExtra.getText().isBlank())
                        el.setWarrantyMonths(Integer.parseInt(txtExtra.getText().trim()));
                    item = el;
                }
                case "Art" -> {
                    Art art = new Art(id, name, desc, price, price);
                    art.setArtist(txtExtra.getText().trim());
                    item = art;
                }
                case "Vehicle" -> {
                    Vehicle vehicle = new Vehicle(id, name, desc, price, price);
                    if (!txtExtra.getText().isBlank())
                        vehicle.setEngineCapacity(Double.parseDouble(txtExtra.getText().trim()));
                    item = vehicle;
                }
                default -> { return null; }
            }

            item.setSellerName(sellerName);
            item.setSellerUsername(sellerUsername);
            // Dùng selectedImagePath (URI thật) thay vì text của txtImageUrl
            item.setImageUrl(selectedImagePath.isBlank() ? "" : selectedImagePath);
            item.setStatus(ItemStatus.PENDING);
            return item;

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá hoặc thông tin thêm phải là số.");
            return null;
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để sửa.");
            return;
        }
        loadItemToForm(selected);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để xóa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa sản phẩm \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                itemList.remove(selected);
                itemDAO.deleteItem(selected.getId());
            }
        });
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        clearForm();
        selectedItem = null;
        lblFormTitle.setText("THÊM SẢN PHẨM");
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

    public void setLblUsername(String username) {
        initData(username, username);
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtStartPrice.clear();
        txtImageUrl.clear();
        txtExtra.clear();
        selectedImagePath = ""; // reset path ảnh
        cmbCategory.setValue(null);
        lblExtra.setVisible(false);
        lblExtra.setManaged(false);
        txtExtra.setVisible(false);
        txtExtra.setManaged(false);
        tableItems.getSelectionModel().clearSelection();

        // Reset preview
        if (imgPreview != null) {
            imgPreview.setImage(null);
            imgPreview.setVisible(false);
            imgPreview.setManaged(false);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initData(String username) {
        initData(username, username);
    }

    public void initData(String username, String fullName) {
        sellerUsername = username;
        sellerName = fullName == null || fullName.isBlank() ? username : fullName;
        lblUsername.setText("Tên tài khoản: " + sellerName.toUpperCase());
        loadSellerItems();
    }

    private void loadSellerItems() {
        itemList.setAll(itemDAO.getItemsBySeller(sellerUsername));
    }
}
