package com.auction.controller;

import com.auction.model.items.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @FXML private TextField txtExtra;
    @FXML private Label lblExtra2;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item selectedItem = null; // item đang chọn để sửa

    @FXML
    public void initialize() {
        // Setup cột bảng
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        // Cột danh mục — lấy tên class
        colCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getClass().getSimpleName()
                )
        );

        // Cột trạng thái — tạm hardcode
        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("Đang mở")
        );

        tableItems.setItems(itemList);

        // Setup ComboBox danh mục
        cmbCategory.setItems(FXCollections.observableArrayList(
                "Electronics", "Art", "Vehicle"
        ));

        // Khi đổi danh mục thì hiện/ẩn field đặc trưng
        cmbCategory.setOnAction(e -> updateExtraField());

        // Khi click vào bảng thì load thông tin lên form
        tableItems.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> loadItemToForm(newVal)
        );
    }

    // Hiện/ẩn field đặc trưng theo danh mục
    private void updateExtraField() {
        String category = cmbCategory.getValue();
        if (category == null) return;
        switch (category) {
            case "Electronics":
                lblExtra.setText("Bảo hành (tháng):");
                lblExtra.setVisible(true); lblExtra.setManaged(true);
                txtExtra.setPromptText("VD: 12");
                txtExtra.setVisible(true); txtExtra.setManaged(true);
                break;
            case "Art":
                lblExtra.setText("Tên nghệ sĩ:");
                lblExtra.setVisible(true); lblExtra.setManaged(true);
                txtExtra.setPromptText("VD: Vincent van Gogh");
                txtExtra.setVisible(true); txtExtra.setManaged(true);
                break;
            case "Vehicle":
                lblExtra.setText("Năm sản xuất:");
                lblExtra.setVisible(true); lblExtra.setManaged(true);
                txtExtra.setPromptText("VD: 2024");
                txtExtra.setVisible(true); txtExtra.setManaged(true);
                break;
            default:
                lblExtra.setVisible(false); lblExtra.setManaged(false);
                txtExtra.setVisible(false); txtExtra.setManaged(false);
        }
    }

    // Load item lên form để sửa
    private void loadItemToForm(Item item) {
        if (item == null) return;
        selectedItem = item;
        lblFormTitle.setText("SỬA SẢN PHẨM");
        txtName.setText(item.getName());
        txtDescription.setText(item.getDescription());
        txtStartPrice.setText(String.valueOf(item.getStartingPrice()));

        if (item instanceof Electronics) {
            cmbCategory.setValue("Electronics");
            txtExtra.setText(String.valueOf(((Electronics) item).getWarrantyMonths()));
        } else if (item instanceof Art) {
            cmbCategory.setValue("Art");
        } else if (item instanceof Vehicle) {
            cmbCategory.setValue("Vehicle");
        }
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
        // Validate
        if (txtName.getText().isEmpty() || cmbCategory.getValue() == null
                || txtStartPrice.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try {
            String id = UUID.randomUUID().toString().substring(0, 8);
            String name = txtName.getText().trim();
            String desc = txtDescription.getText().trim();
            double startPrice = Double.parseDouble(txtStartPrice.getText().trim());
            String category = cmbCategory.getValue();

            Item newItem;
            switch (category) {
                case "Electronics":
                    int warranty = txtExtra.getText().isEmpty() ? 0
                            : Integer.parseInt(txtExtra.getText().trim());
                    Electronics e = new Electronics(id, name, desc, startPrice, startPrice);
                    e.setWarrantyMonths(warranty);
                    newItem = e;
                    break;
                case "Art":
                    newItem = new Art(id, name, desc, startPrice, startPrice);
                    break;
                case "Vehicle":
                    newItem = new Vehicle(id, name, desc, startPrice, startPrice);
                    break;
                default:
                    return;
            }

            if (selectedItem != null) {
                // Sửa: thay thế item cũ
                int index = itemList.indexOf(selectedItem);
                itemList.set(index, newItem);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật sản phẩm!");
            } else {
                // Thêm mới
                itemList.add(newItem);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm sản phẩm!");
            }

            clearForm();
            selectedItem = null;

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tiền hoặc bảo hành phải là số!");
        }
    }

    @FXML
    public void handleEdit(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để sửa!");
            return;
        }
        loadItemToForm(selected);
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        Item selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn sản phẩm để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                itemList.remove(selected);
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Đã xóa", "Xóa sản phẩm thành công!");
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

    public void setLblUsername(String username) {
        lblUsername.setText("Tên TK: " + username.toUpperCase());
    }

    private void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtStartPrice.clear();
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
}