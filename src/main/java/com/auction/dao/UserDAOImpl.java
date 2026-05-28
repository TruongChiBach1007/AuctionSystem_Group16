package com.auction.dao;

import com.auction.model.users.User;
import com.auction.utils.DatabaseConnection;
import java.util.List;

public class UserDAOImpl implements IUserDAO {

    // Lấy dữ liệu từ Singleton DatabaseConnection
    private List<User> userTable;

    public UserDAOImpl() {
        // Kết nối đến "kho dữ liệu" duy nhất của hệ thống
        this.userTable = DatabaseConnection.getInstance().getUserTable();
    }

    @Override
    public User findByUsername(String username) {
        // Duyệt danh sách để tìm user khớp username
        for (User user : userTable) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null; // Không tìm thấy
    }

    @Override
    public List<User> getAllUsers() {
        return userTable;
    }

    @Override
    public void addUser(User user) {
        userTable.add(user);
        DatabaseConnection.getInstance().save();
    }
}
