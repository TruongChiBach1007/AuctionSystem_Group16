package com.auction.security;

import com.auction.model.users.User;
import com.auction.utils.DatabaseConnection; // Quan trọng: phải có dòng này

public class AuthService {
    private static AuthService instance;
    private User currentUser;

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    // Chỉ để MỘT hàm login duy nhất ở đây
    public boolean login(String username, String password) {
        // 1. Lấy thực thể Database duy nhất
        DatabaseConnection db = DatabaseConnection.getInstance();

        // 2. Duyệt danh sách người dùng để so khớp
        for (User user : db.getUserTable()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                this.currentUser = user;
                System.out.println("Đăng nhập thành công! Chào mừng: " + user.getFullName());
                return true;
            }
        }

        // 3. Nếu không tìm thấy
        System.out.println("Lỗi: Tên đăng nhập hoặc mật khẩu không chính xác!");
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}