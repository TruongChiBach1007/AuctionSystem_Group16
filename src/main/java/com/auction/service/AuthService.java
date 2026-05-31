package com.auction.service;

import com.auction.dao.IUserDAO;
import com.auction.dao.UserDAOImpl;
import com.auction.model.users.User;
import com.auction.utils.DatabaseConnection;

public class AuthService {
    private static AuthService instance;
    private User currentUser;
    private IUserDAO userDAO;

    private AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        // ✅ SỬA: Reload dữ liệu từ file trước khi tìm user
        // Đảm bảo lấy balance mới nhất sau khi admin xác nhận nạp tiền
        DatabaseConnection.getInstance().reload();

        User user = userDAO.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // ✅ THÊM MỚI: cho phép cập nhật lại currentUser khi cần
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
