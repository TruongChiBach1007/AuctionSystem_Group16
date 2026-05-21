package com.auction.controller;

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
import javafx.stage.Stage;

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
    @FXML private TextField txtImageUrl;
    @FXML private TextField txtExtra;

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item selectedItem;
    private AuctionClient auctionClient;
    private String sellerName = "Seller";

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
        tableItems.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadItemToForm(newVal));
        connectSocket();
    }

    private void connectSocket() {
        auctionClient = new AuctionClient();
        try {
            auctionClient.connect(MessageType.REGISTER_SELLER, message -> {});
        } catch (IOException e) {
            System.out.println("Seller cannot connect socket: " + e.getMessage());
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
            case "Electronics" -> {
                lblExtra.setText("Bao hanh (thang):");
                txtExtra.setPromptText("VD: 12");
            }
            case "Art" -> {
                lblExtra.setText("Ten nghe si:");
                txtExtra.setPromptText("VD: Van Gogh");
            }
            case "Vehicle" -> {
                lblExtra.setText("Dung tich dong co:");
                txtExtra.setPromptText("VD: 1.5");
            }
        }
    }

    private void loadItemToForm(Item item) {
        if (item == null) return;
        selectedItem = item;
        lblFormTitle.setText("SUA SAN PHAM");
        txtName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        txtStartPrice.setText(String.valueOf(item.getStartingPrice()));
        txtImageUrl.setText(item.getImageUrl());
        cmbCategory.setValue(item.getCategory());
        if (item instanceof Electronics electronics) txtExtra.setText(String.valueOf(electronics.getWarrantyMonths()));
        if (item instanceof Art art) txtExtra.setText(art.getArtist());
        if (item instanceof Vehicle vehicle) txtExtra.setText(String.valueOf(vehicle.getEngineCapacity()));
        updateExtraField();
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        clearForm();
        lblFormTitle.setText("THEM SAN PHAM");
        selectedItem = null;
    }

    @FXML
    public void handleSave(ActionEvent event) {
        Item item = buildItemFromForm();
        if (item == null) return;

        if (selectedItem != null) {
            int index = itemList.indexOf(selectedItem);
            itemList.set(index, item);
            showAlert(Alert.AlertType.INFORMATION, "Thanh cong", "Da cap nhat san pham.");
        } else {
            itemList.add(item);
            auctionClient.send(new AuctionMessage(MessageType.ITEM_REQUEST, item));
            showAlert(Alert.AlertType.INFORMATION, "Thanh cong", "Da gui san pham cho Admin duyet.");
        }

        clearForm();
        selectedItem = null;
    }

    private Item buildItemFromForm() {
        if (txtName.getText().isBlank() || cmbCategory.getValue() == null || txtStartPrice.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Loi", "Vui long nhap ten, danh muc va gia.");
            return null;
        }

        try {
            String id = selectedItem == null ? UUID.randomUUID().toString() : selectedItem.getId();
            String name = txtName.getText().trim();
            String desc = txtDescription.getText().trim();
            double price = Double.parseDouble(txtStartPrice.getText().trim().replace(",", ""));
            Item item;

            switch (cmbCategory.getValue()) {
                case "Electronics" -> {
                    Electronics electronics = new Electronics(id, name, desc, price, price);
                    if (!txtExtra.getText().isBlank()) electronics.setWarrantyMonths(Integer.parseInt(txtExtra.getText().trim()));
                    item = electronics;
                }
                case "Art" -> {
                    Art art = new Art(id, name, desc, price, price);
                    art.setArtist(txtExtra.getText().trim());
                    item = art;
                }
                case "Vehicle" -> {
                    Vehicle vehicle = new Vehicle(id, name, desc, price, price);
                    if (!txtExtra.getText().isBlank()) vehicle.setEngineCapacity(Double.parseDouble(txtExtra.getText().trim()));
                    item = vehicle;
                }
                default -> {
                    return null;
                }
            }

            item.setSellerName(sellerName);
            item.setImageUrl(txtImageUrl.getText().trim());
            item.setStatus(ItemStatus.PENDING);
            return item;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Loi", "Gia hoac thong tin them phai la so.");
            return null;
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon san pham de sua.");
            return;
        }
        loadItemToForm(selected);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chua chon", "Vui long chon san pham de xoa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xac nhan xoa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xoa san pham \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) itemList.remove(selected);
        });
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        clearForm();
        selectedItem = null;
        lblFormTitle.setText("THEM SAN PHAM");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (auctionClient != null) auctionClient.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Dang nhap he thong");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLblUsername(String username) {
        sellerName = username;
        lblUsername.setText("Ten tai khoan: " + username.toUpperCase());
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtStartPrice.clear();
        txtImageUrl.clear();
        txtExtra.clear();
        cmbCategory.setValue(null);
        lblExtra.setVisible(false);
        lblExtra.setManaged(false);
        txtExtra.setVisible(false);
        txtExtra.setManaged(false);
        tableItems.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initData(String fullName) {
    }
}
