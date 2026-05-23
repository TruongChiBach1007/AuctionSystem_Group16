package com.auction.utils;

import com.auction.model.core.DepositRequest;
import com.auction.model.items.Electronics;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.items.Vehicle;
import com.auction.model.users.Admin;
import com.auction.model.users.Bidder;
import com.auction.model.users.Seller;
import com.auction.model.users.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final List<User> userTable;
    private final List<Item> itemTable;
    private final List<DepositRequest> depositRequestTable;

    private DatabaseConnection() {
        userTable = new ArrayList<>();
        itemTable = new ArrayList<>();
        depositRequestTable = new ArrayList<>();

        userTable.add(new Bidder(1, "dung123", "123", "Nguyễn Việt Dũng", "dung@uet.edu.vn", 500000.0));
        userTable.add(new Seller(2, "bach123", "123", "Trương Chí Bách", "bach@uet.edu.vn"));
        userTable.add(new Admin(3, "admin", "123", "Admin", "admin@auction.com", 1));
        userTable.add(new Bidder(4, "hminh", "123", "Trần Hoàng Minh", "hminh@uet.com", 500000.0));

        // iPhone 15 Pro Max
        Electronics iphone = new Electronics("item-1", "iPhone 15 Pro Max", "iPhone 15 Pro Max 256GB titan tự nhiên", 35000000, 35000000);
        iphone.setImageUrl(getClass().getResource("/com/auction/images/iphone.jpg").toExternalForm());
        iphone.setStatus(ItemStatus.APPROVED);
        iphone.setSellerName("admin");
        itemTable.add(iphone);

// Honda Civic 2024
        Vehicle honda = new Vehicle("item-2", "Honda Civic 2024", "Xe cũ của Sơn Tùng MTP, bị ngập nước", 720000000, 720000000);
        honda.setImageUrl(getClass().getResource("/com/auction/images/honda.jpg").toExternalForm());
        honda.setStatus(ItemStatus.APPROVED);
        honda.setSellerName("admin");
        itemTable.add(honda);
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public List<User> getUserTable() {
        return userTable;
    }

    public List<Item> getItemTable() {
        return itemTable;
    }

    public List<DepositRequest> getDepositRequestTable() {
        return depositRequestTable;
    }
}
