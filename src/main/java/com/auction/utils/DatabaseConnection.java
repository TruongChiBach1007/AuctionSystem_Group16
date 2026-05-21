package com.auction.utils;

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

    private DatabaseConnection() {
        userTable = new ArrayList<>();
        itemTable = new ArrayList<>();

        userTable.add(new Bidder(1, "dung123", "123", "Nguyen Viet Dung", "dung@uet.edu.vn", 500000.0));
        userTable.add(new Seller(2, "bach123", "123", "Truong Chi Bach", "bach@uet.edu.vn"));
        userTable.add(new Admin(3, "admin", "123", "He Thong", "admin@auction.com", 1));
        userTable.add(new Bidder(4, "hminh", "123", "Tran Hoang Minh", "hminh@uet.com", 500000.0));

        Electronics phone = new Electronics("sample-phone", "iPhone 15 Pro Max", "May moi fullbox", 35000000, 35000000);
        phone.setSellerName("He thong");
        phone.setImageUrl("https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-max-blue-titanium-select?wid=470&hei=556&fmt=png-alpha&.v=1692894041808");
        phone.setStatus(ItemStatus.APPROVED);
        itemTable.add(phone);

        Vehicle car = new Vehicle("sample-car", "Honda Civic 2024", "Xe Honda Civic RS 2024", 720000000, 720000000);
        car.setSellerName("He thong");
        car.setImageUrl("https://www.honda.com.vn/otosites/images/cars/civic/overview/car.png");
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
}
