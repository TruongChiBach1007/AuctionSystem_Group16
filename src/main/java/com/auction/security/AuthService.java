package com.auction.security;

import com.auction.dao.IUserDAO;
import com.auction.dao.UserDAOImpl;
import com.auction.model.users.User;

public class AuthService {
    private static AuthService instance;
    private User currentUser;
    private IUserDAO userDAO; // Khai báo ở đây

    private AuthService() {
        // Khởi tạo trạm trung chuyển dữ liệu
        this.userDAO = new UserDAOImpl();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        // NHỜ DAO TÌM GIÚP USER
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
}