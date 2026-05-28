package com.auction.utils;

import com.auction.model.core.DepositRequest;
import com.auction.model.items.Electronics;
import com.auction.model.items.Item;
import com.auction.model.items.ItemStatus;
import com.auction.model.items.Vehicle;
import com.auction.model.users.Admin;
import com.auction.model.users.Bidder;
import com.auction.model.users.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    private static final String DATA_FILE_PROPERTY = "auction.data.file";
    private static final String DEFAULT_DATA_FILE = "data/auction-data.ser";
    private static DatabaseConnection instance;

    private final List<User> userTable;
    private final List<Item> itemTable;
    private final List<DepositRequest> depositRequestTable;

    private DatabaseConnection() {
        DataSnapshot snapshot = loadSnapshot();
        if (snapshot != null) {
            userTable = new ArrayList<>(snapshot.users);
            itemTable = new ArrayList<>(snapshot.items);
            depositRequestTable = new ArrayList<>(snapshot.depositRequests);
        } else {
            userTable = new ArrayList<>();
            itemTable = new ArrayList<>();
            depositRequestTable = new ArrayList<>();
            seedDefaultData();
            save();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized void save() {
        Path dataFile = dataFilePath();
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
                out.writeObject(new DataSnapshot(userTable, itemTable, depositRequestTable));
            }
        } catch (IOException e) {
            System.err.println("Cannot save auction data: " + e.getMessage());
        }
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

    private DataSnapshot loadSnapshot() {
        Path dataFile = dataFilePath();
        if (!Files.exists(dataFile)) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(dataFile))) {
            Object obj = in.readObject();
            if (obj instanceof DataSnapshot snapshot) {
                return snapshot;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Cannot load auction data, using defaults: " + e.getMessage());
        }
        return null;
    }

    private static Path dataFilePath() {
        return Path.of(System.getProperty(DATA_FILE_PROPERTY, DEFAULT_DATA_FILE)).toAbsolutePath();
    }

    private void seedDefaultData() {
        userTable.add(new Bidder(1, "dung123", "123", "Nguyen Viet Dung", "dung@uet.edu.vn", 500000.0));
        userTable.add(new Bidder(2, "bach123", "123", "Truong Chi Bach", "bach@uet.edu.vn", 500000.0));
        userTable.add(new Admin(3, "admin", "123", "Admin", "admin@auction.com", 1));
        userTable.add(new Bidder(4, "hminh", "123", "Tran Hoang Minh", "hminh@uet.com", 500000.0));

        Electronics iphone = new Electronics("item-1", "iPhone 15 Pro Max",
                "iPhone 15 Pro Max 256GB titan tu nhien", 36000000, 36000000);
        iphone.setImageUrl(getClass().getResource("/com/auction/images/iphone.jpg").toExternalForm());
        iphone.setStatus(ItemStatus.APPROVED);
        iphone.setSellerName("admin");
        iphone.setSellerUsername("admin");
        itemTable.add(iphone);

        Vehicle honda = new Vehicle("item-2", "Honda Civic 2024",
                "Xe cu cua Son Tung MTP, bi ngap nuoc", 800000000, 800000000);
        honda.setImageUrl(getClass().getResource("/com/auction/images/honda.jpg").toExternalForm());
        honda.setStatus(ItemStatus.APPROVED);
        honda.setSellerName("admin");
        honda.setSellerUsername("admin");
        itemTable.add(honda);
    }

    private static class DataSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<User> users;
        private final List<Item> items;
        private final List<DepositRequest> depositRequests;

        private DataSnapshot(List<User> users, List<Item> items, List<DepositRequest> depositRequests) {
            this.users = new ArrayList<>(users);
            this.items = new ArrayList<>(items);
            this.depositRequests = new ArrayList<>(depositRequests);
        }
    }
}
