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

        Electronics phone = new Electronics("sample-phone", "iPhone 15 Pro Max", "May moi fullbox", 35000000, 35000000);
        phone.setSellerName("He thong");
        phone.setSellerUsername("system");
        phone.setImageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/IPhone_15_Pro_%26_iPhone_15_Pro_Max.jpg?width=800");
        phone.setStatus(ItemStatus.APPROVED);
        itemTable.add(phone);

        Vehicle car = new Vehicle("sample-car", "Honda Civic 2024", "Xe Honda Civic RS 2024", 720000000, 720000000);
        car.setSellerName("He thong");
        car.setSellerUsername("system");
        car.setImageUrl("https://commons.wikimedia.org/wiki/Special:FilePath/2022_Honda_Civic_eHEV_Advance_2.0_Front.jpg?width=800");
        car.setStatus(ItemStatus.APPROVED);
        itemTable.add(car);
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
