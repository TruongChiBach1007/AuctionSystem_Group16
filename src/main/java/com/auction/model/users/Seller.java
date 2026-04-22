package com.auction.model.users;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    // Thuộc tính riêng: Danh sách các tên sản phẩm mà người bán này đang đăng
    private List<String> listedItems;

    public Seller(int id, String username, String password, String fullName, String email) {
        // Gọi constructor của lớp cha User
        super(id, username, password, fullName, email);
        this.listedItems = new ArrayList<>();
    }

    // Chức năng: Thêm sản phẩm mới vào danh sách quản lý của người bán
    public void addListedItem(String itemName) {
        listedItems.add(itemName);
    }

    public List<String> getListedItems() {
        return listedItems;
    }
    @Override
    public void displayRoleInfo() {
        System.out.println("Vai trò: Người bán (Seller)");
        System.out.println("Số lượng sản phẩm đã đăng: " + listedItems.size());
    }
}