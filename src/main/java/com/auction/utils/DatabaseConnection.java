package com.auction.utils;

import com.auction.model.users.User;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final List<User> userTable;

    private DatabaseConnection() {
        userTable = new ArrayList<>();
        userTable.add(new com.auction.model.users.Bidder(1, "dung123", "123", "Nguyễn Việt Dũng", "dung@uet.edu.vn", 0.0));

        userTable.add(new com.auction.model.users.Seller(2, "bach123", "123", "Trương Chí Bách", "bach@uet.edu.vn"));

        userTable.add(new com.auction.model.users.Admin(3, "admin", "123", "Hệ Thống", "admin@auction.com", 1));

        userTable.add(new com.auction.model.users.Bidder(1, "hminh", "123", "Trần Hoàng Minh", "hminh@uet.com", 0.0));
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
}